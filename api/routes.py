import os

from flask import Blueprint, jsonify, send_file, url_for, abort
from werkzeug.utils import secure_filename

import helpers
import index

api = Blueprint('api', __name__, template_folder='templates', url_prefix='/api')


@api.get("/v3/contents")
def get_contents():
    contents = []
    for content in index.get()["contents"]:
        # Append to contents
        contents.append({
            "author": content["metaxml"]["app"].get("coder", content["information"]["author"]),
            "category": content["information"]["category"],
            "description": {
                "long": content["metaxml"]["app"].get("long_description", "No description provided."),
                "short": content["metaxml"]["app"].get("short_description", "No description provided.")
            },
            "file_size": {
                "binary": content["index_computed_info"]["binary_size"],
                "icon": content["index_computed_info"]["icon_size"],
                "zip_compressed": content["index_computed_info"]["compressed_size"],
                "zip_uncompressed": content["index_computed_info"]["uncompressed_size"]
            },
            "name": content["metaxml"]["app"].get("name", content["information"]["name"]),
            "package_type": content["index_computed_info"]["package_type"],
            "peripherals": content["information"]["peripherals"],
            "release_date": content["index_computed_info"].get("release_date", 0),
            "shop": {
                "title_id": content["index_computed_info"].get("title_id"),
                "title_version": content["index_computed_info"].get("title_version")
            },
            "slug": content["information"]["slug"],
            "subdirectories": content["index_computed_info"]["subdirectories"],
            "url": {
                "icon": url_for('api.get_content_icon', slug=content["information"]["slug"], _external=True),
                "zip": url_for('api.get_content_zip', slug=content["information"]["slug"], _slug=content["information"]["slug"], _external=True),
            },
            "version": content["metaxml"]["app"].get("version", "Unknown")
        })

    return jsonify(contents)


@api.get("/v3/information")
def get_information():
    repository = index.get()["repository"]

    information = {
        "name": repository["name"],
        "provider": repository["provider"],
        "description": repository["description"],
        "available_categories": index.get()["categories"],
        "available_apps_count": len(index.get()["contents"]),
        "git_url": helpers.get_settings()['git_url']
    }

    return jsonify(information)


@api.get("/v3/contents/<slug>/icon.png")
def get_content_icon(slug):
    slug = secure_filename(slug)
    icon_path = os.path.join(helpers.app_index_directory_location(slug), "apps", slug, "icon.png")
    if os.path.exists(icon_path):
        return send_file(icon_path, download_name="icon.png")
    else:
        abort(404)


@api.get("/v3/contents/<slug>/<_slug>.zip")
def get_content_zip(slug, _slug):
    slug = secure_filename(slug)
    zip_path = os.path.join("data", "contents", slug + ".zip")
    if os.path.exists(zip_path):
        return send_file(zip_path, download_name=slug + ".zip")
    else:
        abort(404)


@api.after_request
def after_request(response):
    header = response.headers
    header['Access-Control-Allow-Origin'] = '*'
    return response
