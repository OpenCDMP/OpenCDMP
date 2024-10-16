package org.opencdmp.model.prefillingsource;

public class PrefillingSourceDefinitionField {

    private String code;
    public final static String _code = "code";
    private String systemFieldTarget;
    public final static String _systemFieldTarget = "systemFieldTarget";
    private String semanticTarget;
    public final static String _semanticTarget = "semanticTarget";
    private String trimRegex;
    public final static String _trimRegex = "trimRegex";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

}
