package org.opencdmp.data;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.DescriptionStatusNullableConverter;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.data.types.SQLXMLType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"DescriptionStatus\"")
public class DescriptionStatusEntity extends TenantScopedBaseEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "name", length = DescriptionStatusEntity._nameLength, nullable = false)
    private String name;
    public static final String _name = "name";
    public static final int _nameLength = 250;

    @Column(name = "description", nullable = true)
    private String description;
    public static final String _description = "description";

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

    @Column(name = "internal_status", nullable = true)
    @Convert(converter = DescriptionStatusNullableConverter.class)
    private DescriptionStatus internalStatus;
    public static final String _internalStatus = "internalStatus";

    @Type(SQLXMLType.class)
    @Column(name = "definition", nullable = false, columnDefinition = "xml")
    private String definition;
    public static final String _definition = "definition";

    public UUID getId() {
        return this.id;
    }
    public void setId(UUID id) { this.id = id;}

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description;}

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

    public DescriptionStatus getInternalStatus() { return internalStatus; }
    public void setInternalStatus(DescriptionStatus internalStatus) { this.internalStatus = internalStatus; }

    public String getDefinition() {
        return this.definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
