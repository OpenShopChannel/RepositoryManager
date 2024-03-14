package org.oscwii.repositorymanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "repository-manager")
public class RepoManConfig
{
    private String defaultPlatform;
    private Path repoDir;

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
