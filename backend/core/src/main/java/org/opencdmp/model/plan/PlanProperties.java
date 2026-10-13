package org.opencdmp.model.plan;

import org.opencdmp.model.description.PropertyDefinitionFieldSet;

import java.util.List;
import java.util.Map;

public class PlanProperties {

    public final static String _fieldSets = "fieldSets";

    private Map<String, PropertyDefinitionFieldSet> fieldSets;

    private List<PlanContact> contacts;

    public static final String _contacts = "contacts";

    private PlanPropertiesConsent consent;

    public static final String _consent = "consent";

    public Map<String, PropertyDefinitionFieldSet> getFieldSets() {
        return fieldSets;
    }

    public void setFieldSets(Map<String, PropertyDefinitionFieldSet> fieldSets) {
        this.fieldSets = fieldSets;
    }

    public List<PlanContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<PlanContact> contacts) {
        this.contacts = contacts;
    }

    public PlanPropertiesConsent getConsent() {
        return consent;
    }

    public void setConsent(PlanPropertiesConsent consent) {
        this.consent = consent;
    }
}
