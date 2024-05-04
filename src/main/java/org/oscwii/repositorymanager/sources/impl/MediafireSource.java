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

package org.oscwii.repositorymanager.sources.impl;

import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.sources.BaseSourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class MediafireSource extends BaseSourceDownloader
{
    @Autowired
    private MediafireSource(SourceRegistry registry)
    {
        super("mediafire", "MediaFire Source Downloader", "Downloads files from MediaFire.");
        registry.registerDownloader(this);
    }

    @Override
    protected Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir) throws IOException
    {
        Request fetchReq = new Request.Builder()
                .url(app.getMeta().source().url())
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                .build();

        Document html;
        try(Response response = HttpUtil.fetchSite(httpClient, fetchReq))
        {
            Assert.notNull(response.body(), "Response body is null!");
            html = Jsoup.parse(response.body().string());
        }

        // This is dirty.
        Element downloadElement = html.selectFirst("a.input.popsok");
        Assert.notNull(downloadElement, "Download element not found!");
        String downloadUrl = downloadElement.attr("href");

        logger.info("  - Successfully retrieved file location from MediaFire");
        logger.info("    - Location: {}", downloadUrl);

        return new Request.Builder()
                .url(downloadUrl)
                .addHeader("User-Agent", config.getUserAgent())
                .build();
    }

    @Override
    protected void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException
    {
        HttpUtil.downloadFile(httpClient, request, archivePath);
        logger.info("  - Downloaded file from MediaFire successfully");
    }
}
