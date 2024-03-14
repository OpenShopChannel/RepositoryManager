package org.oscwii.repositorymanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.utils.OSCMetaTypeAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig
{
    @Bean
    public Gson gson()
    {
        return new GsonBuilder()
                .registerTypeAdapter(OSCMeta.class, new OSCMetaTypeAdapter())
                .create();
    }
}
