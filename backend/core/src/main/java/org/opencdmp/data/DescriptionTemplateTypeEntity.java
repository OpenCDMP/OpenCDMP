package org.opencdmp.data;

import org.opencdmp.commons.enums.DescriptionTemplateTypeStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.DescriptionTemplateTypeStatusConverter;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"DescriptionTemplateType\"")
public class DescriptionTemplateTypeEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    public static final String _id = "id";

    @Column(name = "code", length = _codeLength, nullable = false)
    private String code;
    public final static String _code = "code";
    public final static int _codeLength = 200;

    @Column(name = "name", length = DescriptionTemplateTypeEntity._nameLength, nullable = false)
    private String name;
    public static final int _nameLength = 250;

    public static final String _name = "name";

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

    @Column(name = "status", nullable = false)
    @Convert(converter = DescriptionTemplateTypeStatusConverter.class)
    private DescriptionTemplateTypeStatus status;

    public static final String _status = "status";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public DescriptionTemplateTypeStatus getStatus() {
        return status;
    }

    public void setStatus(DescriptionTemplateTypeStatus status) {
        this.status = status;
    }
}
