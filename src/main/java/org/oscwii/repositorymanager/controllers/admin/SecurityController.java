package org.oscwii.repositorymanager.controllers.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.model.security.UserForm;
import org.oscwii.repositorymanager.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class SecurityController
{
    private final AuthService authService;
    private final RepoManConfig config;

    @Autowired
    public SecurityController(AuthService authService, RepoManConfig config)
    {
        this.authService = authService;
        this.config = config;
    }

    @GetMapping("/admin/login")
    public String login(HttpServletRequest request, Model model) throws ServletException
    {
        List<Map<String, String>> messages = new ArrayList<>();

        if(request.getParameter("logout") != null)
        {
            request.logout();
            messages.add(Map.of("Logged out.", "info"));
        }
        else if(request.getRemoteUser() != null)
           return "redirect:/admin";

        if(request.getParameter("error") != null)
            messages.add(Map.of("Incorrect username or password.", "danger"));

        model.addAttribute("messages", messages);
        return "login";
    }

    @GetMapping("/admin/register")
    public Object register(HttpServletRequest request, Model model)
    {
        if(!config.isAllowRegistration())
            return ResponseEntity.badRequest().body("Registration is disabled.");

        if(request.getRemoteUser() != null)
            return "redirect:/admin";

        model.addAttribute("messages", List.of());
        return "register";
    }

    @PostMapping(value = "/admin/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object registerAction(HttpServletRequest request, UserForm form, Model model)
    {
        if(!config.isAllowRegistration())
            return ResponseEntity.badRequest().body("Registration is disabled.");

        if(request.getRemoteUser() != null)
            return "redirect:/admin";

        List<Map<String, String>> messages = new ArrayList<>();
        String username = form.getUsername();
        String email = form.getEmail();
        String password = form.getPassword();

        if(username == null || email == null || password == null)
            messages.add(Map.of("All fields are required.", "danger"));
        else if(authService.userExists(username))
            messages.add(Map.of("Username already in use.", "danger"));
        else if(authService.isEmailInUse(email))
            messages.add(Map.of("Email already in use.", "danger"));
        else
        {
            authService.createUser(form);
            return "redirect:/admin/login";
        }

        model.addAttribute("messages", messages);
        return "register";
    }
}
