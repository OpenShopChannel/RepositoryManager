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

package org.oscwii.repositorymanager.controllers.hbb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.utils.AppUtil;
import org.oscwii.repositorymanager.utils.FileUtil;
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
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, method = RequestMethod.GET)
public class HBBResourceController extends RepoManController
{
    private final Logger logger;

    public HBBResourceController()
    {
        this.logger = LogManager.getLogger("Homebrew Browser Resources");
    }

    @GetMapping("/hbb/homebrew_browser/temp_files.zip")
    public ResponseEntity<Resource> getIconsZip()
    {
        Path archive = Path.of("data", "icons.zip");
        if(Files.notExists(archive))
        {
            logger.warn("Icon cache not generated!");
            return ResponseEntity.notFound().build();
        }

        return FileUtil.getContent(archive);
    }

    @GetMapping(path = {"/hbb/{slug}.png", "/hbb/{slug}/icon.png"}, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<ByteArrayResource> getIcon(@PathVariable String slug) throws IOException
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return AppUtil.getIcon(app);
    }

    @GetMapping(path = "/hbb/{slug}/{_slug}.zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getZip(@PathVariable String slug)
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return FileUtil.getContent(app.getDataPath().getParent().resolve(slug + ".zip"));
    }

    @GetMapping(path = "/unzipped_apps/{slug}/apps/{_slug}/meta.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Resource> getMetaXml(@PathVariable String slug)
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return FileUtil.getContent(app.getAppFilesPath().resolve("meta.xml"));
    }

    @GetMapping(path = "/unzipped_apps/{slug}/apps/{_slug}/boot.{type}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getBinary(@PathVariable String slug, @PathVariable String type)
    {
        InstalledApp app = index.getApp(slug);
        if(app == null)
            return ResponseEntity.notFound().build();

        return FileUtil.getContent(app.getAppFilesPath().resolve("boot." + type));
    }

    @GetMapping({"/hbb/hbb_download.php", "/hbb_download.php"})
    public ResponseEntity<String> registerDownload()
    {
        return ResponseEntity.noContent().build();
    }
}
