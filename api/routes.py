from flask import Blueprint, jsonify

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
                "binary": content["index_extra_info"]["binary_size"],
                "icon": content["index_extra_info"]["icon_size"],
                "zip_compressed": content["index_extra_info"]["compressed_size"],
                "zip_uncompressed": content["index_extra_info"]["uncompressed_size"]
            },
            "name": content["name"],
            "package_type": content["index_extra_info"]["package_type"],
            "peripherals": content["peripherals"],
            "release_date": content["index_extra_info"]["release_date"],
            "slug": content["slug"],
            "version": content["metaxml"]["app"]["version"]
        })

    return jsonify(contents)


@api.after_request
def after_request(response):
    header = response.headers
    header['Access-Control-Allow-Origin'] = '*'
    return response
