package org.opencdmp.model.webhook;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.WebhookDeliveryStatus;
import org.opencdmp.commons.enums.WebhookEventType;

import java.time.Instant;
import java.util.UUID;

public class WebhookDelivery {

    private UUID id;
    public static final String _id = "id";

    private UUID subscriptionId;
    public static final String _subscriptionId = "subscriptionId";

    private WebhookEventType eventType;
    public static final String _eventType = "eventType";

    private String payload;
    public static final String _payload = "payload";

    private WebhookDeliveryStatus status;
    public static final String _status = "status";

    private Integer attempts;
    public static final String _attempts = "attempts";

    private Integer lastHttpStatus;
    public static final String _lastHttpStatus = "lastHttpStatus";

    private String lastError;
    public static final String _lastError = "lastError";

    private Instant nextRetryAt;
    public static final String _nextRetryAt = "nextRetryAt";

    private Instant deliveredAt;
    public static final String _deliveredAt = "deliveredAt";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public WebhookEventType getEventType() {
        return eventType;
    }

    public void setEventType(WebhookEventType eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public WebhookDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(WebhookDeliveryStatus status) {
        this.status = status;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Integer getLastHttpStatus() {
        return lastHttpStatus;
    }

    public void setLastHttpStatus(Integer lastHttpStatus) {
        this.lastHttpStatus = lastHttpStatus;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }
}
