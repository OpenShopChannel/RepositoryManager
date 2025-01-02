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

package org.oscwii.repositorymanager.security;

import org.aopalliance.intercept.MethodInvocation;
import org.oscwii.repositorymanager.model.security.Role;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.security.annotations.Anyone;
import org.oscwii.repositorymanager.security.annotations.RequiredRole;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.DIRECT;
import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.SUPERCLASS;

@Component
public class RepoManAuthorizationManager implements AuthorizationManager<MethodInvocation>
{
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation mi)
    {
        boolean denied = false;
        Method method = mi.getMethod();
        MergedAnnotations annotations = MergedAnnotations.from(method, DIRECT);
        MergedAnnotations classAnnotations = MergedAnnotations.from(method.getDeclaringClass(), SUPERCLASS);

        // By default, controllers should be denied, unless annotated with @Anyone
        if(classAnnotations.isPresent(Controller.class))
        {
            MergedAnnotation<Anyone> anyone = annotations.get(Anyone.class);
            if(!anyone.isPresent())
                anyone = classAnnotations.get(Anyone.class);
            denied = !(anyone.isPresent() && anyone.synthesize().value());
        }

        MergedAnnotation<RequiredRole> annotation = annotations.get(RequiredRole.class);
        if(!annotation.isPresent())
        {
            annotation = classAnnotations.get(RequiredRole.class);
            if(!annotation.isPresent())
                return denied ? new AuthorizationDecision(false) : null;
        }

        Role requiredRole = annotation.synthesize().value();
        Authentication auth = authentication.get();
        if(!(auth.getPrincipal() instanceof User user))
            throw new AccessDeniedException("Not Authenticated");

        return new AuthorizationDecision(user.hasAccess(requiredRole));
    }
}
