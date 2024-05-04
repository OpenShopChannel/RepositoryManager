/*
 * Copyright (c) 2023-2024 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.oscwii.repositorymanager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.database.dao.SettingsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;

import static org.eclipse.jgit.api.MergeResult.MergeStatus.CHECKOUT_CONFLICT;
import static org.eclipse.jgit.api.MergeResult.MergeStatus.CONFLICTING;

@Component
public class RepositorySource
{
    private final RepoManConfig config;
    private final SettingsDAO settingsDao;

    @Autowired
    public RepositorySource(RepoManConfig config, SettingsDAO settingsDao)
    {
        this.config = config;
        this.settingsDao = settingsDao;
    }

    public void initialize()
    {
        try
        {
            Optional<String> gitUrl = settingsDao.getSetting("git_url");
            if(Files.exists(config.getRepoDir()))
                FileSystemUtils.deleteRecursively(config.getRepoDir());

            Git.cloneRepository()
                    .setURI(gitUrl.orElseThrow())
                    .setDirectory(config.getRepoDir().toFile())
                    .call()
                    .close();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to clone app repository:", e);
        }
    }

    public void pull()
    {
        try(Repository repository = new FileRepositoryBuilder()
                .findGitDir(config.getRepoDir().toFile())
                .build();
            Git git = Git.wrap(repository))
        {
            PullResult result = git.pull()
                    .setFastForward(FastForwardMode.FF)
                    .setStrategy(MergeStrategy.RECURSIVE)
                    .setContentMergeStrategy(ContentMergeStrategy.CONFLICT)
                    .call();

            if(!result.isSuccessful())
            {
                MergeResult mergeResult = result.getMergeResult();
                if(mergeResult != null)
                {
                    MergeResult.MergeStatus status = mergeResult.getMergeStatus();
                    if(status == CHECKOUT_CONFLICT)
                        throw new RuntimeException("Checkout conflict: " + prettyList(mergeResult.getCheckoutConflicts()));
                    else if(status == CONFLICTING)
                        throw new RuntimeException("Merge conflict: " + prettyList(mergeResult.getConflicts().keySet()));
                }

                git.reset().call();
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to pull app repository:", e);
        }
    }

    private String prettyList(Collection<String> list)
    {
        return String.join("\n", list);
    }
}
