package org.oscwii.repositorymanager.sources;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.config.repoman.FetchConfig;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.utils.HttpUtil;
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
    protected Gson gson;
    @Autowired
    protected OkHttpClient httpClient;

    protected BaseSourceDownloader(String type)
    {
        this.logger = SourceDownloader.LOGGER;
        this.type = type;
    }

    @Override
    public Path downloadFiles(InstalledApp app, Path tmpDir) throws IOException
    {
        Path archivePath = tmpDir.resolve(app.getSlug() + ".package");

        logger.info("- Fetching source information");
        Request request = fetchFileInformation(app, archivePath, tmpDir);

        logger.info("- Processing obtained files");
        processFiles(app, archivePath, tmpDir, request);

        return archivePath;
    }

    protected abstract Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir);

    protected abstract void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException;

    protected void downloadFileFromUrl(String url, Path destination) throws IOException
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", config.getUserAgent())
                .build();
        HttpUtil.downloadFile(httpClient, request, destination);
    }

    @Override
    public String getType()
    {
        return type;
    }
}
