package org.opencdmp.data;

import jakarta.persistence.*;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.PlanUserRoleConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"PlanUser\"")
public class PlanUserEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    public static final String _id = "id";

    @Column(name = "plan", columnDefinition = "uuid", nullable = false)
    private UUID planId;

    public static final String _planId = "planId";

    @Column(name = "\"user\"", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    public static final String _userId = "userId";

    @Column(name = "role", nullable = false)
    @Convert(converter = PlanUserRoleConverter.class)
    private PlanUserRole role;

    public static final String _role = "role";

    @Column(name = "section_id", columnDefinition = "uuid", nullable = true)
    private UUID sectionId;
    public static final String _sectionId = "sectionId";

    @Column(name = "\"ordinal\"", nullable = false)
    private Integer ordinal;
    public static final String _ordinal = "ordinal";

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

    public UUID getUserId() {
        return this.userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public PlanUserRole getRole() {
        return this.role;
    }

    public void setRole(PlanUserRole role) {
        this.role = role;
    }

    public UUID getSectionId() {
        return this.sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
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
