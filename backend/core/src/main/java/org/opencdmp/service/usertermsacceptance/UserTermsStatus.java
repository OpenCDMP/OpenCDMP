package org.opencdmp.service.usertermsacceptance;

public class UserTermsStatus {

    private boolean termsOfServiceAcceptanceRequired;
    private String content;

    public boolean isTermsOfServiceAcceptanceRequired() {
        return termsOfServiceAcceptanceRequired;
    }

    public void setTermsOfServiceAcceptanceRequired(boolean termsOfServiceAcceptanceRequired) {
        this.termsOfServiceAcceptanceRequired = termsOfServiceAcceptanceRequired;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
