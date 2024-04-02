package org.oscwii.repositorymanager.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.model.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping(path = "/admin", method = RequestMethod.GET)
public class AdminController
{
    @Autowired
    private RepositoryIndex index;

    @GetMapping
    public String home(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User("admin", "admin", "Administrator"))
                .addAttribute("repoInfo", index.getInfo())
                .addAttribute("applications", index.getContents().size())
                .addAttribute("request", request);
        return "admin/home";
    }

    @GetMapping("/debug")
    public String debug(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User("admin", "admin", "Administrator"))
                .addAttribute("request", request);
        return "admin/debug";
    }

    @GetMapping("/moderation")
    public String moderation(HttpServletRequest request, Model model)
    {
        model.addAttribute("currentUser", new User("admin", "admin", "Administrator"))
                .addAttribute("modEntries", List.of()) // TODO
                .addAttribute("request", request);
        return "admin/moderation";
    }
}
