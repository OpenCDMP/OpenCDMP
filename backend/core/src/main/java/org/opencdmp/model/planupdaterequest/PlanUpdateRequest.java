package org.opencdmp.model.planupdaterequest;


import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanUpdateRequestActionType;
import org.opencdmp.commons.enums.PlanUpdateRequestStatus;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class PlanUpdateRequest {

    private UUID id;
    public static final String _id = "id";

    private Plan plan;
    public static final String _plan = "plan";

    private User submitter;
    public static final String _submitter = "submitter";

    private PlanUpdateRequestActionType actionType;
    public static final String _actionType = "actionType";

    private PlanUpdateRequestStatus status;
    public static final String _status = "status";

    private String data;
    public static final String _data = "data";

    private String sourceCode;
    public static final String _sourceCode = "sourceCode";

    private Instant requestAt;
    public static final String _requestAt = "requestAt";

    private User approvedBy;
    public static final String _approvedBy = "approvedBy";

    private Instant approvedAt;
    public static final String _approvedAt = "approvedAt";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private String hash;
    public static final String _hash = "hash";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public User getSubmitter() {
        return submitter;
    }

    public void setSubmitter(User submitter) {
        this.submitter = submitter;
    }

    public PlanUpdateRequestActionType getActionType() {
        return actionType;
    }

    public void setActionType(PlanUpdateRequestActionType actionType) {
        this.actionType = actionType;
    }

    public PlanUpdateRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PlanUpdateRequestStatus status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Instant getRequestAt() {
        return requestAt;
    }

    public void setRequestAt(Instant requestAt) {
        this.requestAt = requestAt;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
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
}
