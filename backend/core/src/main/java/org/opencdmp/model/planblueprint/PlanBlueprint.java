package org.opencdmp.model.planblueprint;

import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.commons.enums.IsActive;

import java.time.Instant;
import java.util.UUID;

public class PlanBlueprint {

    private UUID id;

    public static final String _id = "id";

    private String label;

    public static final String _label = "label";

    private String code;

    public final static String _code = "code";

    private Definition definition;

    public static final String _definition = "definition";

    private PlanBlueprintStatus status;

    public static final String _status = "status";

    private UUID groupId;

    public static final String _groupId = "groupId";

    private Short version;

    public static final String _version = "version";

    private PlanBlueprintVersionStatus versionStatus;

    public static final String _versionStatus = "versionStatus";

    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;

    public static final String _isActive = "isActive";

    private String hash;

    public static final String _hash = "hash";

    private String description;

    public static final String _description = "description";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    private Integer ordinal;
    public static final String _ordinal = "ordinal";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
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

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public PlanBlueprintStatus getStatus() {
        return status;
    }

    public void setStatus(PlanBlueprintStatus status) {
        this.status = status;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    public PlanBlueprintVersionStatus getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(PlanBlueprintVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
