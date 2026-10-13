package org.opencdmp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * A tenant-registered webhook endpoint: signed HTTP callbacks are delivered to the
 * url for the domain events listed in event_types (JSON array of WebhookEventType
 * values). The secret signs payloads (HMAC-SHA256) and is write-only through the
 * API — it is returned exactly once, on creation.
 */
@Entity
@Table(name = "\"WebhookSubscription\"")
public class WebhookSubscriptionEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    public static final String _id = "id";

    @Column(name = "label", nullable = false)
    private String label;

    public static final String _label = "label";

    @Column(name = "url", nullable = false)
    private String url;

    public static final String _url = "url";

    @Column(name = "secret", nullable = false)
    private String secret;

    public static final String _secret = "secret";

    @Column(name = "event_types", nullable = false)
    private String eventTypes;

    public static final String _eventTypes = "eventTypes";

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    public static final String _enabled = "enabled";

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(String eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
