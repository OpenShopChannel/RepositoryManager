package org.oscwii.repositorymanager.config.repoman;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "repository-manager.mail")
public record MailConfig(
        String senderAddress,
        String sendGridApiKey)
{
}
