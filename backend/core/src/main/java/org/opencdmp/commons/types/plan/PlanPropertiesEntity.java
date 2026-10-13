package org.opencdmp.commons.types.plan;

import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;

import java.util.List;
import java.util.Map;

public class PlanPropertiesEntity {

    private Map<String, PropertyDefinitionFieldSetEntity> fieldSets;

    private List<PlanContactEntity> contacts;

    private PlanPropertiesConsentEntity consent;

    public Map<String, PropertyDefinitionFieldSetEntity> getFieldSets() {
        return fieldSets;
    }

    public void setFieldSets(Map<String, PropertyDefinitionFieldSetEntity> fieldSets) {
        this.fieldSets = fieldSets;
    }

    public List<PlanContactEntity> getContacts() {
        return this.contacts;
    }

    public void setContacts(List<PlanContactEntity> contacts) {
        this.contacts = contacts;
    }

    public PlanPropertiesConsentEntity getConsent() {
        return consent;
    }

    public void setConsent(PlanPropertiesConsentEntity consent) {
        this.consent = consent;
    }
}
