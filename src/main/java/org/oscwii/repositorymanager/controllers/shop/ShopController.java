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

package org.oscwii.repositorymanager.controllers.shop;

import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.services.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shop")
public class ShopController extends RepoManController
{
    private final DownloadService downloadService;

    @Autowired
    public ShopController(DownloadService downloadService)
    {
        this.downloadService = downloadService;
    }

    @PostMapping("/download/{slug}")
    public ResponseEntity<?> reportDownload(@PathVariable String slug, @RequestBody String client)
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        downloadService.reportShopDownload(app, client);
        return ResponseEntity.noContent().build();
    }
}
