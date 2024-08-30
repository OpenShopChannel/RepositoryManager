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

package org.oscwii.repositorymanager.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.database.dao.AppDAO;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class DownloadService
{
    private final AppDAO appDao;
    private final Cache<String, Boolean> dlAnAb;
    private final Logger logger;

    @Autowired
    public DownloadService(AppDAO appDao)
    {
        this.appDao = appDao;
        this.dlAnAb = CacheBuilder.newBuilder()
                .expireAfterAccess(3, TimeUnit.MINUTES)
                .build();
        this.logger = LogManager.getLogger(DownloadService.class);
    }

    public void reportShopDownload(InstalledApp app, String data)
    {
        if(storeDownload(app, data))
            logger.debug("Recorded download from SHOP channel for app {}", app);
    }

    public ResponseEntity<Resource> provideDownload(InstalledApp app, HttpServletRequest req)
    {
        // Increment download counter
        if(storeDownload(app, getDownloadKey(req, app.getSlug())))
            logger.debug("Recorded download from DIRECT channel for app {}", app);

        Path zip = app.getDataPath().getParent().resolve(app.getSlug() + ".zip");
        return FileUtil.getContent(zip);
    }

    private boolean storeDownload(InstalledApp app, String downloadKey)
    {
        if(dlAnAb.getIfPresent(downloadKey) == null)
        {
            app.incrementDownloads();
            appDao.setDownloads(app);
            dlAnAb.put(downloadKey, false);
            return true;
        }

        return false;
    }

    private String getDownloadKey(HttpServletRequest req, String slug)
    {
        ByteArrayDataOutput encode = ByteStreams.newDataOutput();
        encode.writeUTF(req.getRemoteAddr());
        encode.writeUTF(req.getHeader("User-Agent"));
        encode.writeUTF(slug);
        return Base64.encodeBase64String(encode.toByteArray());
    }
}
