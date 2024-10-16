package org.opencdmp.model;

import java.util.UUID;

public class PlanInvitationResult {

    private UUID planId;

    private Boolean isAlreadyAccepted;

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public Boolean getIsAlreadyAccepted() {
        return isAlreadyAccepted;
    }

    public void setIsAlreadyAccepted(Boolean alreadyAccepted) {
        isAlreadyAccepted = alreadyAccepted;
    }
}
