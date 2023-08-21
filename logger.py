import datetime
import os

import config
from integrations.discord import send_webhook_message
from scheduler import log_signal


class Log:
    def __init__(self, name="log"):
        self.log_lines = {}
        self.timestamp = datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        self.name = name

    def __del__(self):
        self.save_log()

    def get_filename(self):
        return f"{self.name}-{self.timestamp}.log"

    def log_status(self, message, status='info', silent=False):
        try:
            timestamp = datetime.datetime.now()
            log_entry = f"[{status}] {message}"
            self.log_lines[timestamp] = log_entry
            print(log_entry)

            line = {
                "message": message,
                "status": status
            }

            if not silent:
                log_signal.send(line)
        except Exception as e:
            print(f"Failed writing a log line: {e}")

    def save_log(self):
        try:
            directory = 'logs'
            if not os.path.exists(directory):
                os.makedirs(directory)

            filepath = os.path.join(directory, self.get_filename())
            with open(filepath, 'w') as file:
                for timestamp, log_entry in self.log_lines.items():
                    file.write(f"{timestamp}: {log_entry}\n")

            self.log_status("Written log file to: " + self.get_filename())
            send_webhook_message(config.DISCORD_INFO_WEBHOOK_URL, "Produced new log file", self.get_filename())
        except NameError:
            # Python is likely shutting down. We will abort log saving in this case.
            pass
