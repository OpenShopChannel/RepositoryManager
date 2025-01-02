/*
 * Copyright (c) 2023-2025 Open Shop Channel
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

package org.oscwii.repositorymanager.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.oscwii.repositorymanager.controllers.admin.SecurityController.UserForm;
import org.oscwii.repositorymanager.database.dao.SettingsDAO;
import org.oscwii.repositorymanager.model.security.DummyUser;
import org.oscwii.repositorymanager.model.security.Role;
import org.oscwii.repositorymanager.security.annotations.Anyone;
import org.oscwii.repositorymanager.services.AuthService;
import org.oscwii.repositorymanager.validation.UserNotExists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Anyone
@Controller
@RequestMapping("/setup")
public class SetupController
{
    @Autowired
    private AuthService authService;
    @Autowired
    private SettingsDAO settingsDAO;

    @GetMapping
    public String setup(Model model)
    {
        if(settingsDAO.getSetting("setup_complete").isPresent())
            return "redirect:/admin";
        model.addAttribute("messages", Map.of());
        return "setup";
    }

    @PostMapping
    public Object setupAction(@Valid SetupForm form)
    {
        if(settingsDAO.getSetting("setup_complete").isPresent())
            return ResponseEntity.badRequest().body("Setup is complete. Erase settings table to re-run setup.");
        settingsDAO.insertSetting("git_url", form.gitUrl);
        DummyUser user = new DummyUser(form.admin.username(), form.admin.email(), form.admin.password(), Role.ADMINISTRATOR);
        authService.createUser(user);
        settingsDAO.insertSetting("setup_complete", "true");
        return "redirect:/admin";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleInvalidForm(Model model, MethodArgumentNotValidException e)
    {
        Map<String, String> errors = new HashMap<>();
        for(ObjectError error : e.getBindingResult().getAllErrors())
            errors.put(error.getDefaultMessage(), "danger");

        model.addAttribute("version", getVersion()).addAttribute("messages", errors);
        return "setup";
    }

    @ModelAttribute("version")
    public String getVersion()
    {
        return System.getProperty("repoman.release");
    }

    public record SetupForm(
            @NotBlank(message = "Git url is required!") String gitUrl,
            @UserNotExists UserForm admin)
    {}
}
