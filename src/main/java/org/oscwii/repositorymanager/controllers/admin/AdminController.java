package org.oscwii.repositorymanager.controllers.admin;

import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.oscwii.repositorymanager.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(path = "/admin", method = RequestMethod.GET)
public class AdminController extends BaseAdminController
{
    @Autowired
    private RepositoryIndex index;
    @Autowired
    private SourceRegistry sourceRegistry;

    @GetMapping
    public String home(Model model)
    {
        model.addAttribute("repoInfo", index.getInfo())
                .addAttribute("applications", index.getContents().size());
        return "admin/home";
    }

    @GetMapping("/debug")
    public String debug()
    {
        return "admin/debug";
    }

    @GetMapping("/moderation")
    public String moderation(Model model)
    {
        model.addAttribute("modEntries", List.of()); // TODO
        return "admin/moderation";
    }

    @GetMapping("/apps")
    public String apps(Model model)
    {
        model.addAttribute("contents", index.getContents());
        return "admin/apps";
    }

    @GetMapping("/sources")
    public String sources(Model model)
    {
        model.addAttribute("sources", sourceRegistry.getSources());
        return "admin/sources";
    }

    @GetMapping("/logs")
    public String logs(Model model) throws IOException
    {
        model.addAttribute("logs", getLogFiles());
        return "admin/logs";
    }

    @GetMapping("/settings")
    public String settings()
    {
        return "admin/settings";
    }

    @GetMapping("/log/{log}")
    public ResponseEntity<Resource> getLog(@PathVariable String log)
    {
        return FileUtil.getContent(Path.of("logs", log));
    }

    private boolean isLog(Path path)
    {
        return path.toFile().getName().endsWith(".log");
    }

    private List<LogFile> getLogFiles() throws IOException
    {
        List<LogFile> logs = new ArrayList<>();
        List<File> logFiles = new ArrayList<>(FileUtil.getFiles(Path.of("logs"), 1, this::isLog));
        logFiles.sort(Comparator.comparing(File::lastModified).reversed());

        for(File file : logFiles)
        {
            List<String> lines = Files.readAllLines(file.toPath());
            if(lines.isEmpty()) // latest.log
                continue;
            var attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            int errors = (int) lines.stream().filter(line -> line.contains("[ERROR]")).count();
            String creationDate = FormatUtil.formatDate(new Date(attr.creationTime().toMillis()));
            logs.add(new LogFile(file.getName(), lines.size(), errors, creationDate));
        }
        return logs;
    }

    public record LogFile(String name, int lines, int errors, String creationDate) {}
}
