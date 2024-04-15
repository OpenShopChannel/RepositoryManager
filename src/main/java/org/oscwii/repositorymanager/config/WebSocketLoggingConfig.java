package org.oscwii.repositorymanager.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

@Configuration
public class WebSocketLoggingConfig
{
    @Autowired
    private WebSocketMessageBrokerStats stats;

    @PostConstruct
    public void configure()
    {
        stats.setLoggingPeriod(0);
    }
}
