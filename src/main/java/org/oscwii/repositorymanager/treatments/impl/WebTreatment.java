package org.oscwii.repositorymanager.treatments.impl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.oscwii.repositorymanager.config.repoman.FetchConfig;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;
import org.oscwii.repositorymanager.treatments.BaseTreatmentRunnable;
import org.oscwii.repositorymanager.treatments.TreatmentRegistry;
import org.oscwii.repositorymanager.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class WebTreatment
{
    private final FetchConfig config;
    private final OkHttpClient httpClient;

    @Autowired
    private WebTreatment(TreatmentRegistry registry, FetchConfig config, OkHttpClient httpClient)
    {
        this.config = config;
        this.httpClient = httpClient;
        registry.registerTreatment(new Download());
    }

    public class Download extends BaseTreatmentRunnable
    {
        private Download()
        {
            super("web.download");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            String[] arguments = treatment.arguments();
            Assert.isTrue(arguments.length >= 2, "Download treatment requires two arguments!");

            Path destination = workingDir.resolve(arguments[1]);
            checkAllowedPath(workingDir, destination);

            Request request = new Request.Builder()
                    .url(arguments[0])
                    .addHeader("User-Agent", determineUserAgent(app))
                    .build();

            Files.createDirectories(destination.getParent());
            HttpUtil.downloadFile(httpClient, request, destination);
            logger.info("  - Downloaded {} to {}", arguments[0], arguments[1]);
        }
    }

    private String determineUserAgent(InstalledApp app)
    {
        OSCMeta.Source source = app.getMeta().source();
        String userAgent = config.getUserAgent();

        // Check if we need to use a custom user agent
        if(source.userAgent() != null)
        {
            // Get the real secret user agent or use the one in the manifest
            String customAgent = source.userAgent();
            userAgent = config.getSecretUserAgents().getOrDefault(customAgent, customAgent);
        }

        return userAgent;
    }
}
