package org.opencdmp.data;

import jakarta.persistence.*;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.PlanAccessTypeNullableConverter;
import org.opencdmp.data.converters.enums.PlanStatusConverter;
import org.opencdmp.data.converters.enums.PlanVersionStatusConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"Plan\"")
public class PlanEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    public static final String _id = "id";

    @Column(name = "label", length = DescriptionEntity._labelLength, nullable = false)
    private String label;

    public static final String _label = "label";
    public static final int _labelLength = 250;

    @Column(name = "version", nullable = false)
    private Short version;

    public static final String _version = "version";

    @Column(name = "version_status", nullable = false)
    @Convert(converter = PlanVersionStatusConverter.class)
    private PlanVersionStatus versionStatus;

    public static final String _versionStatus = "versionStatus";

    @Column(name = "status", nullable = false)
    @Convert(converter = PlanStatusConverter.class)
    private PlanStatus status;

    public static final String _status = "status";

    @Column(name = "properties", nullable = true)
    private String properties;

    public static final String _properties = "properties";

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    public static final String _groupId = "groupId";

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

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    public static final String _finalizedAt = "finalizedAt";

    @Column(name = "creator", nullable = false)
    private UUID creatorId;

    public static final String _creatorId = "creatorId";

    @Column(name = "access_type", nullable = true)
    @Convert(converter = PlanAccessTypeNullableConverter.class)
    private PlanAccessType accessType;

    public static final String _accessType = "accessType";

    @Column(name = "blueprint", nullable = false)
    private UUID blueprintId;

    public static final String _blueprintId = "blueprintId";

    @Column(name = "language", nullable = true)
    private String language;

    public static final String _language = "language";

    @Column(name = "public_after", nullable = true)
    private Instant publicAfter;

    public static final String _publicAfter = "publicAfter";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Short getVersion() {
        return this.version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    public PlanStatus getStatus() {
        return this.status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public String getProperties() {
        return this.properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public UUID getGroupId() {
        return this.groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Instant getFinalizedAt() {
        return this.finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public UUID getCreatorId() {
        return this.creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public PlanAccessType getAccessType() {
        return this.accessType;
    }

    public void setAccessType(PlanAccessType accessType) {
        this.accessType = accessType;
    }

    public UUID getBlueprintId() {
        return this.blueprintId;
    }

    public void setBlueprintId(UUID blueprintId) {
        this.blueprintId = blueprintId;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Instant getPublicAfter() {
        return this.publicAfter;
    }

    public void setPublicAfter(Instant publicAfter) {
        this.publicAfter = publicAfter;
    }

    public PlanVersionStatus getVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(PlanVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
    }
}
