import os

from flask import Blueprint, jsonify, send_file, url_for

import helpers
import index

api = Blueprint('api', __name__, template_folder='templates', url_prefix='/api')


@api.get("/v3/contents")
def get_contents():
    contents = []
    for content in index.get()["contents"]:
        # Ensure all required fields are present
        if "long_description" not in content["metaxml"]["app"]:
            content["metaxml"]["app"]["long_description"] = "No description provided."

        # Append to contents
        contents.append({
            "author": content["author"],
            "category": content["category"],
            "description": {
                "long": content["metaxml"]["app"]["long_description"],
                "short": content["metaxml"]["app"]["short_description"]
            },
            "file_size": {
                "binary": content["index_computed_info"]["binary_size"],
                "icon": content["index_computed_info"]["icon_size"],
                "zip_compressed": content["index_computed_info"]["compressed_size"],
                "zip_uncompressed": content["index_computed_info"]["uncompressed_size"]
            },
            "name": content["name"],
            "package_type": content["index_computed_info"]["package_type"],
            "peripherals": content["peripherals"],
            "release_date": content["index_computed_info"]["release_date"],
            "slug": content["slug"],
            "subdirectories": content["index_computed_info"]["subdirectories"],
            "url": {
                "icon": url_for('api.get_content_icon', slug=content["slug"], _external=True),
                "zip": url_for('api.get_content_zip', slug=content["slug"], _external=True),
            },
            "version": content["metaxml"]["app"]["version"]
        })

    return jsonify(contents)


@api.get("/v3/contents/<slug>/icon.png")
def get_content_icon(slug):
    icon_path = os.path.join(helpers.app_index_directory_location(slug), "apps", slug, "icon.png")
    return send_file(icon_path, download_name="icon.png")


@api.get("/v3/contents/<slug>/<_slug>.zip")
def get_content_zip(slug, _slug):
    zip_path = os.path.join("data", "contents", slug + ".zip")
    return send_file(zip_path, download_name=slug + ".zip")


@api.after_request
def after_request(response):
    header = response.headers
    header['Access-Control-Allow-Origin'] = '*'
    return response
