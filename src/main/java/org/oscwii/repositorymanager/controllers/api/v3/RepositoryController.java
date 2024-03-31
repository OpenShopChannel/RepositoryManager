package org.oscwii.repositorymanager.controllers.api.v3;

import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.api.App;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.services.FeaturedApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/api/v3", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public class RepositoryController
{
    private final FeaturedApp featuredApp;
    private final RepositoryIndex index;

    @Autowired
    public RepositoryController(FeaturedApp featuredApp, RepositoryIndex index)
    {
        this.featuredApp = featuredApp;
        this.index = index;
    }

    @GetMapping("/information")
    public ResponseEntity<Map<String, Object>> getInformation()
    {
        RepositoryInfo info = index.getInfo();
        Map<String, Object> information = Map.of(
                "name", info.name(),
                "provider", info.provider(),
                "description", info.description(),
                "available_categories", index.getCategories(),
                "available_apps_count", index.getContents().size(),
                "git_url", "TODO" // TODO
        );

        return ResponseEntity.ok(information);
    }

    @GetMapping("/contents")
    public ResponseEntity<List<App>> getContents()
    {
        List<App> apps = new ArrayList<>(index.getContents().size());
        for(InstalledApp installedApp : index.getContents())
            apps.add(new App(installedApp));

        return ResponseEntity.ok(apps);
    }

    @GetMapping("/featured-app")
    public ResponseEntity<App> getFeaturedApp()
    {
        InstalledApp app = featuredApp.getFeatured();
        if(app == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new App(app));
    }
}
