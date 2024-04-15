package org.oscwii.repositorymanager.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

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

        webSocket.convertAndSend("/logUpdate", new LogLine(message, level));
    }
}