import os
import stat

import pygit2

from models import SettingsModel, db, ModeratedBinariesModel


def get_settings():
    settings = {}
    for setting in SettingsModel.query.all():
        settings[setting.key] = setting.value
    return settings


def update_setting(key: str, value: str):
    # create if it doesn't exist
    if SettingsModel.query.filter_by(key=key).first() is None:
        settings = SettingsModel(key=key, value=value)
        db.session.add(settings)
    else:
        settings = SettingsModel.query.filter_by(key=key).first()
        settings.value = value
    db.session.commit()


# delete a directory and all its contents, even if read-only files are present
def rmtree(top):
    for root, dirs, files in os.walk(top, topdown=False):
        for name in files:
            filename = os.path.join(root, name)
            os.chmod(filename, stat.S_IWUSR)
            os.remove(filename)
        for name in dirs:
            os.rmdir(os.path.join(root, name))
    os.rmdir(top)


def pull(repo, remote_name='origin', branch='master'):
    """
    Pull changes for the specified remote (defaults to origin).

    Code originally from Michael Boselowitz at: https://github.com/MichaelBoselowitz/pygit2-examples/blob/master/examples.py#L54-L91
    Licensed under the MIT license.
    """
    for remote in repo.remotes:
        if remote.name == remote_name:
            remote.fetch()
            remote_master_id = repo.lookup_reference('refs/remotes/origin/%s' % (branch)).target
            merge_result, _ = repo.merge_analysis(remote_master_id)
            # Up to date, do nothing
            if merge_result & pygit2.GIT_MERGE_ANALYSIS_UP_TO_DATE:
                return
            # We can just fastforward
            elif merge_result & pygit2.GIT_MERGE_ANALYSIS_FASTFORWARD:
                repo.checkout_tree(repo.get(remote_master_id))
                try:
                    master_ref = repo.lookup_reference('refs/heads/%s' % (branch))
                    master_ref.set_target(remote_master_id)
                except KeyError:
                    repo.create_branch(branch, repo.get(remote_master_id))
                repo.head.set_target(remote_master_id)
            elif merge_result & pygit2.GIT_MERGE_ANALYSIS_NORMAL:
                repo.merge(remote_master_id)

                if repo.index.conflicts is not None:
                    for conflict in repo.index.conflicts:
                        print('Conflicts found in:', conflict[0].path)
                    raise AssertionError('Conflicts, ahhhhh!!')

                user = repo.default_signature
                tree = repo.index.write_tree()
                commit = repo.create_commit('HEAD',
                                            user,
                                            user,
                                            'Merge!',
                                            tree,
                                            [repo.head.target, remote_master_id])
                # We need to do this or git CLI will think we are still merging.
                repo.state_cleanup()
            else:
                raise AssertionError('Unknown merge analysis result')


def app_index_directory_location(slug):
    return os.path.join('data', 'contents', slug)


def notifications():
    return {
        "pending_moderation": ModeratedBinariesModel.query.filter_by(status='pending').count()
    }
