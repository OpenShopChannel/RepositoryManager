package org.oscwii.repositorymanager.sources.impl;

import com.google.gson.JsonObject;
import okhttp3.Request;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.sources.BaseSourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class SourceForgeSourceDownloader extends BaseSourceDownloader
{
    @Autowired
    private SourceForgeSourceDownloader(SourceRegistry registry)
    {
        super("sourceforge_release", "SourceForge Best Release Source Downloader", "Downloads files from a SourceForge \"best release\".");
        registry.registerDownloader(this);
    }

    @Override
    protected Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir) throws IOException
    {
        OSCMeta.Source source = app.getMeta().source();
        String project = source.url();

        return new Request.Builder()
                .addHeader("User-Agent", config.getUserAgent())
                .url(BEST_RELEASE.formatted(project))
                .build();
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException
    {
        JsonObject obj = HttpUtil.fetchJson(gson, httpClient, request);
        logger.info("  - Successfully retrieved \"best release\" information from SourceForge");

        JsonObject platforms = obj.getAsJsonObject("platform_releases");
        JsonObject release = platforms.getAsJsonObject("windows"); // why?
        String url = release.get("url").getAsString();
        downloadFileFromUrl(url, archivePath);
        logger.info("  - Downloaded file \"{}\"", release.get("filename").getAsString());
    }

    private static final String BEST_RELEASE = "https://sourceforge.net/projects/%s/best_release.json";
}
