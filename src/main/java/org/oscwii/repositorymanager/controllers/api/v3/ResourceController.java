package org.oscwii.repositorymanager.controllers.api.v3;

import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping(path = "/api/v3", method = RequestMethod.GET)
public class ResourceController
{
    private final RepositoryIndex index;

    @Autowired
    public ResourceController(RepositoryIndex index)
    {
        this.index = index;
    }

    @GetMapping(value = "/contents/{slug}/icon.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<ByteArrayResource> getIcon(@PathVariable String slug) throws IOException
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        byte[] image = Files.readAllBytes(app.getAppFilesPath().resolve("icon.png"));
        return ResponseEntity.ok()
                .contentLength(image.length)
                .contentType(MediaType.IMAGE_PNG)
                .body(new ByteArrayResource(image));
    }

    @GetMapping(value = "/contents/{slug}/*.zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getZip(@PathVariable String slug)
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        Path archive = app.getDataPath().getParent().resolve(slug + ".zip");
        if(Files.notExists(archive))
            return ResponseEntity.notFound().build();

        FileSystemResource res = new FileSystemResource(archive);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + app.getSlug() + ".zip\"")
                .body(res);
    }
}
