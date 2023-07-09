import requests

import config
from sources.base_source_downloader import BaseSourceDownloader


class SourceDownloader(BaseSourceDownloader):
    def __init__(self, oscmeta, temp_dir, log):
        super().__init__(oscmeta, temp_dir, log)
        self.game = None
        self.uploads = None

    def fetch_source_information(self):
        self.game = requests.get(
            f"https://{self.oscmeta['source']['creator']}.itch.io/{self.oscmeta['source']['game']}/data.json").json()
        self.log.log_status(f"  - Successfully found itch.io game \"{self.game['title']}\"")

        self.log.log_status(f"  - Authenticating with itch.io")
        self.uploads = requests.get(f"https://itch.io/api/1/{config.ITCHIO_KEY}/game/{self.game['id']}/uploads").json()

    def process_files(self):
        # find the specified archive file and download it
        found = False
        for upload in self.uploads["uploads"]:
            if upload["display_name"] == self.oscmeta["source"]["upload"]:
                found = True
                self.log.log_status(f"  - Found upload with ID {upload['id']}")
                download = requests.get(
                    f"https://itch.io/api/1/{config.ITCHIO_KEY}/upload/{upload['id']}/download").json()

                # download the archive
                with open(self.archive_path, "wb") as f:
                    f.write(requests.get(download['url']).content)
                self.log.log_status(f"  - Downloaded upload \"{upload['filename']}\"")
        if not found:
            raise Exception("Could not find itch.io upload")
