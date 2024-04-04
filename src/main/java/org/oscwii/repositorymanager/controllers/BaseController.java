package org.oscwii.repositorymanager.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class BaseController
{
    @Autowired
    protected AuthService authService;

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
