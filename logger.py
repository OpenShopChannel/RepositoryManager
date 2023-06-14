import datetime
import os

from scheduler import log_signal


class Log:
    def __init__(self, name="log"):
        self.log_lines = {}
        self.timestamp = datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        self.name = name

    def __del__(self):
        self.save_log()

    def log_status(self, message, status='info'):
        timestamp = datetime.datetime.now()
        log_entry = f"[{status}] {message}"
        self.log_lines[timestamp] = log_entry
        print(log_entry)

        log_signal.send(message)

    def save_log(self):
        directory = 'logs'
        if not os.path.exists(directory):
            os.makedirs(directory)

        filename = f"{self.name}-{self.timestamp}.log"
        filepath = os.path.join(directory, filename)
        with open(filepath, 'w') as file:
            for timestamp, log_entry in self.log_lines.items():
                file.write(f"{timestamp}: {log_entry}\n")

        self.log_status("Saved copy of this log to file: " + filename)
