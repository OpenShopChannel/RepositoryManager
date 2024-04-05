package org.oscwii.repositorymanager.model.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
