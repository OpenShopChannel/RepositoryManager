/*
 * Copyright (c) 2023-2024 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.oscwii.repositorymanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.sentry.okhttp.SentryOkHttpEventListener;
import io.sentry.okhttp.SentryOkHttpInterceptor;
import okhttp3.OkHttpClient;
import org.oscwii.repositorymanager.config.repoman.FetchConfig;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.utils.serializers.OSCMetaTypeAdapter;
import org.oscwii.repositorymanager.utils.serializers.SourceTypeAdapter;
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
                .addInterceptor(new SentryOkHttpInterceptor())
                .eventListener(new SentryOkHttpEventListener())
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
    }

    @Autowired
    public void setupSentry(Optional<GitProperties> gitProperties)
    {
        String rel = gitProperties.isEmpty() ? "DEV" : gitProperties.get().getCommitId();
        System.setProperty("repoman.release", rel);
        System.setProperty("sentry.release", rel);
        System.setProperty("sentry.stacktrace.app.packages", "org.oscwii");
    }
}
