package org.opencdmp.data;


import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"Tenant\"")
public class TenantEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public final static String _id = "id";

    @Column(name = "code", length = _codeLength, nullable = false)
    private String code;
    public final static String _code = "code";
    public final static int _codeLength = 200;

    @Column(name = "name", length = _nameLength, nullable = false)
    private String name;
    public final static String _name = "name";
    public final static int _nameLength = 500;

    @Column(name = "description", nullable = false)
    private String description;
    public final static String _description = "description";

    @Column(name = "is_active", length = 20, nullable = false)
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
