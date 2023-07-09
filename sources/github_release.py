import fnmatch
import os

import requests

import config
from sources.base_source_downloader import BaseSourceDownloader


class SourceDownloader(BaseSourceDownloader):
    def __init__(self, oscmeta, temp_dir):
        super().__init__(oscmeta, temp_dir)
        self.response = None
        self.url = None
        self.headers = None

    def fetch_source_information(self):
        # check for token
        if config.GITHUB_TOKEN != "":
            # we have a token, let's use it
            self.log.log_status(f'  - Authenticating with GitHub')
            self.headers = {"Authorization": f"token {config.GITHUB_TOKEN}"}
        else:
            self.log.log_status(f'  - No valid GitHub token found, using unauthenticated requests. '
                                f'Please configure a token in config.py')
            self.headers = {}

        # fetch the latest release
        self.url = f'https://api.github.com/repos/{self.oscmeta["source"]["repository"]}/releases/latest'
        self.response = requests.get(self.url, headers=self.headers)

    def process_files(self):
        if self.response.status_code == 200:
            self.log.log_status(f'  - Successfully fetched latest release')
            assets = self.response.json()["assets"]

            # check if additional files are specified
            if "additional_files" in self.oscmeta["source"]:
                files = [self.oscmeta["source"]["file"]] + self.oscmeta["source"]["additional_files"]
            else:
                files = [self.oscmeta["source"]["file"]]

            for file in files:
                for asset in assets:
                    # check if asset name matches pattern
                    if fnmatch.fnmatch(asset["name"], file):
                        self.log.log_status(f'  - Found asset {asset["name"]}')
                        # download the asset
                        url = asset["browser_download_url"]

                        # check if archive
                        if file == self.oscmeta["source"]["file"]:
                            # download the file
                            with open(self.archive_path, "wb") as f:
                                f.write(requests.get(url).content)
                        else:
                            # download the file
                            with open(os.path.join(self.temp_dir, file), "wb") as f:
                                f.write(requests.get(url).content)

                        self.log.log_status(f'  - Downloaded asset {asset["name"]}')
                        break
