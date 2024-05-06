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

import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.database.dao.SettingsDAO;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.api.PublishedAppV3;
import org.oscwii.repositorymanager.model.api.PublishedAppV4;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.services.FeaturedAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public class VersionedAPIController extends RepoManController
{
    private final FeaturedAppService featuredApp;
    private final SettingsDAO settingsDao;

    @Autowired
    public VersionedAPIController(FeaturedAppService featuredApp, SettingsDAO settingsDao)
    {
        this.featuredApp = featuredApp;
        this.settingsDao = settingsDao;
    }

    @GetMapping({"/v3/information", "/v4/information"})
    public ResponseEntity<Map<String, Object>> getInformation()
    {
        Optional<String> gitUrl = settingsDao.getSetting("git_url");
        RepositoryInfo info = index.getInfo();

        Map<String, Object> information = new LinkedHashMap<>()
        {{
            put("name", info.name());
            put("provider", info.provider());
            put("description", info.description());
            put("git_url", gitUrl.orElse("Unknown"));
            put("available_apps_count", index.getContents().size());
            put("available_categories", index.getCategories());
            put("available_platforms", index.getPlatforms());
        }};

        return ResponseEntity.ok(information);
    }

    // API v3

    @GetMapping("/v3/contents")
    public ResponseEntity<List<PublishedAppV3>> getContentsV3()
    {
        List<PublishedAppV3> apps = new ArrayList<>(index.getContents().size());
        for(InstalledApp installedApp : index.getContents())
            apps.add(new PublishedAppV3(installedApp));

        return ResponseEntity.ok(apps);
    }

    @GetMapping("/v3/featured-app")
    public ResponseEntity<PublishedAppV3> getFeaturedAppV3()
    {
        InstalledApp app = featuredApp.getFeatured();
        if(app == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new PublishedAppV3(app));
    }

    // API v4

    @GetMapping("/v4/contents")
    public ResponseEntity<List<PublishedAppV4>> getContentsV4()
    {
        List<PublishedAppV4> apps = new ArrayList<>(index.getContents().size());
        for(InstalledApp installedApp : index.getContents())
            apps.add(new PublishedAppV4(installedApp));

        return ResponseEntity.ok(apps);
    }

    @GetMapping("/v4/featured-app")
    public ResponseEntity<PublishedAppV4> getFeaturedAppV4()
    {
        InstalledApp app = featuredApp.getFeatured();
        if(app == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new PublishedAppV4(app));
    }
}
