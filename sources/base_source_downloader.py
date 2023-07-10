import os

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
