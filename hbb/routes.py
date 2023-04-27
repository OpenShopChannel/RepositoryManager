import zipfile
from io import BytesIO

from flask import Blueprint, request, abort, send_file

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
            if app["category"] == category:
                apps.append(app)

    # Formulate our response.
    content = Normalize()
    content.add_line("Homebrew 2092896 v0.3.9e | - Updated with latest libogc which should correct network issues some users are experiencing")

    current_category = apps[0]["category"]
    for app in apps:
        # append "=Category=" if reached last item in category
        if current_category != app["category"]:
            content.add_line(f"={current_category.capitalize()}=")
            current_category = app["category"]

        # The following app metadata should all be on one line.
        # -----
        # Internal name
        content.add(app["slug"])
        # Date added to repo
        content.add(app["index_extra_info"]["release_date"])
        # Size of icon.png
        content.add(app["index_extra_info"]["icon_size"])
        # Size of package
        content.add(app["index_extra_info"]["binary_size"])
        # Type of package (dol/elf/etc)
        content.add(app["index_extra_info"]["package_type"])
        # Size of total zip
        content.add(app["index_extra_info"]["compressed_size"])
        # Download count
        # TODO: support download count
        content.add(0)
        # Ratings count
        content.add(0)
        # Peripherals
        content.add(app["index_extra_info"]["peripherals"])
        # Folders to create
        content.add("")
        # Folders to not delete
        content.add(".")
        # Files to not extract
        content.add_line(".")
        # -----

        # Name
        content.add_line(app["metaxml"]["app"]["name"])
        # Author
        content.add_line(app["metaxml"]["app"]["coder"])
        # Version
        content.add_line(app["metaxml"]["app"]["version"])
        # Extracted Size
        content.add_line(str(app["index_extra_info"]["uncompressed_size"]))
        # Short Description
        content.add_line(app["metaxml"]["app"]["short_description"])
        # Long Description
        try:
            content.add_line(app["metaxml"]["app"]["long_description"])
        except KeyError:
            content.add_line("")
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


# Stub
@hbb.route('/hbb/hbb_download.php')
def register_download():
    """
    Registers that an app was downloaded.
    """
    app_name = request.args.get("name")
    return ""


#@hbb.route('/hbb/repo_list.txt')
#def repo_list():
#    """
#    Returns a list of all the repos in the homebrew browser repos list format.
#    """
#    repos: [ReposModel] = ReposModel.query.all()
#
#    # Add our version, "1", to the response.
#    content = Normalize()
#    content.add_line("1")
#
#    for repo in repos:
#        # Repo's name
#        content.add_line(repo.name)
#        # Repo's host
#        content.add_line(repo.host)
#        # Repo's contents
#        content.add_line("/hbb/homebrew_browser/listv036.txt")
#        # Repo's subdirectory
#        content.add_line("/hbb/")
#
#    return content.response
#
#
#def get_repo_id() -> str:
#    """Returns the repository ID for the given hostname, Aborts with a 404 if not found."""
#    hostname = request.host
#    repo = ReposModel.query.where(ReposModel.host == hostname).limit(1).all()
#    if not repo:
#        abort(404)
#
#    return repo[0].id
#
#
#@hbb.route('/hbb/<slug>/<_slug>.zip')
#def hbb_zip(slug):
#    return serve_slug_file(slug, FileTypes.ZIP)
#
#
#@hbb.route('/hbb/<slug>/icon.png')
#@hbb.route('/hbb/<slug>.png')
#def hbb_icon(slug):
#    return serve_slug_icon(slug)
#
#
#@hbb.route('/hbb/<slug>/meta.xml')
#def hbb_meta(slug):
#    return serve_slug_file(slug, FileTypes.META)
#
#
#@hbb.route('/hbb/homebrew_browser/temp_files.zip')
#def hbb_icon_zip():
#    # TODO: This should be done upon catalogue update,
#    # instead of dynamically, to avoid server strain.
#    # For development purposes, we do not.
#
#    # Create a zip file within memory.
#    memory_buffer = BytesIO()
#    zip_file = zipfile.ZipFile(memory_buffer, 'w')
#
#    # Zip all icons within our icon directory.
#    repo: [AppsModel] = AppsModel.query.all()
#    for app in repo:
#        icon_path = file_path(app.meta_data.file_uuid, FileTypes.ICON)
#        file_name = f"{app.slug}.png"
#        zip_file.write(icon_path, arcname=file_name)
#
#    # Rewind to the start of the buffer so that it can be read in full.
#    zip_file.close()
#    memory_buffer.seek(0)
#
#    return send_file(memory_buffer, mimetype='application/zip')
