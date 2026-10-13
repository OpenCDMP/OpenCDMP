package org.opencdmp.model.webhook;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.WebhookEventType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WebhookSubscription {

    private UUID id;
    public static final String _id = "id";

    private String label;
    public static final String _label = "label";

    private String url;
    public static final String _url = "url";

    /**
     * Populated only in the response of the creating persist call — the builder
     * never emits it, so reads always return null.
     */
    private String secret;
    public static final String _secret = "secret";

    private List<WebhookEventType> eventTypes;
    public static final String _eventTypes = "eventTypes";

    private Boolean enabled;
    public static final String _enabled = "enabled";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private String hash;
    public static final String _hash = "hash";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

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

    public List<WebhookEventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<WebhookEventType> eventTypes) {
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
