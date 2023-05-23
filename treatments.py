import os
import shutil
from xml.etree import ElementTree as et

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

        os.rename(from_path, to_path)

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
