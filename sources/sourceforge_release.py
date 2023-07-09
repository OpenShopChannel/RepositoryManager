import requests

from sources.base_source_downloader import BaseSourceDownloader


class SourceDownloader(BaseSourceDownloader):
    def __init__(self, oscmeta, temp_dir, log):
        super().__init__(oscmeta, temp_dir, log)
        self.best_release = None

    def fetch_source_information(self):
        self.best_release = requests.get(
            f"https://sourceforge.net/projects/{self.oscmeta['source']['project']}/best_release.json").json()
        self.log.log_status("  - Successfully retrieved \"best release\" information from SourceForge")

    def process_files(self):
        # download the archive
        with open(self.archive_path, "wb") as f:
            f.write(requests.get(self.best_release["platform_releases"]["windows"]["url"]).content)
        self.log.log_status(f"  - Downloaded file \"{self.best_release['release']['filename']}\"")
