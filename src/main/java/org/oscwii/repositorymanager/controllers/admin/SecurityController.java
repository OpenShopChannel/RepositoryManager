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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION;

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
        Map<String, String> messages = new HashMap<>();

        if(request.getParameter("logout") != null)
        {
            request.logout();
            messages.put("Logged out.", "info");
        }
        else if(request.getRemoteUser() != null)
           return "redirect:/admin";

        if(request.getParameter("error") != null)
            messages.put(getLoginErrorMessage(request), "danger");

        model.addAttribute("message", messages);
        return "login";
    }

    @GetMapping("/admin/register")
    public Object register(HttpServletRequest request, Model model)
    {
        if(!config.isAllowRegistration())
            return ResponseEntity.badRequest().body("Registration is disabled.");

        if(request.getRemoteUser() != null)
            return "redirect:/admin";

        model.addAttribute("message", Map.of());
        return "register";
    }

    @PostMapping(value = "/admin/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object registerAction(HttpServletRequest request, UserForm form, Model model)
    {
        if(!config.isAllowRegistration())
            return ResponseEntity.badRequest().body("Registration is disabled.");

        if(request.getRemoteUser() != null)
            return "redirect:/admin";

        Map<String, String> messages = new HashMap<>();
        String username = form.getUsername();
        String email = form.getEmail();
        String password = form.getPassword();

        if(username == null || email == null || password == null)
            messages.put("All fields are required.", "danger");
        else if(authService.userExists(username))
            messages.put("Username already in use.", "danger");
        else if(authService.isEmailInUse(email))
            messages.put("Email already in use.", "danger");
        else
        {
            authService.createUser(form);
            return "redirect:/admin/login";
        }

        model.addAttribute("message", messages);
        return "register";
    }

    private String getLoginErrorMessage(HttpServletRequest request)
    {
        String message = "Incorrect username or password.";
        Object ex = request.getAttribute(AUTHENTICATION_EXCEPTION);

        if(ex instanceof AuthenticationException authEx)
        {
            if(StringUtils.hasText(authEx.getMessage()))
                message = authEx.getMessage();
        }

        return message;
    }
}
