import requests

from sources.base_source_downloader import BaseSourceDownloader


class SourceDownloader(BaseSourceDownloader):
    name = "SourceForge Best Release Source Downloader"
    description = "Downloads files from a SourceForge \"best release\"."

    def __init__(self, oscmeta, temp_dir, log):
        super().__init__(oscmeta, temp_dir, log)
        self.best_release = None

    def fetch_source_information(self):
        self.best_release = requests.get(
            f"https://sourceforge.net/projects/{self.oscmeta['source']['project']}/best_release.json").json()
        self.log.log_status("  - Successfully retrieved \"best release\" information from SourceForge")

    def process_files(self):
        # download the archive
        self.download_from_url_to_file(self.best_release["platform_releases"]["windows"]["url"], self.archive_path)
        self.log.log_status(f"  - Downloaded file \"{self.best_release['release']['filename']}\"")
