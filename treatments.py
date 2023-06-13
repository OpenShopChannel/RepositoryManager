import glob
import os
import re
import shutil
from xml.etree import ElementTree as et

import eventlet
import requests

import config
import helpers


class Treatment:
    def __init__(self, directory, oscmeta, slug):
        self.directory = directory
        self.oscmeta = oscmeta
        self.slug = slug


# Treatments for managing contents
class Contents(Treatment):
    def move(self, parameters):
        # move files from one directory to another
        # parameters: ["from_path", "to_path"]

        from_path = os.path.normpath(os.path.join(self.directory, parameters[0]))
        to_path = os.path.normpath(os.path.join(self.directory, parameters[1]))

        # create directories in the to_path if they don't exist
        if not os.path.exists(os.path.dirname(to_path)):
            os.makedirs(os.path.dirname(to_path))

        files = glob.glob(from_path)  # Get a list of files matching the pattern
        for file in files:
            shutil.move(file, to_path)  # Move each file to the target directory

        helpers.log_status(f'  - Moved {parameters[0]} to {parameters[1]}', 'success')

    def delete(self, parameters):
        # delete a file or directory
        # parameters: ["path"]

        path = os.path.normpath(os.path.join(self.directory, parameters[0]))

        if os.path.isfile(path):
            os.remove(path)
        else:
            shutil.rmtree(path)

        helpers.log_status(f'  - Deleted {parameters[0]}', 'success')


# Treatments for editing meta.xml
class Meta(Treatment):
    def init(self):
        # initializes a new meta.xml file with default values

        default_values = {
            "name": self.oscmeta["information"]["name"],
            "coder": self.oscmeta["information"]["author"],
            "version": self.oscmeta["information"]["version"],
            "short_description": "No description provided."
        }

        meta_xml_path = os.path.join(self.directory, "apps", self.slug, "meta.xml")

        # create the root element
        root = et.Element("app", version="1")

        # create child elements and set their values from the dictionary
        for key, value in default_values.items():
            element = et.SubElement(root, key)
            element.text = value

        # create the XML tree
        meta_xml = et.ElementTree(root)

        # write meta.xml
        meta_xml.write(meta_xml_path, encoding="UTF-8", xml_declaration=True)

        helpers.log_status(f'  - Created new meta.xml file', 'success')

    def set(self, parameters):
        # set a value in meta.xml (oscmeta is not the same as meta.xml)
        # parameters: ["key", "value"]

        key = parameters[0]
        value = parameters[1]

        meta_xml_path = os.path.join(self.directory, "apps", self.slug, "meta.xml")

        # read meta.xml
        meta_xml = et.parse(meta_xml_path)
        root = meta_xml.getroot()

        # create or update the key in root
        for element in root:
            if element.tag == key:
                element.text = value
                break
        else:
            new_element = et.SubElement(root, key)
            new_element.text = value

        # write meta.xml
        meta_xml.write(meta_xml_path)

        helpers.log_status(f'  - Set {key} to {value} in meta.xml', 'success')

    def remove_declaration(self):
        # Remove the unicode declaration from meta.xml, can help with some broken meta.xml files

        meta_xml_path = os.path.join(self.directory, "apps", self.slug, "meta.xml")

        # read meta.xml as file
        with open(meta_xml_path, "r") as f:
            xml = f.read()

            # Remove unicode declaration
            xml = xml.split("\n", 1)[1]

        # write meta.xml
        with open(meta_xml_path, "w") as f:
            f.write(xml)

        helpers.log_status(f'  - Removed unicode declaration from meta.xml', 'success')

    def remove_comments(self):
        # Remove comments from meta.xml, can help with some broken meta.xml files

        meta_xml_path = os.path.join(self.directory, "apps", self.slug, "meta.xml")

        # read meta.xml as file
        with open(meta_xml_path, "r") as f:
            xml = f.read()

            # Remove comments
            xml = re.sub(r"<!--(.|\s)*?-->", "", xml)

        # write meta.xml
        with open(meta_xml_path, "w") as f:
            f.write(xml)

        helpers.log_status(f'  - Removed comments from meta.xml', 'success')


class Web(Treatment):
    def __init__(self, user_agent, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.user_agent = user_agent

    def download(self, parameters):
        # download a file from a url
        # parameters: ["url", "path"]

        url = parameters[0]
        path = os.path.normpath(os.path.join(self.directory, parameters[1]))

        # create directories in the path if they don't exist
        if not os.path.exists(os.path.dirname(path)):
            os.makedirs(os.path.dirname(path))

        # download the file
        with eventlet.Timeout(config.URL_DOWNLOAD_TIMEOUT):
            r = requests.get(url, headers={"User-Agent": self.user_agent})
            with open(path, "wb") as f:
                f.write(r.content)

        helpers.log_status(f'  - Downloaded {parameters[0]} to {parameters[1]}', 'success')


class Archive(Treatment):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def extract(self, parameters):
        # extract an archive
        # parameters: ["path", "to_path"]

        path = os.path.normpath(os.path.join(self.directory, parameters[0]))
        to_path = os.path.normpath(os.path.join(self.directory, parameters[1]))

        # create directories in the to_path if they don't exist
        if not os.path.exists(os.path.dirname(to_path)):
            os.makedirs(os.path.dirname(to_path))

        shutil.unpack_archive(path, to_path)

        helpers.log_status(f'  - Extracted {parameters[0]} to {parameters[1]}', 'success')
