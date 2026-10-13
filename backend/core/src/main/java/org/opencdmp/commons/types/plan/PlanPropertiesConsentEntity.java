package org.opencdmp.commons.types.plan;

import java.time.Instant;
import java.util.UUID;

public class PlanPropertiesConsentEntity {

    private UUID consentBy;

    private Instant consentAt;

    public PlanPropertiesConsentEntity() {
    }

    public PlanPropertiesConsentEntity(UUID consentBy, Instant consentAt) {
        this.consentBy = consentBy;
        this.consentAt = consentAt;
    }

    public UUID getConsentBy() {
        return consentBy;
    }

    public void setConsentBy(UUID consentBy) {
        this.consentBy = consentBy;
    }

    public Instant getConsentAt() {
        return consentAt;
    }

    public void setConsentAt(Instant consentAt) {
        this.consentAt = consentAt;
    }
}
