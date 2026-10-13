package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
public class PlanCreationConsentEntity {

    @XmlAttribute(name="enabled")
    private Boolean enabled;

    @XmlElement(name="message")
    private String message;

    @XmlElement(name="details")
    private String details;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
