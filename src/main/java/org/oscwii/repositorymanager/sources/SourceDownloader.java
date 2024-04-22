package org.oscwii.repositorymanager.sources;

import org.oscwii.repositorymanager.model.app.InstalledApp;

import java.io.IOException;
import java.nio.file.Path;

public interface SourceDownloader
{
    String getType();

    String getName();

    String getDescription();

    Path downloadFiles(InstalledApp app, Path tmpDir) throws IOException;
}
