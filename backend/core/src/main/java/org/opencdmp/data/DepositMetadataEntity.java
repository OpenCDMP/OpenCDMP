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
 * Persisted deposit-preview overrides for a plan/repository pair: the values a
 * user confirmed in the deposit preview dialog, together with the mapped base
 * values they replaced. The payload is an opaque JSON document interpreted only
 * by the deposit plugin (field codes are plugin-defined); the core stores and
 * forwards it so overrides survive failed deposits and prefill re-deposits of
 * later plan versions.
 */
@Entity
@Table(name = "\"DepositMetadata\"")
public class DepositMetadataEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    public static final String _id = "id";

    @Column(name = "plan", nullable = false)
    private UUID planId;

    public static final String _planId = "planId";

    @Column(name = "repository_id", nullable = false)
    private String repositoryId;

    public static final String _repositoryId = "repositoryId";

    @Column(name = "payload", nullable = false)
    private String payload;

    public static final String _payload = "payload";

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

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
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
