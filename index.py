import json
import os
import shutil
import time
import zipfile
import tempfile
import fnmatch
from datetime import datetime

import eventlet
import requests
import xmltodict

import config
import helpers
import treatments

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

    # create icons directory if it doesn't exist
    if not os.path.exists(os.path.join("data", "icons")):
        os.makedirs(os.path.join("data", "icons"))

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
                oscmeta["information"]["slug"] = file.replace('.oscmeta', '')

                # download application files to add to the index, and update the oscmeta with the obtained meta.xml
                try:
                    oscmeta["metaxml"] = update_application(oscmeta)
                except (Exception, eventlet.timeout.Timeout) as e:
                    helpers.log_status(f'Failed to process {file}, moving on. ({type(e).__name__}: {e})', 'error')

                    # load the previous index file and set it to the current index for this app, to avoid losing it
                    with open(os.path.join('data', 'index.json')) as f:
                        old_repo_index = json.load(f)
                        for app in old_repo_index['contents']:
                            if app["information"]["slug"] == oscmeta["information"]["slug"]:
                                oscmeta = app

                # add the oscmeta to the index if it includes a metaxml
                if "metaxml" in oscmeta:
                    repo_index['contents'].append(oscmeta)

    # write the index to the index file
    with open(os.path.join('data', 'index.json'), 'w') as f:
        json.dump(repo_index, f)

    # remove the icons zip if it exists
    if os.path.exists(os.path.join("data", "icons.zip")):
        os.remove(os.path.join("data", "icons.zip"))

    # put all icons into a zip file called icons.zip
    # this will be used for the icon cache that the HBB downloads
    helpers.log_status(f'Creating HBB icon cache')
    if not os.path.exists(os.path.join("data", "icons")):
        os.makedirs(os.path.join("data", "icons"))

    for oscmeta in repo_index["contents"]:
        shutil.copy(os.path.join(helpers.app_index_directory_location(oscmeta["information"]["slug"]), 'apps',
                                 oscmeta["information"]["slug"], "icon.png"),
                    os.path.join("data", "icons", oscmeta["information"]["slug"] + ".png"))

    with zipfile.ZipFile(os.path.join("data", "icons.zip"), "w") as zipf:
        for file in os.listdir(os.path.join("data", "icons")):
            zipf.write(os.path.join("data", "icons", file), file)

    shutil.rmtree(os.path.join("data", "icons"))

    helpers.log_status(f'Finished updating repository index', 'success')
    return repo_index


def get():
    # open the index file
    with open(os.path.join('data', 'index.json')) as f:
        return json.load(f)


def update_application(oscmeta):
    # update the application contents in the index
    helpers.log_status(f'Updating application {oscmeta["information"]["slug"]}')

    metadata = None
    app_directory = helpers.app_index_directory_location(oscmeta["information"]["slug"])

    # create contents directory if it doesn't exist
    if not os.path.exists(os.path.join('data', 'contents')):
        os.mkdir(os.path.join('data', 'contents'))

    # determine user-agent
    # check if user-agent is set
    if "user-agent" in oscmeta["source"]:
        # check if this user-agent string is included in the config.SECRET_USER_AGENTS dictionary
        if oscmeta["source"]["user-agent"] in config.SECRET_USER_AGENTS:
            # if it is, use the actual user-agent string
            user_agent = config.SECRET_USER_AGENTS[oscmeta["source"]["user-agent"]]
        else:
            user_agent = oscmeta["source"]["user-agent"]
    else:
        user_agent = config.USER_AGENT

    # create temporary directory where we will download the application files and run the treatments
    with tempfile.TemporaryDirectory() as temp_dir:
        # download the application files
        helpers.log_status(f'- Downloading application files')
        match oscmeta["source"]["type"]:
            case "url":
                url = oscmeta["source"]["location"]

                headers = {"User-Agent": user_agent}

                filename = os.path.join(temp_dir, oscmeta["information"]["slug"] + ".package")
                # download the file
                with open(filename, "wb") as f:
                    with eventlet.Timeout(config.URL_DOWNLOAD_TIMEOUT):
                        if "user-agent" in oscmeta["source"]:
                            helpers.log_status(f'  - Using custom user-agent: {oscmeta["source"]["user-agent"]}')
                        f.write(requests.get(url, headers=headers).content)

            case "github_release":
                if config.GITHUB_TOKEN != "":
                    # we have a token, let's use it
                    helpers.log_status(f'  - Authenticating with GitHub')
                    headers = {"Authorization": f"token {config.GITHUB_TOKEN}"}
                else:
                    helpers.log_status(f'  - No valid GitHub token found, using unauthenticated requests. '
                                       f'Please configure a token in config.py')
                    headers = {}

                # fetch the latest release
                url = f'https://api.github.com/repos/{oscmeta["source"]["repository"]}/releases/latest'
                response = requests.get(url, headers=headers)

                if response.status_code == 200:
                    helpers.log_status(f'  - Successfully fetched latest release')
                    assets = response.json()["assets"]
                    for asset in assets:
                        # check if asset name matches pattern
                        if fnmatch.fnmatch(asset["name"], oscmeta["source"]["file"]):
                            helpers.log_status(f'  - Found asset {asset["name"]}')
                            # download the asset
                            url = asset["browser_download_url"]

                            filename = os.path.join(temp_dir, oscmeta["information"]["slug"] + ".package")
                            # download the file
                            with open(filename, "wb") as f:
                                f.write(requests.get(url).content)

                            helpers.log_status(f'  - Downloaded asset {asset["name"]}')
                            break
            case "manual":
                helpers.log_status(f'  - Manual source type, downloads will be handled by treatments')
            case _:
                Exception("Unsupported source type")

        # extract the application files
        if oscmeta["source"]["type"] != "manual":
            helpers.log_status('- Extracting application files')
            shutil.unpack_archive(filename, temp_dir, oscmeta["source"]["format"])
            os.remove(filename)

        helpers.log_status(f'- Applying Treatments:')

        # process treatments, eventually will be moved to a separate function
        if "treatments" in oscmeta:
            for treatment in oscmeta["treatments"]:
                # math the treatment group (e.g. contents)
                match treatment["treatment"][:treatment["treatment"].index(".")]:
                    case "contents":
                        contents = treatments.Contents(temp_dir, oscmeta, oscmeta["information"]["slug"])
                        # match the treatment (e.g. move)
                        match treatment["treatment"][treatment["treatment"].index(".") + 1:]:
                            case "move":
                                contents.move(treatment["arguments"])
                            case "delete":
                                contents.delete(treatment["arguments"])
                    case "meta":
                        meta = treatments.Meta(temp_dir, oscmeta, oscmeta["information"]["slug"])
                        match treatment["treatment"][treatment["treatment"].index(".") + 1:]:
                            case "init":
                                meta.init()
                            case "set":
                                meta.set(treatment["arguments"])
                            case "remove_declaration":
                                meta.remove_declaration()
                            case "remove_comments":
                                meta.remove_comments()
                    case "web":
                        web = treatments.Web(user_agent, temp_dir, oscmeta, oscmeta["information"]["slug"])
                        match treatment["treatment"][treatment["treatment"].index(".") + 1:]:
                            case "download":
                                web.download(treatment["arguments"])
                    case "archive":
                        archive = treatments.Archive(temp_dir, oscmeta, oscmeta["information"]["slug"])
                        match treatment["treatment"][treatment["treatment"].index(".") + 1:]:
                            case "extract":
                                archive.extract(treatment["arguments"])

        # remove the app directory if it exists (to ensure we don't have any old files)
        if os.path.exists(os.path.join(app_directory)):
            shutil.rmtree(os.path.join(app_directory))

        # copy the files to app directory in the index
        shutil.copytree(temp_dir, app_directory, dirs_exist_ok=True)

    helpers.log_status(f'- Creating ZIP file')

    # we will create a zip for homebrew browser and the API to use
    # create the zip file
    with zipfile.ZipFile(os.path.join("data", "contents", oscmeta["information"]["slug"] + ".zip"), 'w',
                         compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zip_ref:
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
    with open(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"], "meta.xml")) as f:
        # convert the metadata to JSON
        metadata = json.loads(json.dumps(xmltodict.parse(f.read())))

    # time for determining some extra information needed by API and Homebrew Browser, and adding to index
    helpers.log_status(f'- Retrieving Extra Information')
    oscmeta["index_computed_info"] = {}

    # Check type (dol/elf)
    if os.path.exists(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"], "boot.dol")):
        oscmeta["index_computed_info"]["package_type"] = "dol"
    elif os.path.exists(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"], "boot.elf")):
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
            timestamp = os.path.getmtime(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"],
                                                      "boot." + oscmeta["index_computed_info"]["package_type"]))
            timestamp = datetime.fromtimestamp(timestamp)
    else:
        # we will obtain the timestamp from the boot.dol creation date, because the meta.xml lacks a release date
        timestamp = os.path.getmtime(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"],
                                                  "boot." + oscmeta["index_computed_info"]["package_type"]))
        timestamp = datetime.fromtimestamp(timestamp)

    timestamp = int(time.mktime(timestamp.timetuple()))

    oscmeta["index_computed_info"]["release_date"] = timestamp

    # File size of icon.png
    oscmeta["index_computed_info"]["icon_size"] = os.path.getsize(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"], "icon.png"))

    # File size of zip
    oscmeta["index_computed_info"]["compressed_size"] = os.path.getsize(os.path.join("data", "contents", oscmeta["information"]["slug"] + ".zip"))

    # File size of dol/elf
    oscmeta["index_computed_info"]["binary_size"] = os.path.getsize(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"], "boot." + oscmeta["index_computed_info"]["package_type"]))

    # Uncompressed size of zip
    oscmeta["index_computed_info"]["uncompressed_size"] = 0
    with zipfile.ZipFile(os.path.join("data", "contents", oscmeta["information"]["slug"] + ".zip"), 'r') as zip_ref:
        for file in zip_ref.infolist():
            oscmeta["index_computed_info"]["uncompressed_size"] += file.file_size

    # Build peripherals string
    oscmeta["index_computed_info"]["peripherals"] = ""
    for peripheral in oscmeta["information"]["peripherals"]:
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
    for root, dirs, files in os.walk(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"])):
        for dir in dirs:
            # we need to make sure that these strings are in the right format for the HBB.
            # example format: /apps/slug/subdirectory1/subdirectory2
            oscmeta["index_computed_info"]["subdirectories"].append(os.path.relpath(os.path.join(root, dir), os.path.join(app_directory, 'apps', oscmeta["information"]["slug"])))
            oscmeta["index_computed_info"]["subdirectories"][-1] = "/apps/" + oscmeta["information"]["slug"] + "/" + oscmeta["index_computed_info"]["subdirectories"][-1].replace("\\", "/")

    helpers.log_status(f'- Adding to Index')

    return metadata
