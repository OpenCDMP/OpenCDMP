package org.opencdmp.model.plan;

import org.opencdmp.model.user.User;

import java.time.Instant;

public class PlanPropertiesConsent {

    private User consentBy;
    public static final String _consentBy = "consentBy";

    private Instant consentAt;
    public static final String _consentAt = "consentAt";

    public User getConsentBy() {
        return consentBy;
    }

    public void setConsentBy(User consentBy) {
        this.consentBy = consentBy;
    }

    public Instant getConsentAt() {
        return consentAt;
    }

    public void setConsentAt(Instant consentAt) {
        this.consentAt = consentAt;
    }
}
