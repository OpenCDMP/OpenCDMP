package org.opencdmp.data;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.UserDescriptionTemplateRoleConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UserDescriptionTemplate\"")
public class UserDescriptionTemplateEntity extends TenantScopedBaseEntity {
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

    @Column(name = "\"description_template\"", nullable = false)
    private UUID descriptionTemplateId;
    public static final String _descriptionTemplateId = "descriptionTemplateId";

    @Column(name = "role", nullable = false)
    @Convert(converter = UserDescriptionTemplateRoleConverter.class)
    private UserDescriptionTemplateRole role;
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

    public UUID getDescriptionTemplateId() {
        return descriptionTemplateId;
    }

    public void setDescriptionTemplateId(UUID descriptionTemplateId) {
        this.descriptionTemplateId = descriptionTemplateId;
    }

    public UserDescriptionTemplateRole getRole() {
        return role;
    }

    public void setRole(UserDescriptionTemplateRole role) {
        this.role = role;
    }
}
