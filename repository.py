import os
import pygit2

import config
import helpers


def initialize(git_repo_url):
    # create/reset the repository directory
    if os.path.exists(config.REPO_DIR):
        helpers.rmtree(config.REPO_DIR)
    os.mkdir(config.REPO_DIR)

    # clone the repository
    pygit2.clone_repository(git_repo_url, config.REPO_DIR)


def pull():
    # pull the repository
    helpers.pull(pygit2.Repository(config.REPO_DIR))
