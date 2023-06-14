import datetime

from flask import Blueprint, redirect, url_for, request, render_template, flash, jsonify, copy_current_request_context
from flask_login import current_user, login_user, login_required

import config
import helpers
import index
import repository
from models import UserModel, db, SettingsModel
from scheduler import scheduler

admin = Blueprint('admin', __name__, template_folder='templates', url_prefix='/admin')

@admin.route('/')
@login_required
def home():
    return render_template('admin/home.html')


@admin.route('/apps')
@login_required
def apps():
    return render_template('admin/apps.html')


@admin.route('/debug')
@login_required
def debug():
    return render_template('admin/debug.html')


@admin.route('/status')
@login_required
def status():
    return render_template('admin/task_status.html')


@admin.route('/debug/<action>')
@login_required
def debug_action(action):
    if action == 'init_repo':
        repository.initialize(helpers.get_settings()['git_url'])
        flash('Successfully initialized repository', 'success')
    elif action == 'pull_repo':
        repository.pull()
        flash('Successfully pulled repository', 'success')
    elif action == 'update_index':
        index.update()
        flash('Successfully updated index', 'success')
    return redirect(url_for('admin.debug'))


@admin.route('/action/<action>')
@login_required
def action(action):
    match action:
        case 'update':
            scheduler.get_job(job_id="update").modify(next_run_time=datetime.datetime.now())
            flash('Scheduled immediate index update.', 'info')
            return redirect(url_for('admin.status'))
    return redirect(url_for('admin.home'))


@admin.route('/settings', methods=['GET', 'POST'])
@login_required
def settings():
    settings = {}
    for setting in SettingsModel.query.all():
        settings[setting.key] = setting.value
    if request.method == 'POST':
        # update settings in database
        for key in settings:
            if key in request.form:
                setting = SettingsModel.query.filter_by(key=key).first()
                # check if changed
                if setting.value != request.form[key]:
                    setting.value = request.form[key]
                    db.session.commit()
                    flash(f'Successfully updated setting \"{setting.key}\"', 'success')
        return redirect(url_for('admin.settings'))
    return render_template('admin/settings.html', settings=settings)


@admin.route('/login', methods=['POST', 'GET'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('admin.home'))

    if request.method == 'POST':
        email = request.form['email']
        user = UserModel.query.filter_by(email=email).first()
        remember = False

        if request.form['remember'] != "No":
            remember = True

        if user is not None and user.check_password(request.form['password']):
            login_user(user, remember=remember)
            return redirect(url_for('admin.home'))

    return render_template('login.html')


@admin.route('/register', methods=['POST', 'GET'])
def register():
    if config.allow_admin_register:
        if current_user.is_authenticated:
            return redirect(url_for('admin.home'))

        if request.method == 'POST':
            email = request.form['email']
            username = request.form['username']
            password = request.form['password']
            if UserModel.query.filter_by(email=email).first():
                return 'Email already in use'
            user = UserModel(email=email, username=username)
            user.set_password(password)
            db.session.add(user)
            db.session.commit()
            return redirect(url_for('admin.login'))
        return render_template('register.html')
    else:
        return 'Registration is disabled'
