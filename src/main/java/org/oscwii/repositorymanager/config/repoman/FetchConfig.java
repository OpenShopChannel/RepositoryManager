package org.oscwii.repositorymanager.config.repoman;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "repository-manager.fetch")
public class FetchConfig
{
    private String userAgent;
    private Map<String, String> secretUserAgents;

    private String githubToken;
    private String itchioToken;

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
