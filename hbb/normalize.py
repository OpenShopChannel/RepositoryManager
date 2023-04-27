from __future__ import annotations

from typing import Union


# Some of the code for HBB support was originally written by Spotlight for Open Shop Channel's "Danbo Shop Server".
class Normalize(object):
    """Normalize ensures CRLF line endings are present on the response.

    In the case of the Homebrew Browser, responses _must_ end with CRLF.
    Otherwise, the browser will horribly fail parsing."""
    response = ""

    def add(self, string: Union[int | str]):
        """Adds a value to the internal string, adding only a space after it.

        This is useful for the Homebrew Browser, with multiple properties present on a line."""
        if type(string) == int:
            string = f"{string}"

        self.response += string + " "

    def add_line(self, string: str):
        """Adds a value to the internal string, inserting a CRLF after it."""
        self.response += string + "\r\n"
