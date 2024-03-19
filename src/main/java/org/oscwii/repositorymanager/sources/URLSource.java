package org.oscwii.repositorymanager.sources;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.utils.QuietException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class URLSource extends BaseSourceDownloader
{
    @Autowired
    private URLSource(SourceRegistry registry)
    {
        super("url");
        registry.registerDownloader(this);
    }

    @Override
    protected void fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir)
    {
        // no-op
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir) throws IOException
    {
        OSCMeta.Source source = app.getMeta().source();
        Request request = prepareRequest(source);

        Call call = httpClient.newCall(request);
        try(Response response = call.execute())
        {
            if(!response.isSuccessful())
                throw new QuietException("HTTP request failed with status code " + response.code());

            Assert.notNull(response.body(), "Response body is null!");
            Files.copy(response.body().byteStream(), archivePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Request prepareRequest(OSCMeta.Source source)
    {
        String url = source.url();
        String userAgent = config.getUserAgent();

        // Check if we need to use a custom user agent
        if(source.userAgent() != null)
        {
            // Get the real secret user agent or use the one in the manifest
            String customAgent = source.userAgent();
            userAgent = config.getSecretUserAgents().getOrDefault(customAgent, customAgent);
        }

        return new Request.Builder()
                .url(url)
                .addHeader("User-Agent", userAgent)
                .build();
    }
}
