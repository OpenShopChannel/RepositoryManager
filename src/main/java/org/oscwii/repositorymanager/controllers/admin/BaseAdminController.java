package org.oscwii.repositorymanager.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.security.RequiredRole;
import org.oscwii.repositorymanager.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

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
}
