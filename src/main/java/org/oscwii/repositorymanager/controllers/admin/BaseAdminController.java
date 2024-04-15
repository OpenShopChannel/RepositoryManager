package org.oscwii.repositorymanager.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.security.annotations.RequiredRole;
import org.oscwii.repositorymanager.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@RequiredRole // GUEST = Logged in
public abstract class BaseAdminController extends RepoManController
{
    @Autowired
    protected AuthService authService;
    @Autowired
    protected RepoManConfig config;

    @ModelAttribute("request")
    protected HttpServletRequest getRequest(HttpServletRequest request)
    {
        return request;
    }

    @ModelAttribute("currentUser")
    protected User getUser(HttpServletRequest request)
    {
        String username = request.getRemoteUser();
        if(username == null)
            return null;

        return (User) authService.loadUserByUsername(username);
    }

    @ModelAttribute("messages")
    protected Map<String, String> getMessages(@ModelAttribute("message") String message)
    {
        if(!message.isEmpty())
        {
            String[] split = message.split(":", 2);
            return Map.of(split[1], split[0]);
        }
        else
            return Map.of();
    }
}
