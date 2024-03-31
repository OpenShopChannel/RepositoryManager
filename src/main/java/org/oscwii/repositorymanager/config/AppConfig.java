package org.oscwii.repositorymanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import org.oscwii.repositorymanager.config.repoman.FetchConfig;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.utils.OSCMetaTypeAdapter;
import org.oscwii.repositorymanager.utils.SourceTypeAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig
{
    @Bean
    public Gson gson()
    {
        return new GsonBuilder()
                .registerTypeAdapter(OSCMeta.Source.class, new SourceTypeAdapter())
                .registerTypeAdapter(OSCMeta.class, new OSCMetaTypeAdapter())
                .create();
    }

    @Bean
    public OkHttpClient httpClient(FetchConfig config)
    {
        return new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
    }

    @Autowired
    public void setupSentry(Optional<GitProperties> gitProperties)
    {
        String rel = gitProperties.isEmpty() ? "DEV" : gitProperties.get().getCommitId();
        System.setProperty("sentry.release", rel);
        System.setProperty("sentry.stacktrace.app.packages", "org.oscwii");
    }
}
