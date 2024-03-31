package org.oscwii.repositorymanager.logging;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;

public class DiscordMessage
{
    private final String message;
    private final String title;
    private final String description;

    public DiscordMessage(String message, String description)
    {
        this.message = message;
        this.title = null;
        this.description = description;
    }

    public DiscordMessage(String message, String title, String description)
    {
        this.message = message;
        this.title = title;
        this.description = description;
    }

    public WebhookEmbed toEmbed()
    {
        return new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle(title == null ? message : title, null))
                .setDescription(description)
                .build();
    }

    @Override
    public String toString()
    {
        return message;
    }
}
