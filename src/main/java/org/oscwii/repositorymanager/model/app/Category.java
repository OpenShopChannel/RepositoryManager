package org.oscwii.repositorymanager.model.app;

public record Category(String name, String displayName, String singular, String plural)
{
    @Override
    public String toString()
    {
        return displayName;
    }
}
