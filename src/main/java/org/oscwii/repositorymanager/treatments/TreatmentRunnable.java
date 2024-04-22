package org.oscwii.repositorymanager.treatments;

import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;

import java.io.IOException;
import java.nio.file.Path;

public interface TreatmentRunnable
{
    String getId();

    void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException;
}
