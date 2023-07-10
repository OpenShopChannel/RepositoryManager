import eventlet
import requests

import config
from sources.base_source_downloader import BaseSourceDownloader


class SourceDownloader(BaseSourceDownloader):
    name = "URL Source Downloader"
    description = "Downloads files from a URL source."

    def __init__(self, oscmeta, temp_dir, log):
        super().__init__(oscmeta, temp_dir, log)
        self.headers = None
        self.url = None

    def fetch_source_information(self):
        self.url = self.oscmeta["source"]["location"]

        # determine user-agent
        # check if user-agent is set
        if "user-agent" in self.oscmeta["source"]:
            # check if this user-agent string is included in the config.SECRET_USER_AGENTS dictionary
            if self.oscmeta["source"]["user-agent"] in config.SECRET_USER_AGENTS:
                # if it is, use the actual user-agent string
                user_agent = config.SECRET_USER_AGENTS[self.oscmeta["source"]["user-agent"]]
            else:
                user_agent = self.oscmeta["source"]["user-agent"]
        else:
            user_agent = config.USER_AGENT

        self.headers = {"User-Agent": user_agent}

    def process_files(self):
        # download the archive
        with open(self.archive_path, "wb") as f:
            with eventlet.Timeout(config.URL_DOWNLOAD_TIMEOUT):
                if "user-agent" in self.oscmeta["source"]:
                    self.log.log_status(f'  - Using custom user-agent: {self.oscmeta["source"]["user-agent"]}')
                f.write(requests.get(self.url, headers=self.headers).content)
