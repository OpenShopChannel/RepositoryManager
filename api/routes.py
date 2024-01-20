import os

from flask import Blueprint, jsonify, send_file, abort
from models import AppOfTheDay
from werkzeug.utils import secure_filename

import helpers
import index

api = Blueprint('api', __name__, template_folder='templates', url_prefix='/api')

featured_app = AppOfTheDay()
featured_app.set_package_of_the_day(index)


@api.get("/v3/contents")
def get_contents():
    contents = []
    for content in index.get()["contents"]:
        # Append to contents
        contents.append(helpers.describe_app(content))

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


@api.get("/v3/featured-app")
def get_featured_app():
    app = featured_app.package_of_the_day
    return jsonify(helpers.describe_app(app))


@api.after_request
def after_request(response):
    header = response.headers
    header['Access-Control-Allow-Origin'] = '*'
    return response
