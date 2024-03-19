package org.oscwii.repositorymanager.config.repoman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "repository-manager")
public class RepoManConfig
{
    private String defaultPlatform;
    private Path repoDir;

    @NestedConfigurationProperty
    public FetchConfig fetchConfig;

    @Autowired
    public RepoManConfig(FetchConfig fetchConfig)
    {
        this.fetchConfig = fetchConfig;
    }

    public String getDefaultPlatform()
    {
        return defaultPlatform;
    }

    public void setDefaultPlatform(String defaultPlatform)
    {
        this.defaultPlatform = defaultPlatform;
    }

    public Path getRepoDir()
    {
        return repoDir;
    }

    public void setRepoDir(Path repoDir)
    {
        this.repoDir = repoDir;
    }
}
