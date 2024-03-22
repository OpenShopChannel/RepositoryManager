package org.oscwii.repositorymanager.sources.impl;

import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.sources.BaseSourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class ManualSource extends BaseSourceDownloader
{
    @Autowired
    private ManualSource(SourceRegistry registry)
    {
        super("manual");
        registry.registerDownloader(this);
    }

    @Override
    protected void fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir)
    {
        logger.info("  - Manual source type, downloads will be handled by treatments");
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir) throws IOException
    {
        // no-op
    }
}
