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
import java.nio.file.Files;
import java.nio.file.Path;

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

            if(Files.isRegularFile(source))
                Files.move(source, destination);
            else
                FileUtils.moveDirectory(source.toFile(), destination.toFile());

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
}
