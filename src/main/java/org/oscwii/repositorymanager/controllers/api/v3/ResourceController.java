package org.oscwii.repositorymanager.controllers.api.v3;

import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.utils.AppUtil;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

@Controller("apiResourceController")
@RequestMapping(path = "/api/v3", method = RequestMethod.GET)
public class ResourceController extends RepoManController
{
    @GetMapping(value = "/contents/{slug}/icon.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<ByteArrayResource> getIcon(@PathVariable String slug) throws IOException
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return AppUtil.getIcon(app);
    }

    @GetMapping(value = "/contents/{slug}/{_slug}.zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getZip(@PathVariable String slug)
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return FileUtil.getContent(app.getDataPath().getParent().resolve(slug + ".zip"));
    }
}
