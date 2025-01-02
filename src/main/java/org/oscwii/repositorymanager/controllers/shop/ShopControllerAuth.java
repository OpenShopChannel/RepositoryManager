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

package org.oscwii.repositorymanager.controllers.shop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.oscwii.repositorymanager.config.repoman.ShopConfig;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class ShopControllerAuth implements HandlerInterceptor
{
    private final ShopConfig config;

    public ShopControllerAuth(ShopConfig config)
    {
        this.config = config;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String authorization = request.getHeader("Authorization");
        if(authorization == null || authorization.isBlank())
        {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "No access token provided");
            return false;
        }

        if(!authorization.equals(config.accessToken()))
        {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access token");
            return false;
        }

        return true;
    }
}
