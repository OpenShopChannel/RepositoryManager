/*
 * Copyright (c) 2023-2024 Open Shop Channel
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
