package org.opencdmp.data;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.DescriptionTemplateStatusConverter;
import org.opencdmp.data.converters.enums.DescriptionTemplateVersionStatusConverter;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.data.types.SQLXMLType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"DescriptionTemplate\"")
public class DescriptionTemplateEntity extends TenantScopedBaseEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "label", length = DescriptionTemplateEntity._labelLength, nullable = false)
    private String label;
    public static final String _label = "label";
    public static final int _labelLength = 250;

    @Column(name = "code", length = _codeLength, nullable = false)
    private String code;
    public final static String _code = "code";
    public final static int _codeLength = 200;

    @Type(SQLXMLType.class)
    @Column(name = "definition", nullable = false, columnDefinition = "xml")
    private String definition;
    public static final String _definition = "definition";

    @Column(name = "status", nullable = false)
    @Convert(converter = DescriptionTemplateStatusConverter.class)
    private DescriptionTemplateStatus status;
    public static final String _status = "status";

    @Column(name = "is_active", nullable = false)
    @Convert(converter = IsActiveConverter.class)
    private IsActive isActive;
    public static final String _isActive = "isActive";


    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    @Column(name = "description", nullable = false)
    private String description;
    public static final String _description = "description";

    @Column(name = "group_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID groupId;
    public static final String _groupId = "groupId";

    @Column(name = "version", nullable = false)
    private Short version;
    public static final String _version = "version";

    @Column(name = "version_status", nullable = false)
    @Convert(converter = DescriptionTemplateVersionStatusConverter.class)
    private DescriptionTemplateVersionStatus versionStatus;
    public static final String _versionStatus = "versionStatus";
    
    @Column(name = "language", nullable = false)
    private String language;
    public static final String _language = "language";

    @Column(name = "type", nullable = false)
    private UUID typeId;
    public static final String _typeId = "typeId";

    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getId() {
        return this.id;
    }
    public void setId(UUID id) { this.id = id;}

    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDefinition() {
        return this.definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public UUID getGroupId() { return this.groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId;}

    public Short getVersion() { return this.version; }
    public void setVersion(Short version) { this.version = version; }

    public String getLanguage() {
        return this.language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public DescriptionTemplateStatus getStatus() {
        return this.status;
    }

    public void setStatus(DescriptionTemplateStatus status) {
        this.status = status;
    }

    public IsActive getIsActive() {
        return this.isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
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

    public UUID getTypeId() {
        return this.typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    public DescriptionTemplateVersionStatus getVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(DescriptionTemplateVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
    }
}
