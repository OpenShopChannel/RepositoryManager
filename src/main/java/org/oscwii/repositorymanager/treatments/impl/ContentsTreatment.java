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

package org.oscwii.repositorymanager.treatments.impl;

import org.apache.commons.io.FileUtils;
import org.oscwii.repositorymanager.exceptions.QuietException;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;
import org.oscwii.repositorymanager.treatments.BaseTreatmentRunnable;
import org.oscwii.repositorymanager.treatments.TreatmentRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

@Component
public class ContentsTreatment
{
    @Autowired
    private ContentsTreatment(TreatmentRegistry registry)
    {
        registry.registerTreatment(new Move());
        registry.registerTreatment(new Delete());
    }

    public static class Move extends BaseTreatmentRunnable
    {
        private Move()
        {
            super("contents.move");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            String[] arguments = treatment.arguments();
            Assert.isTrue(arguments.length >= 2, "Move treatment requires two arguments!");

            Path destination = workingDir.resolve(arguments[1]);
            checkAllowedPath(workingDir, destination);

            // Remove trailing slash
            String source = arguments[0];
            if(source.endsWith("/"))
                source = source.substring(0, source.length() - 1);

            PathMatcher m = FileSystems.getDefault().getPathMatcher("glob:" + source);
            try(Stream<Path> stream = Files.walk(workingDir))
            {
                boolean moved = false;
                List<Path> files = stream.filter(path -> !path.equals(workingDir))
                        .toList();

                for(Path file : files)
                {
                    if(moveFile(file, destination, workingDir, m))
                    {
                        moved = true;
                        logger.info("  - Moved {} to {}", source, arguments[1]);
                        break;
                    }
                }

                if(!moved)
                    throw new QuietException("  - No files matched the pattern: '" + source + "'");
            }
        }

        private boolean moveFile(Path file, Path destination, Path workingDir, PathMatcher matcher) throws IOException
        {
            Path path = workingDir.relativize(file);
            checkAllowedPath(workingDir, destination.resolve(path));

            if(matcher.matches(path))
            {
                if(Files.isRegularFile(file))
                {
                    Files.createDirectories(destination.getParent());
                    Files.move(file, destination, StandardCopyOption.REPLACE_EXISTING);
                }
                else
                {
                    if(Files.exists(destination) && Files.isDirectory(destination))
                        mergeDirectories(file, destination);
                    else
                        FileUtils.moveDirectory(file.toFile(), destination.toFile());
                }

                return true;
            }

            return false;
        }

        private void mergeDirectories(Path source, Path destination) throws IOException
        {
            try(Stream<Path> stream = Files.walk(source))
            {
                List<Path> files = stream.filter(path -> !path.equals(source)).toList();
                for(Path path : files)
                {
                    Path relative = destination.resolve(source.relativize(path));

                    if(Files.isDirectory(path))
                        Files.createDirectories(relative);
                    else
                        Files.move(path, relative, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // The following is very annoying and was added to workaround Windows ignoring casing in file names
            FileUtils.deleteDirectory(source.toFile());
            String finalName = source.toFile().getName();
            // First we will have to rename it to something that will not clash with the first name
            String tempName = finalName + "-" + System.currentTimeMillis();
            File tempRenamedFile = destination.getParent().resolve(tempName).toFile();
            FileUtils.moveDirectory(destination.toFile(), tempRenamedFile);
            // Then rename back to the desired name
            FileUtils.moveDirectory(tempRenamedFile, destination.getParent().resolve(finalName).toFile());
        }
    }

    public static class Delete extends BaseTreatmentRunnable
    {
        private Delete()
        {
            super("contents.delete");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            String[] arguments = treatment.arguments();
            Assert.isTrue(arguments.length >= 1, "Delete treatment requires one argument!");

            Path target = workingDir.resolve(arguments[0]);
            checkAllowedPath(workingDir, target);

            if(Files.isRegularFile(target))
                Files.delete(target);
            else
                FileSystemUtils.deleteRecursively(target);

            logger.info("  - Deleted {}", arguments[0]);
        }
    }
}
