import hashlib
import json
import os
import pathlib
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
import logger
import treatments
from models import db, ModeratedBinariesModel


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
    log = logger.Log("index")
    log.log_status(f'Updating repository index')
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
    i = 0
    files = os.listdir(os.path.join(config.REPO_DIR, 'contents'))
    for file in files:
        i += 1
        # check if the file is a .oscmeta file
        if file.endswith('.oscmeta'):
            # open the file
            with open(os.path.join(config.REPO_DIR, 'contents', file)) as f:
                try:
                    oscmeta = json.load(f)
                except Exception as e:
                    log.log_status(f"Failed to parse JSON: \"{type(e).__name__}: {e}\"")

                log.log_status(f'Loaded {file} ({i}/{len(files)})')

                # add the slug to the oscmeta
                oscmeta["information"]["slug"] = file.replace('.oscmeta', '')

                # download application files to add to the index, and update the oscmeta with the obtained meta.xml
                try:
                    oscmeta["metaxml"] = update_application(oscmeta, log)
                except (Exception, eventlet.timeout.Timeout) as e:
                    log.log_status(f'Failed to process {file}, moving on. ({type(e).__name__}: {e})', 'error')

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
    log.log_status(f'Creating HBB icon cache')
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

    log.log_status(f'Finished updating repository index', 'success')
    return repo_index


def get():
    # open the index file
    with open(os.path.join('data', 'index.json')) as f:
        return json.load(f)


def update_application(oscmeta, log=logger.Log("application_update")):
    # update the application contents in the index
    log.log_status(f'Updating application {oscmeta["information"]["slug"]}')

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
        log.log_status(f'- Downloading application files')
        match oscmeta["source"]["type"]:
            case "url":
                url = oscmeta["source"]["location"]

                headers = {"User-Agent": user_agent}

                archive_filename = os.path.join(temp_dir, oscmeta["information"]["slug"] + ".package")
                # download the file
                with open(archive_filename, "wb") as f:
                    with eventlet.Timeout(config.URL_DOWNLOAD_TIMEOUT):
                        if "user-agent" in oscmeta["source"]:
                            log.log_status(f'  - Using custom user-agent: {oscmeta["source"]["user-agent"]}')
                        f.write(requests.get(url, headers=headers).content)

            case "github_release":
                if config.GITHUB_TOKEN != "":
                    # we have a token, let's use it
                    log.log_status(f'  - Authenticating with GitHub')
                    headers = {"Authorization": f"token {config.GITHUB_TOKEN}"}
                else:
                    log.log_status(f'  - No valid GitHub token found, using unauthenticated requests. '
                                       f'Please configure a token in config.py')
                    headers = {}

                # fetch the latest release
                url = f'https://api.github.com/repos/{oscmeta["source"]["repository"]}/releases/latest'
                response = requests.get(url, headers=headers)

                if response.status_code == 200:
                    log.log_status(f'  - Successfully fetched latest release')
                    assets = response.json()["assets"]

                    # check if additional files are specified
                    if "additional_files" in oscmeta["source"]:
                        files = [oscmeta["source"]["file"]] + oscmeta["source"]["additional_files"]
                    else:
                        files = [oscmeta["source"]["file"]]

                    for file in files:
                        for asset in assets:
                            # check if asset name matches pattern
                            if fnmatch.fnmatch(asset["name"], file):
                                log.log_status(f'  - Found asset {asset["name"]}')
                                # download the asset
                                url = asset["browser_download_url"]

                                # check if archive
                                if file == oscmeta["source"]["file"]:
                                    archive_filename = os.path.join(temp_dir, oscmeta["information"]["slug"] + ".package")

                                    # download the file
                                    with open(archive_filename, "wb") as f:
                                        f.write(requests.get(url).content)
                                else:
                                    # download the file
                                    with open(os.path.join(temp_dir, file), "wb") as f:
                                        f.write(requests.get(url).content)

                                log.log_status(f'  - Downloaded asset {asset["name"]}')
                                break
            case "sourceforge_release":
                best_release = requests.get(
                    f"https://sourceforge.net/projects/{oscmeta['source']['project']}/best_release.json").json()
                log.log_status("- Successfully \"best release\" information from SourceForge")

                archive_filename = os.path.join(temp_dir, oscmeta["information"]["slug"] + ".package")

                # download the archive
                with open(archive_filename, "wb") as f:
                    f.write(requests.get(best_release["platform_releases"]["windows"]["url"]).content)
                log.log_status(f"- Downloaded file \"{ best_release['release']['filename'] }\"")
            case "manual":
                log.log_status(f'  - Manual source type, downloads will be handled by treatments')
            case _:
                Exception("Unsupported source type")

        # extract the application files
        if oscmeta["source"]["type"] != "manual":
            log.log_status('- Extracting application files')
            shutil.unpack_archive(archive_filename, temp_dir, oscmeta["source"]["format"])
            os.remove(archive_filename)

        log.log_status(f'- Applying Treatments:')

        # process treatments, eventually will be moved to a separate function
        if "treatments" in oscmeta:
            for treatment in oscmeta["treatments"]:
                # math the treatment group (e.g. contents)
                match treatment["treatment"][:treatment["treatment"].index(".")]:
                    case "contents":
                        contents = treatments.Contents(temp_dir, oscmeta, oscmeta["information"]["slug"], log)
                        # match the treatment (e.g. move)
                        match treatment["treatment"][treatment["treatment"].index(".") + 1:]:
                            case "move":
                                contents.move(treatment["arguments"])
                            case "delete":
                                contents.delete(treatment["arguments"])
                    case "meta":
                        meta = treatments.Meta(temp_dir, oscmeta, oscmeta["information"]["slug"], log)
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
                        web = treatments.Web(user_agent, temp_dir, oscmeta, oscmeta["information"]["slug"], log)
                        match treatment["treatment"][treatment["treatment"].index(".") + 1:]:
                            case "download":
                                web.download(treatment["arguments"])
                    case "archive":
                        archive = treatments.Archive(temp_dir, oscmeta, oscmeta["information"]["slug"], log)
                        match treatment["treatment"][treatment["treatment"].index(".") + 1:]:
                            case "extract":
                                archive.extract(treatment["arguments"])

        # we will check if all files are in the location they are expected to be in
        if not os.path.exists(os.path.join(temp_dir, 'apps', oscmeta["information"]["slug"], "icon.png")):
            raise Exception("Couldn't find icon.png file.")
        if not os.path.exists(os.path.join(temp_dir, 'apps', oscmeta["information"]["slug"], "meta.xml")):
            raise Exception("Couldn't find meta.xml file.")

        # create a dictionary for extra stuff
        oscmeta["index_computed_info"] = {}

        # Check type (dol/elf)
        if os.path.exists(os.path.join(temp_dir, 'apps', oscmeta["information"]["slug"], "boot.dol")):
            oscmeta["index_computed_info"]["package_type"] = "dol"
        elif os.path.exists(os.path.join(temp_dir, 'apps', oscmeta["information"]["slug"], "boot.elf")):
            oscmeta["index_computed_info"]["package_type"] = "elf"
        else:
            raise Exception("Couldn't find binary.")

        # Calculate MD5 checksum of boot.dol/boot.elf
        file_hash = hashlib.md5(pathlib.Path(temp_dir, 'apps', oscmeta["information"]["slug"],
                                             "boot." + oscmeta["index_computed_info"][
                                                 "package_type"]).read_bytes()).hexdigest()

        oscmeta["index_computed_info"]["binary_size"] = file_hash

        # time for moderation!
        log.log_status("- Checking moderation status")

        log.log_status("  - Binary checksum: " + file_hash)

        # check if binary exists in moderation table
        moderation_entry = db.session.query(ModeratedBinariesModel).filter_by(checksum=file_hash).first()
        if moderation_entry:
            if moderation_entry.status == "approved":
                log.log_status("  - Application binary is approved by moderation.")
            elif moderation_entry.status == "pending":
                log.log_status("  - Application binary is currently pending moderation.")
                zip_up_application(temp_dir, os.path.join("data", "moderation", file_hash + ".zip"))
                log.log_status("  - Updated moderation archive.")
                raise Exception("Binary requires moderation.")
            elif moderation_entry.status == "rejected":
                log.log_status("  - Application binary has been rejected by moderation.")
                zip_up_application(temp_dir, os.path.join("data", "moderation", file_hash + ".zip"))
                log.log_status("  - Updated moderation archive.")
                raise Exception("Binary rejected in moderation.")
        else:
            # Create a new entry in moderation table
            new_entry = ModeratedBinariesModel(
                checksum=file_hash,
                app_slug=oscmeta["information"]["slug"],
                status="pending",
                discovery_date=datetime.utcnow(),
                modified_date=datetime.utcnow()
            )
            db.session.add(new_entry)
            db.session.commit()

            log.log_status("  - Submitted application binary for moderation.")
            zip_up_application(temp_dir, os.path.join("data", "moderation", file_hash + ".zip"))
            log.log_status("  - Updated moderation archive.")
            raise Exception("Binary requires moderation.")

        # alright, we passed moderation!

        # remove the app directory if it exists (to ensure we don't have any old files)
        if os.path.exists(os.path.join(app_directory)):
            shutil.rmtree(os.path.join(app_directory))
        # copy the files to app directory in the index
        shutil.copytree(temp_dir, app_directory, dirs_exist_ok=True)

    log.log_status(f'- Creating ZIP file')

    # we will create a zip for homebrew browser and the API to use
    # create the zip file
    zip_up_application(app_directory, os.path.join("data", "contents", oscmeta["information"]["slug"] + ".zip"))

    # open the metadata file
    with open(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"], "meta.xml")) as f:
        # convert the metadata to JSON
        metadata = json.loads(json.dumps(xmltodict.parse(f.read())))

    # time for determining some extra information needed by API and Homebrew Browser, and adding to index
    log.log_status(f'- Retrieving Extra Information')

    # determine release date timestamp and add it to the oscmeta file
    if ("release_date" in metadata["app"]) and (metadata["app"]["release_date"] is not None):
        timestamp = metadata["app"]["release_date"]
        formats = ["%Y%m%d%H%M%S", "%Y%m%d%H%M", "%Y%m%d"]

        for fmt in formats:
            try:
                timestamp = datetime.strptime(timestamp, fmt)
                break
            except ValueError:
                continue
        else:
            # We will obtain the timestamp from the boot.dol creation date
            boot_path = os.path.join(app_directory, 'apps', oscmeta["information"]["slug"],
                                     "boot." + oscmeta["index_computed_info"]["package_type"])
            timestamp = datetime.fromtimestamp(os.path.getmtime(boot_path))
    else:
        # We will obtain the timestamp from the boot.dol creation date
        boot_path = os.path.join(app_directory, 'apps', oscmeta["information"]["slug"],
                                 "boot." + oscmeta["index_computed_info"]["package_type"])
        timestamp = datetime.fromtimestamp(os.path.getmtime(boot_path))

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
            case _:
                log.log_status("  - Unknown peripheral: " + peripheral)

    # Create subdirectories list
    oscmeta["index_computed_info"]["subdirectories"] = []
    for root, dirs, files in os.walk(os.path.join(app_directory, 'apps', oscmeta["information"]["slug"])):
        for dir in dirs:
            # we need to make sure that these strings are in the right format for the HBB.
            # example format: /apps/slug/subdirectory1/subdirectory2
            oscmeta["index_computed_info"]["subdirectories"].append(os.path.relpath(os.path.join(root, dir), os.path.join(app_directory, 'apps', oscmeta["information"]["slug"])))
            oscmeta["index_computed_info"]["subdirectories"][-1] = "/apps/" + oscmeta["information"]["slug"] + "/" + oscmeta["index_computed_info"]["subdirectories"][-1].replace("\\", "/")

    log.log_status(f'- Adding to Index')

    return metadata


def zip_up_application(source, destination):
    # Create the destination directory if it doesn't exist
    os.makedirs(os.path.dirname(destination), exist_ok=True)

    with zipfile.ZipFile(destination, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zip_ref:
        # iterate through the files starting at the app directory
        for root, dirs, files in os.walk(source):
            # iterate through the files
            for file in files:
                # get the full path of the file
                file_path = os.path.join(root, file)

                # get the relative path of the file
                relative_path = os.path.relpath(file_path, source)

                # add the file to the zip
                zip_ref.write(file_path, relative_path)
