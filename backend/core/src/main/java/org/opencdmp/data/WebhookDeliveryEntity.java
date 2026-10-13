package org.opencdmp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.WebhookDeliveryStatus;
import org.opencdmp.commons.enums.WebhookEventType;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.WebhookDeliveryStatusConverter;
import org.opencdmp.data.converters.enums.WebhookEventTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * One webhook callback owed to (or delivered to) a subscription — a
 * webhook-specific outbox. Rows are written in the same transaction as the domain
 * change that caused them and drained by the delivery task with retries and
 * exponential backoff until Delivered or dead-lettered as Failed (redeliverable).
 */
@Entity
@Table(name = "\"WebhookDelivery\"")
public class WebhookDeliveryEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    public static final String _id = "id";

    @Column(name = "subscription", nullable = false)
    private UUID subscriptionId;

    public static final String _subscriptionId = "subscriptionId";

    @Column(name = "event_type", nullable = false)
    @Convert(converter = WebhookEventTypeConverter.class)
    private WebhookEventType eventType;

    public static final String _eventType = "eventType";

    @Column(name = "payload", nullable = false)
    private String payload;

    public static final String _payload = "payload";

    @Column(name = "status", nullable = false)
    @Convert(converter = WebhookDeliveryStatusConverter.class)
    private WebhookDeliveryStatus status;

    public static final String _status = "status";

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    public static final String _attempts = "attempts";

    @Column(name = "last_http_status", nullable = true)
    private Integer lastHttpStatus;

    public static final String _lastHttpStatus = "lastHttpStatus";

    @Column(name = "last_error", nullable = true)
    private String lastError;

    public static final String _lastError = "lastError";

    @Column(name = "next_retry_at", nullable = true)
    private Instant nextRetryAt;

    public static final String _nextRetryAt = "nextRetryAt";

    @Column(name = "delivered_at", nullable = true)
    private Instant deliveredAt;

    public static final String _deliveredAt = "deliveredAt";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    @Column(name = "is_active", nullable = false)
    @Convert(converter = IsActiveConverter.class)
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
