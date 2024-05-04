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

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.PrintWriter;
import java.io.StringWriter;

public class WebSocketAppender extends AbstractAppender
{
    private final SimpMessageSendingOperations webSocket;

    public WebSocketAppender(SimpMessageSendingOperations webSocket)
    {
        super("WebSocketAppender", null, null, true, Property.EMPTY_ARRAY);
        this.webSocket = webSocket;
    }

    @Override
    public void append(LogEvent event)
    {
        String level = event.getLevel().name().toLowerCase();
        String message = event.getMessage().getFormattedMessage();

        if(message.equals("** INDEX SUMMARY **") || message.startsWith("Loading manifest"))
            level = "title";
        else if(message.equals("Finished indexing application manifests"))
            level = "success";

        StringWriter exWriter = new StringWriter();
        PrintWriter exPrinter = new PrintWriter(exWriter);
        if(event.getThrown() != null)
        {
            event.getThrown().printStackTrace(exPrinter);
            exPrinter.flush();
            message += exWriter;
        }

        webSocket.convertAndSend("/logUpdate", new LogLine(message, level));
    }
}
