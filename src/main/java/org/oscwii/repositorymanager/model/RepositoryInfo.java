package org.oscwii.repositorymanager.model;

public record RepositoryInfo(String name, String provider, String description)
{
    public static RepositoryInfo DEFAULT = new RepositoryInfo("No repository configured",
                "Open Shop Channel",
                "Please finish the setup process");
}
