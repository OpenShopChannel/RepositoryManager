/*
 * Copyright (c) 2023-2025 Open Shop Channel
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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "repository-manager.fetch")
public class FetchConfig
{
    private int timeout;
    private String userAgent;
    private Map<String, String> secretUserAgents;

    private String githubToken;
    private String itchioToken;

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    public Map<String, String> getSecretUserAgents()
    {
        return secretUserAgents;
    }

    public void setSecretUserAgents(Map<String, String> secretUserAgents)
    {
        this.secretUserAgents = secretUserAgents;
    }

    public String getGithubToken()
    {
        return githubToken;
    }

    public void setGithubToken(String githubToken)
    {
        this.githubToken = githubToken;
    }

    public String getItchioToken()
    {
        return itchioToken;
    }

    public void setItchioToken(String itchioToken)
    {
        this.itchioToken = itchioToken;
    }
}
