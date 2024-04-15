package org.oscwii.repositorymanager.factory;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import okhttp3.OkHttpClient;
import org.oscwii.repositorymanager.config.repoman.DiscordConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiscordWebhookFactory
{
    private final DiscordConfig config;
    private final OkHttpClient httpClient;

    @Autowired
    public DiscordWebhookFactory(DiscordConfig config, OkHttpClient httpClient)
    {
        this.config = config;
        this.httpClient = httpClient;
    }

    public WebhookClient catalogWebhook()
    {
        if(!config.isLoggingEnabled())
            return null;
        return createWebhook(config.catalogWebhookUrl());
    }

    public WebhookClient modWebhook()
    {
        if(!config.isLoggingEnabled())
            return null;
        return createWebhook(config.modWebhookUrl());
    }

    public WebhookClient logWebhook()
    {
        if(!config.isLoggingEnabled())
            return null;
        return createWebhook(config.logWebhookUrl());
    }

    private WebhookClient createWebhook(String url)
    {
        return new WebhookClientBuilder(url)
                .setHttpClient(httpClient)
                .setAllowedMentions(AllowedMentions.none())
                .setWait(false)
                .build();
    }
}
