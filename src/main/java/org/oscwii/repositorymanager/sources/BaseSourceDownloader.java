package org.oscwii.repositorymanager.sources;

import okhttp3.OkHttpClient;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.config.repoman.FetchConfig;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseSourceDownloader implements SourceDownloader
{
    private final String type;

    protected final Logger logger;

    @Autowired
    protected FetchConfig config;
    @Autowired
    protected OkHttpClient httpClient;

    protected BaseSourceDownloader(String type)
    {
        this.logger = SourceDownloader.LOGGER;
        this.type = type;
    }

    @Override
    public Path downloadFile(InstalledApp app, Path tmpDir) throws IOException
    {
        Path archivePath = tmpDir.resolve(app.getSlug() + ".package");

        logger.info("- Fetching source information");
        fetchFileInformation(app, archivePath, tmpDir);

        logger.info("- Processing obtained files");
        processFiles(app, archivePath, tmpDir);

        return archivePath;
    }

    protected abstract void fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir);

    protected abstract void processFiles(InstalledApp app, Path archivePath, Path tmpDir) throws IOException;

    @Override
    public String getType()
    {
        return type;
    }
}
