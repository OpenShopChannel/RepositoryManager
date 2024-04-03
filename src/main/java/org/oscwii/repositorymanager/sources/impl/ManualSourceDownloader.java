package org.oscwii.repositorymanager.sources.impl;

import okhttp3.Request;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.sources.BaseSourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class ManualSourceDownloader extends BaseSourceDownloader
{
    @Autowired
    private ManualSourceDownloader(SourceRegistry registry)
    {
        super("manual", "Manual Source Downloader", "Handles manual source type.");
        registry.registerDownloader(this);
    }

    @Override
    protected Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir)
    {
        logger.info("  - Manual source type, downloads will be handled by treatments");
        return null;
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException
    {
        // no-op
    }
}
