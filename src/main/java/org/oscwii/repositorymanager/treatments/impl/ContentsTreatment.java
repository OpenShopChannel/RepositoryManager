package org.oscwii.repositorymanager.treatments.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.SimplePathVisitor;
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

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

            Path source = workingDir.resolve(arguments[0]);
            Path destination = workingDir.resolve(arguments[1]);
            checkAllowedPath(workingDir, source);
            checkAllowedPath(workingDir, destination);

            PathMatcher m = FileSystems.getDefault().getPathMatcher("glob:" + arguments[0]);
            Files.walkFileTree(workingDir, new Visitor(m, destination));

            logger.info("  - Moved {} to {}", arguments[0], arguments[1]);
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

    private static class Visitor extends SimplePathVisitor
    {
        private final PathMatcher m;
        private final Path destination;

        private Visitor(PathMatcher m, Path destination)
        {
            this.m = m;
            this.destination = destination;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            if(m.matches(file))
            {
                if(Files.isRegularFile(file))
                    Files.move(file, destination);
                else
                    FileUtils.moveDirectory(file.toFile(), destination.toFile());

                // We have found our file and have moved it, we can't have more than one file
                // in the same destination, so terminate the search.
                return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
        }
    }
}
