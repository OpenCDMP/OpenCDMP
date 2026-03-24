package org.opencdmp.websocket;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {

    private String allowedOrigins;

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}

