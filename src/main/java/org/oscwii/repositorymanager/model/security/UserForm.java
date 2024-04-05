package org.oscwii.repositorymanager.model.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserForm implements UserDetails
{
    private final String username, email, password;
    private final Role role;

    public UserForm(String username, String email, String password)
    {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = Role.GUEST;
    }

    public UserForm(String username, String email, Role role)
    {
        this.username = username;
        this.email = email;
        this.password = null;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return null;
    }

    @Override
    public String getPassword()
    {
        return password;
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

    public Role getRole()
    {
        return role;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return false;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return false;
    }

    @Override
    public boolean isEnabled()
    {
        return false;
    }
}
