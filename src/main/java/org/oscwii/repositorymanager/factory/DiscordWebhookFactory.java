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
