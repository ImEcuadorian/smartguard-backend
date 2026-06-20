package io.github.imecuadorian.smartguardbackend.realtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String endpoint;
    private final String topicPrefix;
    private final String appPrefix;
    private final String[] allowedOrigins;

    public WebSocketConfig(
            @Value("${smartguard.websocket.endpoint:/ws}") String endpoint,
            @Value("${smartguard.websocket.topic-prefix:/topic}") String topicPrefix,
            @Value("${smartguard.websocket.app-prefix:/app}") String appPrefix,
            @Value("${smartguard.websocket.allowed-origins:http://localhost:3000,http://localhost:19006,http://10.0.2.2:8080}")
            String allowedOrigins
    ) {
        this.endpoint = endpoint;
        this.topicPrefix = topicPrefix;
        this.appPrefix = appPrefix;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(topicPrefix);
        registry.setApplicationDestinationPrefixes(appPrefix);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint).setAllowedOrigins(allowedOrigins);
    }
}
