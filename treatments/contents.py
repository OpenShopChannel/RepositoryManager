import os

import helpers


def move(temp_dir, parameters):
    # move files from one directory to another
    # parameters: ["from_path", "to_path"]

    from_path = os.path.normpath(os.path.join(temp_dir, parameters[0]))
    to_path = os.path.normpath(os.path.join(temp_dir, parameters[1]))

    # create directories in the to_path if they don't exist
    if not os.path.exists(os.path.dirname(to_path)):
        os.makedirs(os.path.dirname(to_path))

    os.rename(from_path, to_path)

    helpers.log_status(f'-- Moved {parameters[0]} to {parameters[1]}', 'success')
