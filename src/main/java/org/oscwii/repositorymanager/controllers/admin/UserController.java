package org.oscwii.repositorymanager.controllers.admin;

import org.oscwii.repositorymanager.config.repoman.SecurityConfig;
import org.oscwii.repositorymanager.model.security.DummyUser;
import org.oscwii.repositorymanager.model.security.Role;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.security.annotations.RequiredRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredRole(Role.ADMINISTRATOR)
@RequestMapping(path = "/admin/users")
public class UserController extends BaseAdminController
{
    @Autowired
    private SecurityConfig securityConfig;

    @GetMapping
    public String users(Model model)
    {
        model.addAttribute("users", authService.getUsers());
        return "admin/users";
    }

    @GetMapping("/new")
    public String create(Model model)
    {
        model.addAttribute("messages", Map.of());
        return "admin/user/create";
    }

    @PostMapping("/new")
    public Object createAction(@RequestParam MultiValueMap<String, String> form, Model model, RedirectAttributes attributes)
    {
        String username = form.getFirst("username");
        String email = form.getFirst("email");
        Role role = Role.from(form.getFirst("role"));
        if(username == null || email == null || role == null)
            return ResponseEntity.badRequest().build();

        Map<String, String> messages = new HashMap<>();
        DummyUser user = new DummyUser(username, email, null, role);

        try
        {
            authService.createUser(user);
            attributes.addFlashAttribute("message", "success:User created successfully");
            return "redirect:/admin/users";
        }
        catch(IllegalArgumentException e)
        {messages.put(e.getMessage(), "danger");}
        catch(Exception e)
        {messages.put("Failed to create user: " + e.getMessage(), "danger");}

        model.addAttribute("messages", messages);
        return "admin/user/create";
    }

    @PostMapping("/delete/{id}")
    public Object delete(@PathVariable int id, RedirectAttributes attributes)
    {
        User user = authService.getUser(id);
        if(user == null)
            return ResponseEntity.notFound().build();
        if(user.getId() == getUser().getId())
            return ResponseEntity.badRequest().build();

        if(securityConfig.protectedUsers().contains(user.getId()))
        {
            attributes.addFlashAttribute("message", "danger:Cannot delete protected user!");
            return "redirect:/admin/users";
        }

        authService.deleteUser(user.getUsername());
        attributes.addFlashAttribute("message", "success:User deleted successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/view/{id}")
    public String details(@PathVariable int id, Model model, RedirectAttributes attributes)
    {
        User user = authService.getUser(id);
        if(user == null)
        {
            attributes.addFlashAttribute("message", "danger:Unknown user");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user);
        return "admin/user/details";
    }

    @PostMapping("/view/{id}")
    public Object modify(@PathVariable int id, @RequestParam MultiValueMap<String, String> form, Model model)
    {
        User user = authService.getUser(id);
        if(user == null)
            return ResponseEntity.notFound().build();

        if(form.containsKey("id"))
        {
            String email = form.getFirst("email");
            String enabledStr = form.getFirst("enabled");
            if(email == null)
                return ResponseEntity.badRequest().build();
            user.setEmail(email);
            user.setEnabled("on".equals(enabledStr));
        }
        else if(form.containsKey("role"))
        {
            Role role = Role.from(form.getFirst("role"));
            if(role == null)
                return ResponseEntity.badRequest().build();
            user.setRole(role);
        }

        authService.updateUser(user);
        model.addAttribute("user", authService.getUser(id))
                .addAttribute("messages", Map.of("User updated successfully", "primary"));
        return "admin/user/details";
    }

    @PostMapping("/reset-password/{id}")
    public Object resetPassword(@PathVariable int id, RedirectAttributes attributes)
    {
        User user = authService.getUser(id);
        if(user == null)
            return ResponseEntity.notFound().build();

        authService.requestPasswordReset(user);
        attributes.addFlashAttribute("message", "success:User has been emailed a password reset link");
        return "redirect:/admin/users/view/" + id;
    }
}
