import os

import requests

import logger


class BaseSourceDownloader:
    name = "Unknown Source Downloader"
    description = "Source Downloaders download files from source."

    def __init__(self, oscmeta, temp_dir, log=logger.Log("source_downloader")):
        self.oscmeta = oscmeta
        self.temp_dir = temp_dir
        self.log = log
        self.archive_path = os.path.join(temp_dir, self.oscmeta["information"]["slug"] + ".package")

    def download_files(self):
        # Common code for all source types
        self.log.log_status(f'- Fetching source information')
        self.fetch_source_information()

        self.log.log_status(f'- Processing obtained files')
        self.process_files()

    def fetch_source_information(self):
        # Common logic to fetch source information
        pass

    def process_files(self):
        # Common logic to process files
        pass

    def download_from_url_to_file(self, url, destination_path):
        # download the file
        with open(destination_path, "wb") as f:
            downloaded_file = requests.get(url)
            if downloaded_file.status_code == 200:
                f.write(downloaded_file.content)
            else:
                raise Exception(f"Status code {downloaded_file.status_code} during \"{self.name}\" download.")
