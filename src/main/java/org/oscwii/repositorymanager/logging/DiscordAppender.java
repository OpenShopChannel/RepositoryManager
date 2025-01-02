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

package org.oscwii.repositorymanager.logging;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.message.ObjectMessage;
import org.oscwii.repositorymanager.factory.DiscordWebhookFactory;

public class DiscordAppender extends AbstractAppender
{
    private final WebhookClient webhook;

    public DiscordAppender(DiscordWebhookFactory factory)
    {
        super("DiscordAppender", null, null, true, Property.EMPTY_ARRAY);
        this.webhook = factory.logWebhook();
    }

    @Override
    public void append(LogEvent event)
    {
        if(!(event.getMessage() instanceof ObjectMessage objMessage))
            return;
        if(!(objMessage.getParameter() instanceof DiscordMessage logMessage))
            return;

        WebhookEmbed embed = logMessage.toEmbed();
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setUsername(event.getLevel().isMoreSpecificThan(Level.ERROR) ?
                        "Repository Manager: Error" : "Repository Manager")
                .setAvatarUrl(event.getLevel().isMoreSpecificThan(Level.ERROR) ?
                        ERROR_AVATAR : null)
                .build();
        webhook.send(message);
    }

    public static final String ERROR_AVATAR = "https://cdn.discordapp.com/avatars/1136222166749302864/ab15048a5c7b9a93d6ab42d47cf0a377.webp";
}
