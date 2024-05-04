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
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;
import org.oscwii.repositorymanager.treatments.BaseTreatmentRunnable;
import org.oscwii.repositorymanager.treatments.TreatmentRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

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
                List<Path> files = stream.filter(path -> !path.equals(workingDir))
                        .toList();

                for(Path file : files)
                {
                    if(moveFile(file, destination, workingDir, m))
                    {
                        logger.info("  - Moved {} to {}", source, arguments[1]);
                        break;
                    }
                }
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
                    FileUtils.moveDirectory(file.toFile(), destination.toFile());

                return true;
            }

            return false;
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
