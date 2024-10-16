package org.opencdmp.commons.types.prefillingsource;

import jakarta.xml.bind.annotation.XmlElement;

public class PrefillingSourceDefinitionFixedValueFieldEntity {
    private String systemFieldTarget;
    private String semanticTarget;
    private String trimRegex;
    private String fixedValue;

    public String getSystemFieldTarget() {
        return systemFieldTarget;
    }

    @XmlElement(name = "systemFieldTarget")
    public void setSystemFieldTarget(String systemFieldTarget) {
        this.systemFieldTarget = systemFieldTarget;
    }

    public String getSemanticTarget() {
        return semanticTarget;
    }

    @XmlElement(name = "semanticTarget")
    public void setSemanticTarget(String semanticTarget) {
        this.semanticTarget = semanticTarget;
    }

    public String getTrimRegex() {
        return trimRegex;
    }

    @XmlElement(name = "trimRegex")
    public void setTrimRegex(String trimRegex) {
        this.trimRegex = trimRegex;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    @XmlElement(name = "fixedValue")
    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }
}
