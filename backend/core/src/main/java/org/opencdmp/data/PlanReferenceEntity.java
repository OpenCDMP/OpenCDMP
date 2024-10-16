package org.opencdmp.data;

import jakarta.persistence.*;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"PlanReference\"")
public class PlanReferenceEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "plan_id", columnDefinition = "uuid", nullable = false)
    private UUID planId;
    public static final String _planId = "planId";

    @Column(name = "reference_id", columnDefinition = "uuid", nullable = false)
    private UUID referenceId;
    public static final String _referenceId = "referenceId";

    @Column(name = "data")
    private String data;
    public static final String _data = "data";

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
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlanId() {
        return this.planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public UUID getReferenceId() {
        return this.referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public IsActive getIsActive() {
        return this.isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }
}
