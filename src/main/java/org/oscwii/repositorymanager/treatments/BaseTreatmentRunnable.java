package org.oscwii.repositorymanager.treatments;

import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;

import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseTreatmentRunnable implements TreatmentRunnable
{
    private final String id;

    protected final Logger logger;

    protected BaseTreatmentRunnable(String id)
    {
        this.logger = RepositoryIndex.LOGGER;
        this.id = id;
    }

    @Override
    public abstract void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException;

    @Override
    public String getId()
    {
        return id;
    }

    protected void checkAllowedPath(Path workingDir, Path attempted)
    {
        attempted = attempted.toAbsolutePath();
        if(!attempted.toAbsolutePath().startsWith(workingDir))
        {
            throw new IllegalArgumentException("ILLEGAL ACCESS! Treatment tried to modify a prohibited path: " +
                    attempted);
        }
    }
}
