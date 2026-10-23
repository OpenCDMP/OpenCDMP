package org.opencdmp.data;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UserPlanBlueprintRole;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.UserPlanBlueprintRoleConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UserPlanBlueprint\"")
public class UserPlanBlueprintEntity extends TenantScopedBaseEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "\"user\"", nullable = false)
    private UUID userId;
    public static final String _userId = "userId";

    @Column(name = "is_active", nullable = false)
    @Convert(converter = IsActiveConverter.class)
    private IsActive isActive;
    public static final String _isActive = "isActive";


    @Column(name = "\"created_at\"", nullable = false)
    private Instant createdAt = null;
    public static final String _createdAt = "createdAt";

    @Column(name = "\"updated_at\"", nullable = false)
    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    @Column(name = "\"plan_blueprint\"", nullable = false)
    private UUID planBlueprintId;
    public static final String _planBlueprintId = "planBlueprintId";

    @Column(name = "role", nullable = false)
    @Convert(converter = UserPlanBlueprintRoleConverter.class)
    private UserPlanBlueprintRole role;
    public static final String _role = "role";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
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

    public UUID getPlanBlueprintId() {
        return planBlueprintId;
    }

    public void setPlanBlueprintId(UUID planBlueprintId) {
        this.planBlueprintId = planBlueprintId;
    }

    public UserPlanBlueprintRole getRole() {
        return role;
    }

    public void setRole(UserPlanBlueprintRole role) {
        this.role = role;
    }
}
