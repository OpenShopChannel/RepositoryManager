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

package org.oscwii.repositorymanager.model.security;

public enum Role
{
    GUEST("Guest"),
    MODERATOR("Moderator"),
    ADMINISTRATOR("Administrator");

    private final String displayName;

    Role(String displayName)
    {
        this.displayName = displayName;
    }

    public static Role from(String roleStr)
    {
        for(Role role : values())
        {
            if(role.name().equalsIgnoreCase(roleStr))
                return role;
        }

        return null;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
