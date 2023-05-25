from apscheduler.jobstores.sqlalchemy import SQLAlchemyJobStore
from apscheduler.schedulers.background import BackgroundScheduler
from blinker import signal
from flask_login import current_user
from flask_socketio import SocketIO, disconnect

import config

socketio = SocketIO()

# signal to communicate between background thread and main thread
log_signal = signal('log')


@socketio.on('connect')
def handle_connect():
    if not current_user.is_authenticated:
        # Refuse connection if user is not authenticated
        disconnect()


# signal handler for log_signal to update the log over websockets
@log_signal.connect
def send_log_update(log_line):
    socketio.emit('logUpdate', log_line)

# configure APScheduler
jobstores = {
    'default': SQLAlchemyJobStore(url=config.SCHEDULER_DATABASE_URI)
}
job_defaults = {
    'coalesce': False,
    'max_instances': 1
}

scheduler = BackgroundScheduler(job_defaults=job_defaults, jobstores=jobstores)
