import os
import shutil

import flask_migrate
import py7zr
from flask import Flask, url_for, render_template
from flask_migrate import Migrate
import sentry_sdk
from sentry_sdk.integrations.flask import FlaskIntegration

import config
import helpers
import index
import repository
from admin.routes import admin
from api.routes import api, featured_app
from hbb.routes import hbb
from admin.roles import has_access
from models import db, login
from scheduler import scheduler, socketio
from setup.routes import setup
from models import SettingsModel

if config.SENTRY_DSN:
    sentry_sdk.init(
        dsn=config.SENTRY_DSN,
        integrations=[
            FlaskIntegration(),
        ],
        traces_sample_rate=config.SENTRY_TRACES_SAMPLE_RATE
    )

app = Flask(__name__)
socketio.init_app(app, cors_allowed_origins=config.SOCKETIO_CORS_ALLOWED_ORIGINS)


# Configuration
app.config['JSONIFY_PRETTYPRINT_REGULAR'] = True

# Register blueprints
app.register_blueprint(admin)
app.register_blueprint(api)
app.register_blueprint(hbb)
app.register_blueprint(setup)

# Configure database
app.config['SQLALCHEMY_DATABASE_URI'] = config.SQLALCHEMY_DATABASE_URI
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.secret_key = config.secret_key
db.init_app(app)
login.init_app(app)
login.login_view = 'admin.login'
migrate = Migrate(app, db)


def pull_repo_and_update_index():
    with app.app_context():
        repository.pull()
        index.update()


def refresh_app_of_the_day():
    with app.app_context():
        featured_app.set_package_of_the_day(index)


# Register additional unpack formats
shutil.register_unpack_format("7z", [".7z"], py7zr.unpack_7zarchive)


# before first request
with app.app_context():
    # Register scheduler jobs
    scheduler.add_job(pull_repo_and_update_index, 'interval', hours=6, replace_existing=True, id='update', args=[])
    # Schedule app of the day for refresh once per day at 2:00
    scheduler.add_job(refresh_app_of_the_day, 'cron', hour='2', minute='00', replace_existing=True, id='app-of-the-day')
    scheduler.start()

    # Prepare flask app
    flask_migrate.upgrade("migrations")

    # check if an index file exists
    if not os.path.exists(os.path.join('data', 'index.json')):
        index.initialize()

app.jinja_env.globals.update(index=index.get)
app.jinja_env.globals.update(notifications=helpers.notifications)
app.jinja_env.globals.update(has_access=has_access)

@app.route('/')
def hello_world():
    if not SettingsModel.query.filter_by(key='setup_complete').first():
        return """
        This RepositoryManager instance has not been installed. 
        <a href='/setup'>Click here to go to the setup.</a>
        """

    app_count = len(index.get()["contents"])
    repository_name = index.get()["repository"]["name"]
    repository_provider = index.get()["repository"]["provider"]
    git_url = helpers.get_settings()["git_url"]
    api_url = url_for("hello_world", _external=True)

    return render_template('hello_world.html', app_count=app_count,
                           repository_name=repository_name,
                           repository_provider=repository_provider,
                           git_url=git_url, api_url=api_url)


if __name__ == '__main__':
    app.run()
