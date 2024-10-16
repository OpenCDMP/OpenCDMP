package org.opencdmp.service.elastic;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elastic-query-helper")
public class ElasticQueryHelperServiceProperties {
    private boolean enableDbFallback;

    public boolean getEnableDbFallback() {
        return this.enableDbFallback;
    }

    public void setEnableDbFallback(boolean enableDbFallback) {
        this.enableDbFallback = enableDbFallback;
    }
}