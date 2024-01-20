import random

from datetime import datetime

from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin, LoginManager
from werkzeug.security import generate_password_hash, check_password_hash


login = LoginManager()
db = SQLAlchemy()


class UserModel(UserMixin, db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(80), unique=True)
    username = db.Column(db.String(20), unique=True)
    password_hash = db.Column(db.String())
    role = db.Column(db.String(), server_default="Guest", nullable=False)

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)


@login.user_loader
def load_user(id):
    return UserModel.query.get(int(id))


class SettingsModel(UserMixin, db.Model):
    __tablename__ = 'settings'

    key = db.Column(db.String, primary_key=True)
    value = db.Column(db.String)


class ModeratedBinariesModel(UserMixin, db.Model):
    __tablename__ = 'moderated_binaries'

    checksum = db.Column(db.String, primary_key=True)
    app_slug = db.Column(db.String, nullable=False)
    status = db.Column(db.String, default="pending", nullable=False)
    discovery_date = db.Column(db.DateTime, nullable=False, default=datetime.utcnow())
    modified_date = db.Column(db.DateTime, nullable=False, default=datetime.utcnow())
    moderated_by = db.Column(db.Integer, db.ForeignKey('users.id'))


class PersistentAppInformationModel(UserMixin, db.Model):
    __tablename__ = 'persistent_app_information'

    app_slug = db.Column(db.String, primary_key=True)
    title_id = db.Column(db.String)
    version = db.Column(db.Integer, server_default="1", nullable=False)


class AppOfTheDay:
    package_of_the_day = None

    def set_package_of_the_day(self, index):
        # some quality assurance, some of the apps have covid
        while True:
            package = random.choice(index.get()["contents"])
            # they do not have covid, just did not specify a description
            if package["metaxml"]["app"].get("short_description", "") == "":
                continue
            # they do not have covid, just simply a demo and we can't have that
            if package["information"]["category"] == "demos":
                continue
            # they do not have covid, but don't support wii remotes
            if "Wii Remote" not in package["information"]["peripherals"]:
                continue
            # they do not have covid, but the developer is Danbo
            if package["information"]["author"] == "Danbo":
                continue
            break
        self.package_of_the_day = package