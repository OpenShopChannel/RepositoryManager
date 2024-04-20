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

    @NestedConfigurationProperty
    public DiscordConfig discordConfig;
    @NestedConfigurationProperty
    public FetchConfig fetchConfig;
    @NestedConfigurationProperty
    public MailConfig mailConfig;
    @NestedConfigurationProperty
    public SecurityConfig securityConfig;
    @NestedConfigurationProperty
    public ShopConfig shopConfig;

    @Autowired
    public RepoManConfig(DiscordConfig discordConfig, FetchConfig fetchConfig, MailConfig mailConfig,
                         SecurityConfig securityConfig, ShopConfig shopConfig)
    {
        this.discordConfig = discordConfig;
        this.fetchConfig = fetchConfig;
        this.mailConfig = mailConfig;
        this.securityConfig = securityConfig;
        this.shopConfig = shopConfig;
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
}
