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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

public class User implements UserDetails
{
    private final int id;
    private final String username, passwordHash;

    public boolean enabled;
    private List<GrantedAuthority> authorities;
    private Role role;
    public String email;

    public User(boolean enabled, int id, String username, Role role, String email, String passwordHash)
    {
        this.enabled = enabled;
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        setRole(role);
    }

    public boolean hasAccess(String roleStr)
    {
        if(roleStr.isEmpty())
            return true;
        Role role = Role.from(roleStr);
        Assert.notNull(role, "Unknown role: " + roleStr);
        return hasAccess(role);
    }

    public boolean hasAccess(Role required)
    {
        return role.ordinal() >= required.ordinal();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return authorities;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public String getPassword()
    {
        return passwordHash;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
