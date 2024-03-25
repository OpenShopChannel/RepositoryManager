package org.oscwii.repositorymanager.sources.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.sources.BaseSourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.utils.FnMatch;
import org.oscwii.repositorymanager.utils.QuietException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitHubSourceDownloader extends BaseSourceDownloader
{
    @Autowired
    private GitHubSourceDownloader(SourceRegistry registry)
    {
        super("github_release");
        registry.registerDownloader(this);
    }

    @Override
    protected Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir)
    {
        OSCMeta.Source source = app.getMeta().source();
        Request.Builder request = new Request.Builder();

        if(!config.getGithubToken().isEmpty())
        {
            logger.info("  - Authenticating with GitHub");
            request.addHeader("Authorization", "token " + config.getGithubToken());
        }
        else
            logger.info("  - No valid GitHub token found, using unauthenticated requests.");

        return request.url(LATEST_RELEASE.formatted(source.url()))
                .addHeader("User-Agent", config.getUserAgent())
                .build();
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException
    {
        Call call = httpClient.newCall(request);
        JsonObject obj;
        OSCMeta.Source source = app.getMeta().source();

        try(Response response = call.execute())
        {
            if(!response.isSuccessful())
                throw new QuietException("HTTP request failed with status code " + response.code());

            Assert.notNull(response.body(), "Response body is null!");
            obj = gson.fromJson(new InputStreamReader(response.body().byteStream()), JsonObject.class);
        }

        JsonArray assets = obj.getAsJsonArray("assets");
        List<String> files = new ArrayList<>();
        files.add(source.file());
        files.addAll(source.additionalFiles());

        logger.info("  - Successfully fetched latest release");

        for(String file : files)
        {
            boolean found = false;

            for(JsonElement eAsset : assets)
            {
                JsonObject asset = eAsset.getAsJsonObject();
                String fileName = asset.get("name").getAsString();

                if(!FnMatch.fnmatch(file, fileName))
                    continue;

                found = true;
                logger.info("  - Found asset {}", fileName);
                String url = asset.get("browser_download_url").getAsString();

                if(file.equals(source.file()))
                    downloadFileFromUrl(url, archivePath);
                else
                    downloadFileFromUrl(url, tmpDir.resolve(fileName));

                logger.info("  - Downloaded asset {}", fileName);
                break;
            }

            if(!found)
                throw new QuietException("Could not find asset: " + file);
        }
    }

    private static final String LATEST_RELEASE = "https://api.github.com/repos/%s/releases/latest";
}
