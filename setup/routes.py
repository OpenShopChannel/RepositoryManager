from flask import Blueprint, render_template, redirect, url_for
from flask_wtf import FlaskForm
from wtforms import StringField, SubmitField
from wtforms.validators import DataRequired

import helpers
from models import SettingsModel, UserModel, db

setup = Blueprint('setup', __name__, template_folder='templates', url_prefix='/setup')


class SetupForm(FlaskForm):
    # website configuration
    git_url = StringField('Repository Source (Git)', validators=[DataRequired()])

    # initial admin user
    admin_email = StringField('Email', validators=[DataRequired()])
    admin_username = StringField('Username', validators=[DataRequired()])
    admin_password = StringField('Password', validators=[DataRequired()], render_kw={'type': 'password'})
    submit = SubmitField('Finish')


@setup.route('/', methods=['GET', 'POST'])
def configure():
    if SettingsModel.query.filter_by(key='setup_complete').first():
        return redirect(url_for('admin.home'))
    else:
        form = SetupForm()
        if form.validate_on_submit():
            helpers.update_setting("setup_complete", "true")
            helpers.update_setting("git_url", form.git_url.data)

            user = UserModel(email=form.admin_email.data, username=form.admin_username.data, role="Administrator")
            user.set_password(form.admin_password.data)
            db.session.add(user)
            db.session.commit()
            return redirect(url_for('admin.home'))
        return render_template('setup.html', form=form)
