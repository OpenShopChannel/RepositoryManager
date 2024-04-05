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
