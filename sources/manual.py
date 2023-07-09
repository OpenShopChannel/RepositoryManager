from sources.base_source_downloader import BaseSourceDownloader


class SourceDownloader(BaseSourceDownloader):
    def __init__(self, oscmeta, temp_dir, log):
        super().__init__(oscmeta, temp_dir, log)

    def fetch_source_information(self):
        self.log.log_status(f'  - Manual source type, downloads will be handled by treatments')

    def process_files(self):
        pass
