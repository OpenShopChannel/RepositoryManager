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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.database.dao.SettingsDAO;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.services.FeaturedAppService;
import org.oscwii.repositorymanager.utils.serializers.AppSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public class VersionedAPIController extends RepoManController
{
    private final AppSerializer appSerializer;
    private final FeaturedAppService featuredApp;
    private final Gson gson;
    private final SettingsDAO settingsDao;

    @Autowired
    public VersionedAPIController(FeaturedAppService featuredApp, Gson gson, SettingsDAO settingsDao)
    {
        this.appSerializer = new AppSerializer();
        this.featuredApp = featuredApp;
        this.gson = gson;
        this.settingsDao = settingsDao;
    }

    @GetMapping({"/v3/information", "/v4/information"})
    public ResponseEntity<Map<String, Object>> getInformation()
    {
        Optional<String> gitUrl = settingsDao.getSetting("git_url");
        RepositoryInfo info = index.getInfo();

        Map<String, Object> information = Map.of(
                "name", info.name(),
                "provider", info.provider(),
                "description", info.description(),
                "available_categories", index.getCategories(),
                "available_apps_count", index.getContents().size(),
                "git_url", gitUrl.orElse("Unknown")
        );

        return ResponseEntity.ok(information);
    }

    // API v3

    @GetMapping("/v3/contents")
    public ResponseEntity<String> getContentsV3()
    {
        JsonArray apps = new JsonArray(index.getContents().size());
        for(InstalledApp installedApp : index.getContents())
            apps.add(appSerializer.serializeV3(installedApp));

        return ResponseEntity.ok(gson.toJson(apps));
    }

    @GetMapping("/v3/featured-app")
    public ResponseEntity<String> getFeaturedAppV3()
    {
        InstalledApp app = featuredApp.getFeatured();
        if(app == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(gson.toJson(appSerializer.serializeV3(app)));
    }

    // API v4

    @GetMapping("/v4/contents")
    public ResponseEntity<String> getContentsV4()
    {
        JsonArray apps = new JsonArray(index.getContents().size());
        for(InstalledApp installedApp : index.getContents())
            apps.add(appSerializer.serializeV4(installedApp));

        return ResponseEntity.ok(gson.toJson(apps));
    }

    @GetMapping("/v4/featured-app")
    public ResponseEntity<String> getFeaturedAppV4()
    {
        InstalledApp app = featuredApp.getFeatured();
        if(app == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(gson.toJson(appSerializer.serializeV4(app)));
    }
}
