package org.oscwii.repositorymanager.config.repoman;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "repository-manager.discord")
public record DiscordConfig(boolean enableLogging, String catalogWebhookUrl,
                            String logWebhookUrl, String modWebhookUrl)
{
    public boolean isLoggingEnabled()
    {
        return enableLogging;
    }
}
