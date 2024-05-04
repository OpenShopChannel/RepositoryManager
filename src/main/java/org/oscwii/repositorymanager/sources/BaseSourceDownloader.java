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

package org.oscwii.repositorymanager.sources;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.config.repoman.FetchConfig;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseSourceDownloader implements SourceDownloader
{
    private final String type, name, description;

    protected final Logger logger;

    @Autowired
    protected FetchConfig config;
    @Autowired
    protected Gson gson;
    @Autowired
    protected OkHttpClient httpClient;

    protected BaseSourceDownloader(String type, String name, String description)
    {
        this.type = type;
        this.name = name;
        this.description = description;
        this.logger = RepositoryIndex.LOGGER;
    }

    @Override
    public Path downloadFiles(InstalledApp app, Path tmpDir) throws IOException
    {
        Path archivePath = tmpDir.resolve(app.getSlug() + ".package");

        logger.info("- Fetching source information");
        Request request = fetchFileInformation(app, archivePath, tmpDir);

        logger.info("- Processing obtained files");
        processFiles(app, archivePath, tmpDir, request);

        return archivePath;
    }

    protected abstract Request fetchFileInformation(InstalledApp app, Path archivePath, Path tmpDir) throws IOException;

    protected abstract void processFiles(InstalledApp app, Path archivePath, Path tmpDir, Request request) throws IOException;

    protected void downloadFileFromUrl(String url, Path destination) throws IOException
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", config.getUserAgent())
                .build();
        HttpUtil.downloadFile(httpClient, request, destination);
    }

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }
}
