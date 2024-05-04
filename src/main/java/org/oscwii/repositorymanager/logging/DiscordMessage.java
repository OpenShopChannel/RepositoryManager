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
