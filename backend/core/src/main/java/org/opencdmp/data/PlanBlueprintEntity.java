package org.opencdmp.data;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.data.converters.enums.PlanBlueprintVersionStatusConverter;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.PlanBlueprintStatusConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.data.types.SQLXMLType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"PlanBlueprint\"")
public class PlanBlueprintEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    public static final String _id = "id";

    @Column(name = "label", length = PlanBlueprintEntity._labelLength, nullable = false)
    private String label;

    public static final String _label = "label";

    public static final int _labelLength = 250;

    @Column(name = "code", length = _codeLength, nullable = false)
    private String code;
    public final static String _code = "code";
    public final static int _codeLength = 200;

    @Type(SQLXMLType.class)
    @Column(name = "definition", columnDefinition = "xml")
    private String definition;

    public static final String _definition = "definition";

    @Column(name = "status", nullable = false)
    @Convert(converter = PlanBlueprintStatusConverter.class)
    private PlanBlueprintStatus status;

    public static final String _status = "status";

    @Column(name = "group_id", columnDefinition = "uuid", nullable = false)
    private UUID groupId;

    public static final String _groupId = "groupId";

    @Column(name = "version", nullable = false)
    private Short version;

    public static final String _version = "version";

    @Column(name = "version_status", nullable = false)
    @Convert(converter = PlanBlueprintVersionStatusConverter.class)
    private PlanBlueprintVersionStatus versionStatus;

    public static final String _versionStatus = "versionStatus";

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

    public PlanBlueprintStatus getStatus() {
        return this.status;
    }

    public void setStatus(PlanBlueprintStatus status) {
        this.status = status;
    }

    public UUID getGroupId() {
        return this.groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public Short getVersion() {
        return this.version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    public PlanBlueprintVersionStatus getVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(PlanBlueprintVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
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
