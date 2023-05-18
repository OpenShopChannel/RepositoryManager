import json
import os
import shutil
import time
import zipfile
import tempfile
from datetime import datetime

import py7zr
import requests
import xmltodict

import config
import helpers
import treatments.contents

# why do we use IO instead of the database we already have set up?
# because it's easier to manage. we don't need to worry about migrations, etc.
# we can just use a simple JSON file to store the index! (SSD manufacturers hate this guy)
# you might be wondering, "why do we use a database at all?", and the answer is "passwords, settings and analytics"


def initialize():
    # check if an index file exists
    if not os.path.exists(os.path.join('data', 'index.json')):
        # create an empty index file
        repo_index = {'repository': {'name': 'No repository configured',
                                     'provider': 'Open Shop Channel',
                                     'description': 'Please finish the setup process'},
                      'contents': []}

        if not os.path.exists('data'):
            os.mkdir('data')
        with open(os.path.join('data', 'index.json'), 'w') as f:
            json.dump(repo_index, f)


def update():
    helpers.log_status(f'Updating repository index')
    repo_index = {}

    # index the repository.json file
    with open(os.path.join(config.REPO_DIR, 'repository.json')) as f:
        repo_info = json.load(f)

        repo_index['repository'] = repo_info

    # index the application manifests
    repo_index['contents'] = []
    for file in os.listdir(os.path.join(config.REPO_DIR, 'contents')):
        # check if the file is a .oscmeta file
        if file.endswith('.oscmeta'):
            # open the file
            with open(os.path.join(config.REPO_DIR, 'contents', file)) as f:
                oscmeta = json.load(f)

                helpers.log_status(f'Loaded {file}')

                # add the slug to the oscmeta
                oscmeta["slug"] = file.replace('.oscmeta', '')

                # add the oscmeta to the index
                repo_index['contents'].append(oscmeta)

                # download application files to add to the index, and update the oscmeta with the obtained meta.xml
                oscmeta["metaxml"] = update_application(oscmeta)

    # write the index to the index file
    with open(os.path.join('data', 'index.json'), 'w') as f:
        json.dump(repo_index, f)

    helpers.log_status(f'Finished updating repository index', 'success')

    return repo_index


def get():
    # open the index file
    with open(os.path.join('data', 'index.json')) as f:
        return json.load(f)


def update_application(oscmeta):
    # update the application contents in the index
    helpers.log_status(f'Updating application {oscmeta["slug"]}')

    metadata = None
    app_directory = helpers.app_index_directory_location(oscmeta["slug"])

    # create contents directory if it doesn't exist
    if not os.path.exists(os.path.join('data', 'contents')):
        os.mkdir(os.path.join('data', 'contents'))

    # create temporary directory where we will download the application files and run the treatments
    with tempfile.TemporaryDirectory() as temp_dir:
        # download the application files
        helpers.log_status(f'- Starting Downloads')
        match oscmeta["source"]["type"]:
            case "url":
                url = oscmeta["source"]["location"]

                match oscmeta["source"]["format"]:
                    case "zip":
                        # why do we bother to add the file extension to the filename?
                        # because windows complains otherwise
                        filename = os.path.join(temp_dir, oscmeta["slug"] + ".zip")

                        with open(filename, "wb") as f:
                            f.write(requests.get(url).content)

                        # extract the zip file
                        with zipfile.ZipFile(filename, 'r') as zip_ref:
                            zip_ref.extractall(temp_dir)

                        # remove the zip file
                        os.remove(filename)
                    case "7z":
                        filename = os.path.join(temp_dir, oscmeta["slug"] + ".7z")

                        with open(filename, "wb") as f:
                            f.write(requests.get(url).content)

                        with py7zr.SevenZipFile(filename, 'r') as archive:
                            archive.extractall(temp_dir)

                        # remove the 7z file
                        os.remove(filename)
                    case _:
                        Exception("Unsupported source format")
            case _:
                Exception("Unsupported source type")

        helpers.log_status(f'- Applying Treatments:')

        # process treatments, eventually will be moved to a separate function
        if "treatments" in oscmeta:
            for key, value in oscmeta["treatments"].items():
                if key == "contents.move":
                    treatments.contents.move(temp_dir, value)

        # remove the app directory if it exists (to ensure we don't have any old files)
        if os.path.exists(os.path.join(app_directory)):
            shutil.rmtree(os.path.join(app_directory))

        # copy the files to app directory in the index
        shutil.copytree(temp_dir, app_directory, dirs_exist_ok=True)

    helpers.log_status(f'- Creating ZIP file')

    # we will create a zip for homebrew browser and the API to use
    # create the zip file
    with zipfile.ZipFile(os.path.join("data", "contents", oscmeta["slug"] + ".zip"), 'w') as zip_ref:
        # iterate through the files starting at the app directory
        for root, dirs, files in os.walk(app_directory):
            # iterate through the files
            for file in files:
                # get the full path of the file
                file_path = os.path.join(root, file)

                # get the relative path of the file
                relative_path = os.path.relpath(file_path, app_directory)

                # add the file to the zip
                zip_ref.write(file_path, relative_path)

    # open the metadata file
    with open(os.path.join(app_directory, 'apps', oscmeta["slug"], "meta.xml")) as f:
        # convert the metadata to JSON
        metadata = json.loads(json.dumps(xmltodict.parse(f.read())))

    # time for determining some extra information needed by API and Homebrew Browser, and adding to index
    helpers.log_status(f'- Retrieving Extra Information')
    oscmeta["index_computed_info"] = {}

    # Check type (dol/elf)
    if os.path.exists(os.path.join(app_directory, 'apps', oscmeta["slug"], "boot.dol")):
        oscmeta["index_computed_info"]["package_type"] = "dol"
    elif os.path.exists(os.path.join(app_directory, 'apps', oscmeta["slug"], "boot.elf")):
        oscmeta["index_computed_info"]["package_type"] = "elf"

    # determine release date timestamp and add it to the oscmeta file
    if "release_date" in metadata["app"]:
        timestamp = metadata["app"]["release_date"]
        if len(timestamp) == 14:
            timestamp = datetime.strptime(timestamp, "%Y%m%d%H%M%S")
        elif len(timestamp) == 12:
            timestamp = datetime.strptime(timestamp, "%Y%m%d%H%M")
        elif len(timestamp) == 8:
            timestamp = datetime.strptime(timestamp, "%Y%m%d")
        else:
            # we will obtain the timestamp from the boot.dol creation date, because the meta.xml lacks a release date
            timestamp = os.path.getmtime(os.path.join(app_directory, 'apps', oscmeta["slug"],
                                                      "boot." + oscmeta["index_computed_info"]["package_type"]))
            timestamp = datetime.fromtimestamp(timestamp)
    else:
        # we will obtain the timestamp from the boot.dol creation date, because the meta.xml lacks a release date
        timestamp = os.path.getmtime(os.path.join(app_directory, 'apps', oscmeta["slug"],
                                                  "boot." + oscmeta["index_computed_info"]["package_type"]))
        timestamp = datetime.fromtimestamp(timestamp)

    timestamp = int(time.mktime(timestamp.timetuple()))

    oscmeta["index_computed_info"]["release_date"] = timestamp

    # File size of icon.png
    oscmeta["index_computed_info"]["icon_size"] = os.path.getsize(os.path.join(app_directory, 'apps', oscmeta["slug"], "icon.png"))

    # File size of zip
    oscmeta["index_computed_info"]["compressed_size"] = os.path.getsize(os.path.join("data", "contents", oscmeta["slug"] + ".zip"))

    # File size of dol/elf
    oscmeta["index_computed_info"]["binary_size"] = os.path.getsize(os.path.join(app_directory, 'apps', oscmeta["slug"], "boot." + oscmeta["index_computed_info"]["package_type"]))

    # Uncompressed size of zip
    oscmeta["index_computed_info"]["uncompressed_size"] = 0
    with zipfile.ZipFile(os.path.join("data", "contents", oscmeta["slug"] + ".zip"), 'r') as zip_ref:
        for file in zip_ref.infolist():
            oscmeta["index_computed_info"]["uncompressed_size"] += file.file_size

    # Build peripherals string
    oscmeta["index_computed_info"]["peripherals"] = ""
    for peripheral in oscmeta["peripherals"]:
        match peripheral:
            case "Wii Remote":
                oscmeta["index_computed_info"]["peripherals"] += "w"
            case "GameCube Controller":
                oscmeta["index_computed_info"]["peripherals"] += "g"
            case "Nunchuk":
                oscmeta["index_computed_info"]["peripherals"] += "n"
            case "Classic Controller":
                oscmeta["index_computed_info"]["peripherals"] += "c"
            case "SDHC":
                oscmeta["index_computed_info"]["peripherals"] += "s"
            case "USB Keyboard":
                oscmeta["index_computed_info"]["peripherals"] += "k"
            case "Wii Zapper":
                oscmeta["index_computed_info"]["peripherals"] += "z"

    # Create subdirectories list
    oscmeta["index_computed_info"]["subdirectories"] = []
    for root, dirs, files in os.walk(os.path.join(app_directory, 'apps', oscmeta["slug"])):
        for dir in dirs:
            # we need to make sure that these strings are in the right format for the HBB.
            # example format: /apps/slug/subdirectory1/subdirectory2
            oscmeta["index_computed_info"]["subdirectories"].append(os.path.relpath(os.path.join(root, dir), os.path.join(app_directory, 'apps', oscmeta["slug"])))
            oscmeta["index_computed_info"]["subdirectories"][-1] = "/apps/" + oscmeta["slug"] + "/" + oscmeta["index_computed_info"]["subdirectories"][-1].replace("\\", "/")

    helpers.log_status(f'- Adding to Index')

    return metadata
