import json
import os
import shutil
import zipfile
import tempfile

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

                # add the slug to the oscmeta
                oscmeta["slug"] = file.replace('.oscmeta', '')

                # add the oscmeta to the index
                repo_index['contents'].append(oscmeta)

                # download application files to add to the index, and update the oscmeta with the obtained meta.xml
                oscmeta["metaxml"] = update_application(oscmeta)

    # write the index to the index file
    with open(os.path.join('data', 'index.json'), 'w') as f:
        json.dump(repo_index, f)

    return repo_index


def get():
    # open the index file
    with open(os.path.join('data', 'index.json')) as f:
        return json.load(f)


def update_application(oscmeta):
    # update the application contents in the index
    print(f'Updating application {oscmeta["slug"]}')

    metadata = None
    app_directory = helpers.app_index_directory_location(oscmeta["slug"])

    # create contents directory if it doesn't exist
    if not os.path.exists(os.path.join('data', 'contents')):
        os.mkdir(os.path.join('data', 'contents'))

    # create temporary directory where we will download the application files and run the treatments
    with tempfile.TemporaryDirectory() as temp_dir:
        # download the application files
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

    # open the metadata file
    with open(os.path.join(app_directory, 'apps', oscmeta["slug"], "meta.xml")) as f:
        # convert the metadata to JSON
        metadata = json.loads(json.dumps(xmltodict.parse(f.read())))

    return metadata
