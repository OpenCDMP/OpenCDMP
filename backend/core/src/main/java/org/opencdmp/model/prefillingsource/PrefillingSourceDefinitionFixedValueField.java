package org.opencdmp.model.prefillingsource;

public class PrefillingSourceDefinitionFixedValueField {
    private String systemFieldTarget;
    public final static String _systemFieldTarget = "systemFieldTarget";
    private String semanticTarget;
    public final static String _semanticTarget = "semanticTarget";
    private String trimRegex;
    public final static String _trimRegex = "trimRegex";
    private String fixedValue;
    public final static String _fixedValue = "fixedValue";

    public String getSystemFieldTarget() {
        return systemFieldTarget;
    }

    public void setSystemFieldTarget(String systemFieldTarget) {
        this.systemFieldTarget = systemFieldTarget;
    }

    public String getSemanticTarget() {
        return semanticTarget;
    }

    public void setSemanticTarget(String semanticTarget) {
        this.semanticTarget = semanticTarget;
    }

    public String getTrimRegex() {
        return trimRegex;
    }

    public void setTrimRegex(String trimRegex) {
        this.trimRegex = trimRegex;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }
}
