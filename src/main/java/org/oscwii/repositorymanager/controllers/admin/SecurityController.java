package org.oscwii.repositorymanager.controllers.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.oscwii.repositorymanager.config.repoman.RepoManSecurityConfig;
import org.oscwii.repositorymanager.model.security.DummyUser;
import org.oscwii.repositorymanager.model.security.PasswordToken;
import org.oscwii.repositorymanager.model.security.Role;
import org.oscwii.repositorymanager.security.annotations.Anyone;
import org.oscwii.repositorymanager.services.AuthService;
import org.oscwii.repositorymanager.validation.UserNotExists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION;

@Anyone
@Controller
public class SecurityController
{
    private final AuthService authService;
    private final RepoManSecurityConfig config;

    @Autowired
    public SecurityController(AuthService authService, RepoManSecurityConfig config)
    {
        this.authService = authService;
        this.config = config;
    }

    @GetMapping("/admin/login")
    public String login(HttpServletRequest request, Model model, @ModelAttribute("message") String message) throws ServletException
    {
        Map<String, String> messages = new HashMap<>();
        if(!message.isEmpty())
        {
            String[] split = message.split(":", 2);
            messages.put(split[1], split[0]);
        }

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
        if(!config.allowRegistration())
            return ResponseEntity.badRequest().body("Registration is disabled.");
        if(request.getRemoteUser() != null)
            return "redirect:/admin";

        model.addAttribute("messages", Map.of());
        return "register";
    }

    @PostMapping("/admin/register")
    public Object registerAction(HttpServletRequest request, @Valid @UserNotExists UserForm form)
    {
        if(!config.allowRegistration())
            return ResponseEntity.badRequest().body("Registration is disabled.");
        if(request.getRemoteUser() != null)
            return "redirect:/admin";
        DummyUser user = new DummyUser(form.username(), form.email(), form.password(), Role.GUEST);
        authService.createUser(user);
        return "redirect:/admin/login";
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public Object handleInvalidForm(Model model, HandlerMethodValidationException e)
    {
        if(!config.allowRegistration())
            return ResponseEntity.badRequest().body("Registration is disabled.");
        Map<String, String> errors = new HashMap<>();
        for(MessageSourceResolvable error : e.getAllErrors())
            errors.put(error.getDefaultMessage(), "danger");

        model.addAttribute("messages", errors);
        return "register";
    }

    @GetMapping("/admin/reset-password")
    public String resetPassword(@RequestParam String token, RedirectAttributes attributes)
    {
        try
        {
            authService.validatePasswordToken(token);
        }
        catch(IllegalArgumentException e)
        {
            attributes.addFlashAttribute("message", "danger:" + e.getMessage());
            return "redirect:/admin/login";
        }

        return "password-reset";
    }

    @PostMapping(value = "/admin/reset-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object resetPasswordAction(@RequestParam MultiValueMap<String, String> form, @RequestParam("token") String tokenStr, RedirectAttributes attributes)
    {
        String password = form.getFirst("password");
        if(!StringUtils.hasText(password) || !StringUtils.hasText(tokenStr))
            return ResponseEntity.badRequest().build();

        PasswordToken token = authService.getPasswordToken(tokenStr);
        if(token == null)
            return ResponseEntity.notFound().build();

        if(token.isExpired())
            return ResponseEntity.badRequest().body("This password reset link has expired.");

        authService.changePassword(token.getId(), password);
        attributes.addFlashAttribute("message", "info:Your password has been reset. You may login now.");
        return "redirect:/admin/login";
    }

    private String getLoginErrorMessage(HttpServletRequest request)
    {
        String message = "Incorrect username or password";
        Object ex = request.getSession().getAttribute(AUTHENTICATION_EXCEPTION);

        if(ex instanceof AuthenticationException authEx)
        {
            if(StringUtils.hasText(authEx.getMessage()) && !authEx.getMessage().equals("Bad credentials"))
                message = authEx.getMessage();
        }

        return message;
    }

    public record UserForm(
            @NotBlank(message = "Username is required!") String username,
            @NotBlank(message = "Email is required!") String email,
            @NotBlank(message = "Password is required!") String password)
    {}
}
