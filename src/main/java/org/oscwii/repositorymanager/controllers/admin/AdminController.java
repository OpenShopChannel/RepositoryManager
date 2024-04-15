package org.oscwii.repositorymanager.controllers.admin;

import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.RepositorySource;
import org.oscwii.repositorymanager.database.dao.SettingsDAO;
import org.oscwii.repositorymanager.model.security.Role;
import org.oscwii.repositorymanager.security.annotations.RequiredRole;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.oscwii.repositorymanager.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredRole(Role.ADMINISTRATOR)
public class AdminController extends BaseAdminController
{
    private final RepositoryIndex index;
    private final RepositorySource repoSource;
    private final SettingsDAO settingsDao;
    private final SourceRegistry sourceRegistry;
    private final TaskScheduler scheduler;

    private boolean runIndex = false;

    @Autowired
    public AdminController(RepositoryIndex index, RepositorySource repoSource, SettingsDAO settingsDao,
                           SourceRegistry sourceRegistry, @Qualifier("taskScheduler") TaskScheduler scheduler)
    {
        this.index = index;
        this.repoSource = repoSource;
        this.settingsDao = settingsDao;
        this.sourceRegistry = sourceRegistry;
        this.scheduler = scheduler;
    }

    @GetMapping
    @RequiredRole
    public String home(Model model)
    {
        model.addAttribute("repoInfo", index.getInfo())
                .addAttribute("applications", index.getContents().size());
        return "admin/home";
    }

    @GetMapping("/action/{action}")
    public String action(@PathVariable String action, RedirectAttributes attributes)
    {
        //noinspection SwitchStatementWithTooFewBranches
        switch(action)
        {
            case "update":
                this.runIndex = true;
                attributes.addFlashAttribute("message", "info:Scheduled immediate index update.");
                return "redirect:/admin/status";
            default:
                return "redirect:/admin";
        }
    }

    @GetMapping("/debug")
    public String debug()
    {
        return "admin/debug";
    }

    @GetMapping("/debug/{action}")
    public String debugAction(@PathVariable String action, RedirectAttributes attributes)
    {
        switch(action)
        {
            case "init_repo":
                attributes.addFlashAttribute("message", "success:Successfully initialized repository");
                repoSource.initialize();
                break;
            case "pull_repo":
                attributes.addFlashAttribute("message", "success:Successfully pulled repository");
                repoSource.pull();
                break;
            case "update_index":
                attributes.addFlashAttribute("message", "success:Successfully updated index");
                scheduler.schedule(() -> index.index(true), Instant.now());
        }

        return "admin/debug";
    }

    @GetMapping("/status")
    public String taskStatus()
    {
        if(runIndex)
        {
            this.runIndex = false;
            scheduler.schedule(index::updateIndex, Instant.now().plusSeconds(5));
        }

        return "admin/task_status";
    }

    @GetMapping("/moderation")
    @RequiredRole(Role.MODERATOR)
    public String moderation(Model model)
    {
        model.addAttribute("modEntries", modDao.getAllEntries());
        return "admin/moderation";
    }

    @GetMapping("/apps")
    @RequiredRole
    public String apps(Model model)
    {
        model.addAttribute("contents", index.getContents());
        return "admin/apps";
    }

    @GetMapping("/sources")
    @RequiredRole(Role.MODERATOR)
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
    public String settings(Model model)
    {
        model.addAttribute("settings", settingsDao.getAllSettings());
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam MultiValueMap<String, String> form)
    {
        for(Map.Entry<String, String> entry : form.toSingleValueMap().entrySet())
            settingsDao.insertSetting(entry.getKey(), entry.getValue());
        return "redirect:settings";
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
