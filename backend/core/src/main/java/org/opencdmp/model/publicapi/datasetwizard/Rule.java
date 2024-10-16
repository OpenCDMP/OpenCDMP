package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.commons.types.descriptiontemplate.RuleEntity;

public class Rule {
    private String sourceField;
    private String targetField;
    private String requiredValue;
    private String type;

    public String getSourceField() {
        return this.sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return this.targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getRequiredValue() {
        return this.requiredValue;
    }

    public void setRequiredValue(String requiredValue) {
        this.requiredValue = requiredValue;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Rule fromDefinitionRule(RuleEntity rule) {
        this.targetField = rule.getTarget();
        //TODO
        if (rule.getTextValue() != null) this.requiredValue = rule.getTextValue();
        else if (rule.getDateValue() != null) this.requiredValue = rule.getDateValue().toString();
        else if (rule.getBooleanValue() != null) this.requiredValue = rule.getBooleanValue().toString();
        return this;
    }

}
