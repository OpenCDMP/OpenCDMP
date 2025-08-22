package org.opencdmp.integrationevent;

import gr.cite.queueinbox.entity.QueueInboxStatus;
import gr.cite.queueoutbox.entity.QueueOutboxNotifyStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "queue.task.cleanup")
public class CleanupTaskProperties {

    private boolean enable;
    private int intervalSeconds;
    private int startTimeSeconds;
    private int batchSize;
    private Integer retentionDays;
    private List<QueueInboxStatus> queueInboxStatus;
    private List<QueueOutboxNotifyStatus> queueOutboxStatus;

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public int getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setStartTimeSeconds(int startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public List<QueueInboxStatus> getQueueInboxStatus() {
        return queueInboxStatus;
    }

    public void setQueueInboxStatus(List<QueueInboxStatus> queueInboxStatus) {
        this.queueInboxStatus = queueInboxStatus;
    }

    public List<QueueOutboxNotifyStatus> getQueueOutboxStatus() {
        return queueOutboxStatus;
    }

    public void setQueueOutboxStatus(List<QueueOutboxNotifyStatus> queueOutboxStatus) {
        this.queueOutboxStatus = queueOutboxStatus;
    }
}
