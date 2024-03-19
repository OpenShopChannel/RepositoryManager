package org.oscwii.repositorymanager.sources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.model.app.InstalledApp;

import java.io.IOException;
import java.nio.file.Path;

public interface SourceDownloader
{
    String getType();

    Path downloadFile(InstalledApp app, Path tmpDir) throws IOException;

    Logger LOGGER = LogManager.getLogger(SourceDownloader.class);
}
