package org.oscwii.repositorymanager.config.repoman;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "repository-manager.discord")
public class DiscordConfig
{
    private boolean enable;
    private String catalogWebhookUrl, logWebhookUrl;

    public boolean isLoggingEnabled()
    {
        return enable;
    }

    public void setEnableLogging(boolean enableLogging)
    {
        this.enable = enableLogging;
    }

    public String getCatalogWebhookUrl()
    {
        return catalogWebhookUrl;
    }

    public void setCatalogWebhookUrl(String catalogWebhookUrl)
    {
        this.catalogWebhookUrl = catalogWebhookUrl;
    }

    public String getLogWebhookUrl()
    {
        return logWebhookUrl;
    }

    public void setLogWebhookUrl(String logWebhookUrl)
    {
        this.logWebhookUrl = logWebhookUrl;
    }
}
