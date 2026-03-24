package org.opencdmp.integrationevent.inbox.dmptouch;

import org.opencdmp.commons.enums.PlanUpdateRequestActionType;
import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class DmpTouchedIntegrationEvent extends TrackedEvent {

    private UUID dmpId;

    private Object data;

    private UUID submitterId;

    private PlanUpdateRequestActionType actionType;

    private String sourceCode;

    public UUID getDmpId() {
        return dmpId;
    }

    public void setDmpId(UUID dmpId) {
        this.dmpId = dmpId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public UUID getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(UUID submitterId) {
        this.submitterId = submitterId;
    }

    public PlanUpdateRequestActionType getActionType() {
        return actionType;
    }

    public void setActionType(PlanUpdateRequestActionType actionType) {
        this.actionType = actionType;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
