import datetime
import os

from flask import Blueprint, redirect, url_for, request, render_template, flash, jsonify, copy_current_request_context, \
    abort, send_file
from flask_login import current_user, login_user, login_required

import config
import helpers
import index
import repository
from integrations.discord import send_webhook_message
from models import UserModel, db, SettingsModel, ModeratedBinariesModel
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


@admin.route('/logs')
@login_required
def logs():
    log_files = []
    if os.path.exists("logs"):
        for log in os.listdir("logs"):
            with open(os.path.join("logs", log), 'r') as file:
                lines = file.readlines()
                log_files.append({
                    "name": log,
                    "length": len(lines),
                    "errors": sum(1 for line in lines if '[error]' in line),
                    "created": datetime.datetime.fromtimestamp(os.stat(os.path.join("logs", log)).st_ctime).strftime(
                        '%Y-%m-%d %H:%M:%S')
                })
        log_files = sorted(log_files, key=lambda x: x["created"], reverse=True)
    return render_template('admin/logs.html', log_files=log_files)


@admin.route('/moderation')
@login_required
def moderation():
    return render_template('admin/moderation.html', mod_entries=ModeratedBinariesModel.query.all()[::-1])


@admin.route('/users')
@login_required
def users():
    return render_template('admin/users.html', users=UserModel.query.all())


@admin.route('/jobs')
@login_required
def jobs():
    return render_template('admin/jobs.html', jobs=scheduler.get_jobs())


@admin.route('/moderation/<checksum>/<action>')
@login_required
def moderation_action(checksum, action):
    moderation_entry = db.session.query(ModeratedBinariesModel).filter_by(checksum=checksum).first()
    match action:
        case "approve":
            moderation_entry.status = "approved"
            moderation_entry.moderated_by = current_user.id
            moderation_entry.modified_date = datetime.datetime.now()
            db.session.commit()
            send_webhook_message(config.DISCORD_MOD_WEBHOOK_URL, "Binary approved by moderation",
                                 f"{moderation_entry.app_slug}-{moderation_entry.checksum}\n"
                                 f"This application will be available for download starting with the next re-index.")
            flash(f'Approved \"{moderation_entry.app_slug}-{checksum}\"', 'success')
        case "reject":
            moderation_entry.status = "rejected"
            moderation_entry.moderated_by = current_user.id
            moderation_entry.modified_date = datetime.datetime.now()
            db.session.commit()
            send_webhook_message(config.DISCORD_MOD_WEBHOOK_URL, "Binary rejected by moderation",
                                 f"{moderation_entry.app_slug}-{moderation_entry.checksum}\n"
                                 f"Consider removing {moderation_entry.app_slug}.oscmeta from repository.")
            flash(f'Rejected \"{moderation_entry.app_slug}-{checksum}\"', 'danger')
        case "download":
            moderation_archive = os.path.join("data", "moderation", checksum + ".zip")
            if not os.path.exists(moderation_archive):
                abort(404)  # Archive not found

            return send_file(moderation_archive)

    return redirect(url_for('admin.moderation'))


@admin.route('/log/<file>')
@login_required
def log(file):
    log_path = os.path.join("logs", file)
    if not os.path.exists(log_path):
        abort(404)  # Log file not found

    return send_file(log_path)


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
