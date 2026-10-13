package org.opencdmp.service.updatecheck;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "update-check")
public class UpdateCheckProperties {

    private boolean enabled;
    private String url;
    private String installationUrl;
    private long intervalSeconds;
    private long timeoutSeconds;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInstallationUrl() {
        return installationUrl;
    }

    public void setInstallationUrl(String installationUrl) {
        this.installationUrl = installationUrl;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
