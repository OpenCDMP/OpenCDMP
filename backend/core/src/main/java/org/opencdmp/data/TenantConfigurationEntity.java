package org.opencdmp.data;


import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.TenantConfigurationTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"TenantConfiguration\"")
public class TenantConfigurationEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public final static String _id = "id";

    @Column(name = "value", nullable = false)
    private String value;
    public final static String _value = "value";

    @Column(name = "type", nullable = false)
    @Convert(converter = TenantConfigurationTypeConverter.class)
    private TenantConfigurationType type;
    public final static String _type = "type";

    @Column(name = "is_active", nullable = false)
    @Convert(converter = IsActiveConverter.class)
    private IsActive isActive;
    public final static String _isActive = "isActive";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    public final static String _createdAt = "createdAt";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    public final static String _updatedAt = "updatedAt";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TenantConfigurationType getType() {
        return type;
    }

    public void setType(TenantConfigurationType type) {
        this.type = type;
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
}
