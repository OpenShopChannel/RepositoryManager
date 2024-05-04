/*
 * Copyright (c) 2023-2024 Open Shop Channel
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

package org.oscwii.repositorymanager.controllers.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.controllers.RepoManController;
import org.oscwii.repositorymanager.database.dao.ModerationDAO;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.security.annotations.RequiredRole;
import org.oscwii.repositorymanager.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@RequiredRole // GUEST = Logged in
public abstract class BaseAdminController extends RepoManController
{
    @Autowired
    protected AuthService authService;
    @Autowired
    protected ModerationDAO modDao;
    @Autowired
    protected RepoManConfig config;

    @ModelAttribute("request")
    protected HttpServletRequest getRequest(HttpServletRequest request)
    {
        return request;
    }

    @ModelAttribute("pendingModeration")
    protected long getPendingModEntries()
    {
        return modDao.getPendingEntries();
    }

    @ModelAttribute("currentUser")
    protected User getUser()
    {
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User user)
            return user;
        throw new IllegalStateException("User is not authenticated!");
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
