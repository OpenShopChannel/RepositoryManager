package org.oscwii.repositorymanager.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.model.security.User;
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
public class AdminController
{
    @Autowired
    private RepositoryIndex index;
    @Autowired
    private SourceRegistry sourceRegistry;

    @GetMapping
    public String home(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User(1, "admin", "admin", "Administrator"))
                .addAttribute("repoInfo", index.getInfo())
                .addAttribute("applications", index.getContents().size())
                .addAttribute("request", request);
        return "admin/home";
    }

    @GetMapping("/debug")
    public String debug(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User(1, "admin", "admin", "Administrator"))
                .addAttribute("request", request);
        return "admin/debug";
    }

    @GetMapping("/moderation")
    public String moderation(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User(1, "admin", "admin", "Administrator"))
                .addAttribute("modEntries", List.of()) // TODO
                .addAttribute("request", request);
        return "admin/moderation";
    }

    @GetMapping("/apps")
    public String apps(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User(1, "admin", "admin", "Administrator"))
                .addAttribute("contents", index.getContents())
                .addAttribute("request", request);
        return "admin/apps";
    }

    @GetMapping("/users")
    public String users(HttpServletRequest request, Model model)
    {
        User user = new User(1, "admin", "admin", "Administrator");
        model.addAttribute("currentUser", user)
                .addAttribute("users", List.of(user))
                .addAttribute("request", request);
        return "admin/users";
    }

    @GetMapping("/sources")
    public String sources(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User(1, "admin", "admin", "Administrator"))
                .addAttribute("sources", sourceRegistry.getSources())
                .addAttribute("request", request);
        return "admin/sources";
    }

    @GetMapping("/logs")
    public String logs(HttpServletRequest request, Model model) throws IOException
    {
        model.addAttribute("currentUser", new User(1, "admin", "admin", "Administrator"))
                .addAttribute("logs", getLogFiles())
                .addAttribute("request", request);
        return "admin/logs";
    }

    @GetMapping("/settings")
    public String settings(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User(1, "admin", "admin", "Administrator"))
                .addAttribute("request", request);
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
