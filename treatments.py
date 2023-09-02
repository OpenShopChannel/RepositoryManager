import glob
import os
import re
import shutil
from xml.etree import ElementTree as et

import eventlet
import requests

import config
import logger


class Treatment:
    def __init__(self, directory, oscmeta, slug, log=logger.Log("treatment")):
        self.directory = directory
        self.oscmeta = oscmeta
        self.slug = slug
        self.log = log

    def path_allowed_check(self, path):
        # Check if the treatment is allowed to perform a file operation on a certain path

        # Check if path is outside the temporary directory
        if not (os.path.abspath(path).startswith(os.path.abspath(self.directory))):
            raise Exception(f"CRITICAL INCIDENT! Treatment tried to deal with a prohibited path (Please resolve!): {path}")


# Treatments for managing contents
class Contents(Treatment):
    def move(self, parameters):
        # move files from one directory to another
        # parameters: ["from_path", "to_path"]

        from_path = os.path.normpath(os.path.join(self.directory, parameters[0]))
        to_path = os.path.normpath(os.path.join(self.directory, parameters[1]))
        self.path_allowed_check(from_path)
        self.path_allowed_check(to_path)

        # create directories in the to_path if they don't exist
        if not os.path.exists(os.path.dirname(to_path)):
            os.makedirs(os.path.dirname(to_path))

        files = glob.glob(from_path)  # Get a list of files matching the pattern
        for file in files:
            shutil.move(file, to_path)  # Move each file to the target directory

        self.log.log_status(f'  - Moved {parameters[0]} to {parameters[1]}', 'success')

    def delete(self, parameters):
        # delete a file or directory
        # parameters: ["path"]

        path = os.path.normpath(os.path.join(self.directory, parameters[0]))
        self.path_allowed_check(path)

        if os.path.isfile(path):
            os.remove(path)
        else:
            shutil.rmtree(path)

        self.log.log_status(f'  - Deleted {parameters[0]}', 'success')


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
        meta_xml.write(meta_xml_path, encoding="utf-8", xml_declaration=True)

        self.log.log_status(f'  - Created new meta.xml file', 'success')

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
        et.ElementTree(root).write(meta_xml_path, encoding="utf-8", xml_declaration=True)

        self.log.log_status(f'  - Set {key} to {value} in meta.xml', 'success')

    def remove_declaration(self):
        # Remove the broken unicode declaration from meta.xml, and add a correct one

        meta_xml_path = os.path.join(self.directory, "apps", self.slug, "meta.xml")

        # Read meta.xml as file
        with open(meta_xml_path, "r") as f:
            xml = f.read()

            # Remove the broken unicode declaration
            xml = xml.split("\n", 1)[1]

        # Write meta.xml with a correct XML declaration
        with open(meta_xml_path, "w") as f:
            f.write('<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(xml)

        self.log.log_status(f'  - Removed potentially broken unicode declaration and added correct '
                            f'XML declaration to meta.xml', 'success')

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

        self.log.log_status(f'  - Removed comments from meta.xml', 'success')


class Web(Treatment):
    def __init__(self, user_agent, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.user_agent = user_agent

    def download(self, parameters):
        # download a file from a url
        # parameters: ["url", "path"]

        url = parameters[0]
        path = os.path.normpath(os.path.join(self.directory, parameters[1]))
        self.path_allowed_check(path)

        # create directories in the path if they don't exist
        if not os.path.exists(os.path.dirname(path)):
            os.makedirs(os.path.dirname(path))

        # download the file
        with eventlet.Timeout(config.URL_DOWNLOAD_TIMEOUT):
            r = requests.get(url, headers={"User-Agent": self.user_agent})
            with open(path, "wb") as f:
                f.write(r.content)

        self.log.log_status(f'  - Downloaded {parameters[0]} to {parameters[1]}', 'success')


class Archive(Treatment):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def extract(self, parameters):
        # extract an archive
        # parameters: ["path", "to_path"]

        path = os.path.normpath(os.path.join(self.directory, parameters[0]))
        to_path = os.path.normpath(os.path.join(self.directory, parameters[1]))
        self.path_allowed_check(path)
        self.path_allowed_check(to_path)

        # create directories in the to_path if they don't exist
        if not os.path.exists(os.path.dirname(to_path)):
            os.makedirs(os.path.dirname(to_path))

        shutil.unpack_archive(path, to_path)

        self.log.log_status(f'  - Extracted {parameters[0]} to {parameters[1]}', 'success')
