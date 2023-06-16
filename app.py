import shutil

import flask_migrate
import py7zr
from flask import Flask
from flask_migrate import Migrate

import config
import helpers
import index
import repository
from admin.routes import admin
from api.routes import api
from hbb.routes import hbb
from models import db, login
from scheduler import scheduler, socketio
from setup.routes import setup

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


# Register scheduler jobs
scheduler.add_job(pull_repo_and_update_index, 'interval', hours=24, replace_existing=True, id='update', args=[])
scheduler.start()


# Register additional unpack formats
shutil.register_unpack_format("7z", [".7z"], py7zr.unpack_7zarchive)


# before first request
with app.app_context():
    db.create_all()
    flask_migrate.upgrade("migrations")
    index.initialize()

app.jinja_env.globals.update(index=index.get)
app.jinja_env.globals.update(notifications=helpers.notifications)


@app.route('/')
def hello_world():  # put application's code here
    return 'Open Shop Channel Repository Manager'


if __name__ == '__main__':
    app.run()
