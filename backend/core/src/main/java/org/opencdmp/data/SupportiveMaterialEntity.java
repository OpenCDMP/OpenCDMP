package org.opencdmp.data;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.SupportiveMaterialFieldTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"SupportiveMaterial\"")
public class SupportiveMaterialEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "type", nullable = false)
    @Convert(converter = SupportiveMaterialFieldTypeConverter.class)
    private SupportiveMaterialFieldType type;
    public static final String _type = "type";

    @Column(name = "language_code", length = _languageCodeLength, nullable = false)
    private String languageCode;
    public static final String _languageCode = "languageCode";
    public static final int _languageCodeLength = 20;

    @Column(name = "payload", nullable = false)
    private String payload;
    public static final String _payload = "payload";

    @Column(name = "\"created_at\"", nullable = false)
    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    @Column(name = "\"updated_at\"", nullable = false)
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

    public SupportiveMaterialFieldType getType() {
        return type;
    }

    public void setType(SupportiveMaterialFieldType type) {
        this.type = type;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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
