/*
 * Copyright (c) 2023-2025 Open Shop Channel
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

package org.oscwii.repositorymanager.sources.impl;

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
public class URLSourceDownloader extends BaseSourceDownloader
{
    @Autowired
    private URLSourceDownloader(SourceRegistry registry)
    {
        super("url", "URL Source Downloader", "Downloads files from a URL source.");
        registry.registerDownloader(this);
    }

    @Override
    protected Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir)
    {
        return prepareRequest(app.getMeta().source());
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException
    {
        HttpUtil.downloadFile(httpClient, request, archivePath);
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
