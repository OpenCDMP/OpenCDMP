package org.opencdmp.model.tenantconfiguration;

public class TermsAndConditionsTenantConfiguration {

    private String content;
    public static final String _content = "content";

    private Short version;
    public static final String _version = "version";

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }
}
