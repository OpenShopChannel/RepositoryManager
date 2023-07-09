import re

import requests

from sources.base_source_downloader import BaseSourceDownloader


# mediafire does not have a proper downloads api, and so we will do a little bit of scraping
#
# thanks https://github.com/Juvenal-Yescas/mediafire-dl (MIT license) for some of
# the implementation details of this source downloader.


class SourceDownloader(BaseSourceDownloader):
    def __init__(self, oscmeta, temp_dir, log):
        super().__init__(oscmeta, temp_dir, log)
        self.response = None

    def fetch_source_information(self):
        session = requests.session()
        session.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
        }

        url = self.oscmeta['source']['location']

        while True:
            self.response = session.get(url, stream=True)
            if 'Content-Disposition' in self.response.headers:
                break

            for line in self.response.text.splitlines():
                m = re.search(r'href="((http|https)://download[^"]+)', line)
                if m:
                    url = m.groups()[0]
                    break
            else:
                raise Exception("Permission denied on mediafire file download")

        self.log.log_status("  - Successfully retrieved file location from MediaFire")
        self.log.log_status(f'    - Location: {url}')

    def process_files(self):
        # download the archive
        with open(self.archive_path, "wb") as f:
            f.write(self.response.content)
        self.log.log_status(f"  - Downloaded file from MediaFire successfully")
