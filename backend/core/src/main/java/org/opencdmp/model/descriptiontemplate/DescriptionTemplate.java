package org.opencdmp.model.descriptiontemplate;

import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.UserDescriptionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DescriptionTemplate {

    public final static String _id = "id";
    private UUID id;

    public final static String _label = "label";
    private String label;

    public final static String _code = "code";
    private String code;

    public final static String _description = "description";
    private String description;

    public final static String _groupId = "groupId";
    private UUID groupId;

    public static final String _versionStatus = "versionStatus";
    private DescriptionTemplateVersionStatus versionStatus;

    public final static String _version = "version";
    private Short version;

    public final static String _language = "language";
    private String language;

    public final static String _type = "type";
    private DescriptionTemplateType type;

    public final static String _definition = "definition";
    private Definition definition;

    public final static String _createdAt = "createdAt";
    private Instant createdAt;

    public final static String _updatedAt = "updatedAt";
    private Instant updatedAt;

    public final static String _isActive = "isActive";
    private IsActive isActive;

    public final static String _status = "status";
    private DescriptionTemplateStatus status;

    public final static String _users = "users";
    private List<UserDescriptionTemplate> users;

    public final static String _hash = "hash";
    private String hash;

    private List<String> authorizationFlags;
    public static final String _authorizationFlags = "authorizationFlags";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    private Integer ordinal;
    public static final String _ordinal = "ordinal";

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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public DescriptionTemplateType getType() {
        return this.type;
    }

    public void setType(DescriptionTemplateType type) {
        this.type = type;
    }

    public Definition getDefinition() {
        return this.definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
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

    public DescriptionTemplateStatus getStatus() {
        return this.status;
    }

    public void setStatus(DescriptionTemplateStatus status) {
        this.status = status;
    }

    public List<UserDescriptionTemplate> getUsers() {
        return this.users;
    }

    public void setUsers(List<UserDescriptionTemplate> users) {
        this.users = users;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public DescriptionTemplateVersionStatus getVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(DescriptionTemplateVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
    }

    public List<String> getAuthorizationFlags() {
        return this.authorizationFlags;
    }

    public void setAuthorizationFlags(List<String> authorizationFlags) {
        this.authorizationFlags = authorizationFlags;
    }

    public Boolean getBelongsToCurrentTenant() {
        return this.belongsToCurrentTenant;
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
}
