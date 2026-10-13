package org.opencdmp.service.webhook;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webhook")
public class WebhookProperties {

    private boolean enabled;
    private int maxAttempts;
    private long timeoutSeconds;
    private boolean denyPrivateAddresses;
    private TaskProperties task;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean getDenyPrivateAddresses() {
        return denyPrivateAddresses;
    }

    public void setDenyPrivateAddresses(boolean denyPrivateAddresses) {
        this.denyPrivateAddresses = denyPrivateAddresses;
    }

    public TaskProperties getTask() {
        return task;
    }

    public void setTask(TaskProperties task) {
        this.task = task;
    }

    public static class TaskProperties {
        private boolean enable;
        private long intervalSeconds;
        private int batchSize;

        public boolean getEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public long getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(long intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }
}
