from flask import Flask

import config
import index
from admin.routes import admin
from api.routes import api
from hbb.routes import hbb
from models import db, login
from setup.routes import setup

app = Flask(__name__)


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


# before first request
with app.app_context():
    db.create_all()
    index.initialize()

app.jinja_env.globals.update(index=index.get)


@app.route('/')
def hello_world():  # put application's code here
    return 'Open Shop Channel Repository Manager'


if __name__ == '__main__':
    app.run()
