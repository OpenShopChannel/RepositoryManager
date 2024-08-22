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

package org.oscwii.repositorymanager.controllers.api;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.services.DownloadService;
import org.oscwii.repositorymanager.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController("apiResourceController")
@RequestMapping(path = {"/api", "/api/v3", "/api/v4"}, method = RequestMethod.GET)
public class APIResourceController extends RepoManController
{
    private final DownloadService downloadService;

    @Autowired
    public APIResourceController(DownloadService downloadService)
    {
        this.downloadService = downloadService;
    }

    @GetMapping(value = "/contents/{slug}/icon.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<ByteArrayResource> getIcon(@PathVariable String slug) throws IOException
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return AppUtil.getIcon(app);
    }

    @GetMapping(value = "/contents/{slug}/{_slug}.zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getZip(@PathVariable String slug, HttpServletRequest request)
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return downloadService.provideDownload(app, request);
    }
}
