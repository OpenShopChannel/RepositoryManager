package org.oscwii.repositorymanager.sources.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.Request;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.sources.BaseSourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.utils.HttpUtil;
import org.oscwii.repositorymanager.utils.QuietException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class ItchioSourceDownloader extends BaseSourceDownloader
{
    @Autowired
    private ItchioSourceDownloader(SourceRegistry registry)
    {
        super("itchio");
        registry.registerDownloader(this);
    }

    @Override
    protected Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir) throws IOException
    {
        OSCMeta.Source source = app.getMeta().source();
        String[] split = source.url().split("/", 2);
        Assert.isTrue(split.length == 2, "Invalid itch.io source");

        String creator = split[0];
        String game = split[1];

        // Fetch game data
        Request request = request(GAME_DATA.formatted(creator, game));
        JsonObject obj = HttpUtil.fetchJson(gson, httpClient, request);
        logger.info("  - Successfully found itch.io game {}", obj.get("title").getAsString());

        // Fetch uploads
        Assert.hasText(config.getItchioToken(), "  - No valid Itch.io token found, unable to continue!");
        logger.info("  - Authenticating with itch.io");
        return request(UPLOADS.formatted(config.getItchioToken(), obj.get("id").getAsInt()));
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException
    {
        boolean found = false;
        OSCMeta.Source source = app.getMeta().source();
        JsonObject uploads = HttpUtil.fetchJson(gson, httpClient, request);

        for(JsonElement element : uploads.getAsJsonArray("uploads"))
        {
            JsonObject upload = element.getAsJsonObject();
            if(upload.get("display_name").getAsString().equals(source.file()))
            {
                found = true;
                int gId = upload.get("id").getAsInt();
                logger.info("  - Found upload with ID {}", gId);
                request = request(DOWNLOAD.formatted(config.getItchioToken(), gId));
                JsonObject download = HttpUtil.fetchJson(gson, httpClient, request);
                downloadFileFromUrl(download.get("url").getAsString(), archivePath);
                logger.info("  - Downloaded upload {}", source.file());
                break;
            }
        }

        if(!found)
            throw new QuietException("Could not find itch.io upload");
    }

    private Request request(String url)
    {
        return new Request.Builder()
                .url(url)
                .addHeader("User-Agent", config.getUserAgent())
                .build();
    }

    private static final String GAME_DATA = "https://%s.itch.io/%s/data.json";
    private static final String UPLOADS   = "https://itch.io/api/1/%s/game/%d/uploads";
    private static final String DOWNLOAD  = "https://itch.io/api/1/%s/upload/%d/download";
}
