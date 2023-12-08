import os

from flask import Blueprint, request, abort, send_file, jsonify
from urllib.parse import urlparse
from werkzeug.utils import secure_filename

import config
import helpers
import index
from hbb.normalize import Normalize

hbb = Blueprint('hbb', __name__, template_folder='templates')


# Some of the code for HBB support was originally written by Spotlight for Open Shop Channel's "Danbo Shop Server".
@hbb.route('/hbb/homebrew_browser/listv036.txt')
@hbb.route('/hbb/listv036.txt')
def apps_list():
    """
    Returns a list of all the apps in the homebrew browser list format.
    """

    # order by category, so that all apps in the same category come one after the other
    apps = []
    for category in ["demos", "emulators", "games", "media", "utilities"]:
        for app in index.get()["contents"]:
            if app["information"]["category"] == category:
                apps.append(app)

    # Formulate our response.
    content = Normalize()
    content.add_line("Homebrew 2092896 v0.3.9e | - Updated with latest libogc which should correct network issues some users are experiencing")

    current_category = apps[0]["information"]["category"]
    for app in apps:
        if app["information"]["slug"] != "homebrew_browser":
            # append "=Category=" if reached last item in category
            if current_category != app["information"]["category"]:
                content.add_line(f"={current_category.capitalize()}=")
                current_category = app["information"]["category"]

            # if "writes_to_nand" flag is specified, prepend warning to long description
            flags = app["information"].get("flags", [])
            long_description_prefix = ""
            if "writes_to_nand" in flags:
                long_description_prefix += "[CAUTION! Writes to NAND!] "
            # and if "wii_only" flag is specified, prepend warning to long description
            if "wii_only" in flags:
                long_description_prefix += "[CAUTION! Compatible just with the Wii!] "
            # and if "vwii_only" flag is specified, prepend warning to long description
            if "vwii_only" in flags:
                long_description_prefix += "[CAUTION! Compatible just with the vWii!] "
            # and if "wii_mini_only" flag is specified, prepend warning to long description
            if "wii_mini_only" in flags:
                long_description_prefix += "[CAUTION! Compatible just with the mini!] "

            # The following app metadata should all be on one line.
            # -----
            # Internal name
            content.add(app["information"]["slug"])
            # Date added to repo
            content.add(app["index_computed_info"]["release_date"])
            # Size of icon.png
            content.add(app["index_computed_info"]["icon_size"])
            # Size of package
            content.add(app["index_computed_info"]["binary_size"])
            # Type of package (dol/elf/etc)
            content.add(app["index_computed_info"]["package_type"])
            # Size of total zip
            content.add(app["index_computed_info"]["compressed_size"])
            # Download count
            # TODO: support download count
            content.add(0)
            # Ratings count
            content.add(0)
            # Peripherals
            content.add(app["index_computed_info"]["peripherals"])

            # Folders to create
            subdirectories = ""
            for directory in app["index_computed_info"]["subdirectories"]:
                subdirectories += f"{directory};"
            content.add(subdirectories[:-1])

            # Folders to not delete
            content.add(".")
            # Files to not extract
            content.add_line(".")
            # -----

            # Name
            content.add_line(app["metaxml"]["app"]["name"])
            # Author
            if "coder" in app["metaxml"]["app"]:
                content.add_line(app["metaxml"]["app"]["coder"])
            else:
                content.add_line(app["information"]["author"])
            # Version
            content.add_line(app["metaxml"]["app"]["version"])
            # Extracted Size
            content.add_line(str(app["index_computed_info"]["uncompressed_size"]))
            # Short Description
            if "short_description" in app["metaxml"]["app"]:
                content.add_line(app["metaxml"]["app"]["short_description"])
            else:
                content.add_line("No description provided.")

            # Long Description
            if ("long_description" in app["metaxml"]["app"]) and (app["metaxml"]["app"]["long_description"] is not None):
                if len(long_description_prefix + app["metaxml"]["app"]["long_description"]) > 128:
                    content.add_line((long_description_prefix + app["metaxml"]["app"]["long_description"]).replace('\n', ' ')[:128] + "...")
                else:
                    content.add_line((long_description_prefix + app["metaxml"]["app"]["long_description"]).replace('\n', ' '))
            else:
                if "short_description" in app["metaxml"]["app"]:
                    content.add_line(long_description_prefix + app["metaxml"]["app"]["short_description"])
                else:
                    content.add_line(long_description_prefix + "No description provided.")

    content.add_line(f"={current_category.capitalize()}=")

    return content.response


# Stub
@hbb.route('/hbb/get_rating.php')
def get_rating():
    """
    Returns the rating of an app for a user.
    """
    app_name = request.args.get("name")
    esid = request.args.get("esid")
    return "5"


# Stub
@hbb.route('/hbb/update_rating.php')
def update_rating():
    """
    Updates the rating of an app for a user.
    """
    app_name = request.args.get("name")
    esid = request.args.get("esid")
    rating = request.args.get("rating")
    return "5"


@hbb.route('/hbb/apps_check_new.php')
def apps_check_new():
    """
    Check for new apps
    """
    return "0 0"


@hbb.get("/hbb/<slug>/icon.png")
@hbb.get("/hbb/<slug>.png")
def get_content_icon(slug):
    slug = secure_filename(slug)
    icon_path = os.path.join(helpers.app_index_directory_location(slug), "apps", slug, "icon.png")
    if os.path.exists(icon_path):
        return send_file(icon_path, download_name="icon.png")
    else:
        abort(404)


@hbb.get("/hbb/<slug>/<_slug>.zip")
def get_content_zip(slug, _slug):
    slug = secure_filename(slug)
    zip_path = os.path.join("data", "contents", slug + ".zip")
    if os.path.exists(zip_path):
        return send_file(zip_path, download_name=slug + ".zip")
    else:
        abort(404)


@hbb.get("/unzipped_apps/<slug>/apps/<_slug>/meta.xml")
def get_content_xml(slug, _slug):
    slug = secure_filename(slug)
    xml_path = os.path.join(helpers.app_index_directory_location(slug), "apps", slug, "meta.xml")
    if os.path.exists(xml_path):
        response = send_file(xml_path, download_name="meta.xml")
        response.headers['Access-Control-Allow-Origin'] = '*'
        return response
    else:
        abort(404)


@hbb.get("/unzipped_apps/<slug>/apps/<_slug>/boot.<binary_type>")
def get_content_binary(slug, _slug, binary_type):
    slug = secure_filename(slug)
    binary_type = secure_filename(binary_type)
    if binary_type in ["dol", "elf"]:
        binary_path = os.path.join(helpers.app_index_directory_location(slug), "apps", slug, "boot." + binary_type)
        if os.path.exists(binary_path):
            return send_file(binary_path, download_name="boot." + binary_type)
    abort(404)


@hbb.get("/hbb/homebrew_browser/temp_files.zip")
def get_icons_zip():
    return send_file(os.path.join("data", "icons.zip"), download_name="icons.zip")


# Stub
@hbb.route('/hbb/hbb_download.php')
@hbb.route('/hbb_download.php')
def register_download():
    """
    Registers that an app was downloaded.
    """
    app_name = request.args.get("name")
    return ""


@hbb.route('/hbb/repo_list.txt')
def repo_list():
    """
        Returns a list of all the repos in the homebrew browser repos list format.
    """
    if config.SERVE_REPO_LIST_FILE:
        #repos: [ReposModel] = ReposModel.query.all()

        # Add our version, "1", to the response.
        content = Normalize()
        content.add_line("1")

        #for repo in repos:
        # Repo's name
        content.add_line(index.get()["repository"]["name"])
        # Repo's host
        content.add_line(urlparse(request.base_url).hostname)
        # Repo's contents
        content.add_line("/hbb/homebrew_browser/listv036.txt")
        # Repo's subdirectory
        content.add_line("/hbb/")

        return content.response
    else:
        return abort(404)


@hbb.route('/metadata.json')
def metadata_json():
    """
        This is a legacy endpoint used by some old versions of OSCDL
    """
    metadata = {}
    for app in index.get()["contents"]:
        metadata[app["information"]["slug"]] = [app["information"]["category"].capitalize(),
                                                app['index_computed_info']['peripherals'],
                                                "", "", "", ""]
    return jsonify(dict(sorted(metadata.items())))
