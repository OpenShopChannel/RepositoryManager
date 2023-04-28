from flask import Blueprint, redirect, url_for, request, render_template, flash, jsonify, copy_current_request_context
from flask_login import current_user, login_user, login_required

import config
import helpers
import index
import repository
from models import UserModel, db, SettingsModel, StatusLogsModel

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


@admin.route('/log')
@login_required
def log():
    return render_template('admin/log.html')


@admin.route('/log/json')
@login_required
def log_json():
    logs = []
    for log in StatusLogsModel.query.all():
        logs.append({
            'id': log.id,
            'status': log.status,
            'message': log.message,
            'timestamp': log.timestamp
        })
    return jsonify(logs)


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
        response = redirect(url_for('admin.log'))

        @response.call_on_close
        @copy_current_request_context
        def on_close():
            index.update()

        flash('Started index update', 'info')
        return response
    elif action == 'test_log':
        helpers.log_status("This is a test log")
    return redirect(url_for('admin.debug'))


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
