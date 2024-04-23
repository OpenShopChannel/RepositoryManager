package org.oscwii.repositorymanager.controllers.hbb;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.app.Category;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.InstalledApp.IndexComputedInfo;
import org.oscwii.repositorymanager.model.app.InstalledApp.MetaXml;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/*
 * Some of the code for HBB support was originally written by Spotlight for Open Shop Channel's "Danbo Shop Server".
 * Ported to Java by Artuto.
 */
@RestController
@RequestMapping(path = "/hbb", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET)
public class HBBController extends RepoManController
{
    @GetMapping(value = {"/homebrew_browser/listv036.txt", "/listv036.txt"})
    public ResponseEntity<String> appsList()
    {
        List<InstalledApp> apps = new ArrayList<>(index.getContents().size());
        for(Category category : index.getCategories())
            for(InstalledApp app : index.getContents())
                if(app.getCategory().equals(category))
                    apps.add(app);

        Category currentCategory = apps.stream().findFirst()
                .map(InstalledApp::getCategory).orElse(null);
        HBBResponse response = new HBBResponse();
        response.appendLine(HBBResponse.START_LINE);

        for(InstalledApp app : apps)
        {
            if(app.getSlug().equals("homebrew_browser"))
                continue;

            OSCMeta meta = app.getMeta();
            MetaXml metaXml = app.getMetaXml();
            IndexComputedInfo compInfo = app.getComputedInfo();

            if(currentCategory != app.getCategory())
            {
                response.appendLine("=" + currentCategory + "=");
                currentCategory = app.getCategory();
            }

            String longDescPrefix = "";
            if(meta.flags().contains(OSCMeta.Flag.WRITES_TO_NAND))
                longDescPrefix += "[CAUTION! Writes to NAND!] ";

            // The following metadata should be all in one line
            // Slug
            response.append(app.getSlug())
                    // Date added to repo
                    .append(compInfo.releaseDate)
                    // Icon size
                    .append(compInfo.iconSize)
                    // Binary size
                    .append(compInfo.binarySize)
                    // Package type
                    .append(compInfo.packageType)
                    // Archive size
                    .append(compInfo.archiveSize)
                    // Download and Rating count
                    .append(0).append(0)
                    // Peripherals
                    .append(compInfo.peripherals);

            // Folders to create
            StringBuilder subdirectories = new StringBuilder();
            for(String folder : compInfo.subdirectories)
                subdirectories.append(folder).append(";");
            response.append(subdirectories.toString());

            // Folders to not delete and files to not extract
            response.append(".");
            response.appendLine(".");

            // App name
            response.appendLine(metaXml.name);
            // Author
            response.appendLine(metaXml.coder == null ? meta.author() : metaXml.coder);
            // Version
            response.appendLine(metaXml.version);
            // Extracted size
            response.appendLine(compInfo.rawSize);
            // Short description
            String shortDesc = metaXml.shortDesc == null ? "No description provided." : metaXml.shortDesc;
            response.appendLine(shortDesc);
            // Long description
            String longDesc = metaXml.longDesc;
            if(longDesc != null)
            {
                longDesc = (longDescPrefix + longDesc).replace("\n", " ");
                if(longDesc.length() > 128)
                    longDesc = longDesc.substring(0, 128) + "...";
            }
            else
                longDesc = shortDesc;
            response.appendLine(longDesc);
        }

        // Finish
        if(currentCategory != null)
            response.appendLine("=" + currentCategory + "=");
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping("/repo_list.txt")
    public ResponseEntity<String> repoList(HttpServletRequest request)
    {
        HBBResponse response = new HBBResponse();
        RepositoryInfo info = index.getInfo();

        response.appendLine("1");
        response.appendLine(info.name());
        response.appendLine(request.getServerName());
        response.appendLine("/hbb/homebrew_browser/listv036.txt");
        response.appendLine("/hbb/");
        return ResponseEntity.ok(response.toString());
    }

    @GetMapping("/apps_check_new.php")
    public ResponseEntity<String> checkNewApps()
    {
        return ResponseEntity.ok("0 0");
    }

    // No-Op

    @GetMapping("/get_rating.php")
    public ResponseEntity<String> getRating()
    {
        return ResponseEntity.ok("5");
    }

    @GetMapping("/update_rating.php")
    public ResponseEntity<String> updateRating()
    {
        return ResponseEntity.ok("5");
    }
}
