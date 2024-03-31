package org.oscwii.repositorymanager.config.repoman;

import org.oscwii.repositorymanager.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "repository-manager")
public class RepoManConfig
{
    private String baseUrl;
    private String defaultPlatform;
    private Path repoDir;

    private boolean generateWSCBanner;
    private Path bannerGeneratorPath;

    @NestedConfigurationProperty
    public DiscordConfig discordConfig;
    @NestedConfigurationProperty
    public FetchConfig fetchConfig;

    @Autowired
    public RepoManConfig(DiscordConfig discordConfig, FetchConfig fetchConfig)
    {
        this.discordConfig = discordConfig;
        this.fetchConfig = fetchConfig;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
        FormatUtil.baseUrl = baseUrl;
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

    public boolean generateWSCBanner()
    {
        return generateWSCBanner;
    }

    public void setGenerateWSCBanner(boolean generateWSCBanner)
    {
        this.generateWSCBanner = generateWSCBanner;
    }

    public Path getBannerGeneratorPath()
    {
        return bannerGeneratorPath;
    }

    public void setBannerGeneratorPath(Path bannerGeneratorPath)
    {
        this.bannerGeneratorPath = bannerGeneratorPath;
    }
}
