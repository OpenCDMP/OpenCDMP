package org.opencdmp.model.deposit;

import org.opencdmp.depositbase.enums.DepositType;

public class DepositConfiguration {

    private DepositType depositType;
    public static final String _depositType = "depositType";
    private String repositoryId;
    public static final String _repositoryId = "repositoryId";
    private String repositoryAuthorizationUrl;
    public static final String _repositoryAuthorizationUrl = "repositoryAuthorizationUrl";
    private String repositoryRecordUrl;
    public static final String _repositoryRecordUrl = "repositoryRecordUrl";
    private String repositoryClientId;
    public static final String _repositoryClientId = "repositoryClientId";
    private String redirectUri;
    public static final String _redirectUri = "redirectUri";
    private boolean hasLogo;
    public static final String _hasLogo = "hasLogo";

    public DepositType getDepositType() {
        return depositType;
    }
    public void setDepositType(DepositType depositType) {
        this.depositType = depositType;
    }

    public String getRepositoryId() {
        return repositoryId;
    }
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryAuthorizationUrl() {
        return repositoryAuthorizationUrl;
    }
    public void setRepositoryAuthorizationUrl(String repositoryAuthorizationUrl) {
        this.repositoryAuthorizationUrl = repositoryAuthorizationUrl;
    }

    public String getRepositoryRecordUrl() {
        return repositoryRecordUrl;
    }
    public void setRepositoryRecordUrl(String repositoryRecordUrl) {
        this.repositoryRecordUrl = repositoryRecordUrl;
    }

    public String getRepositoryClientId() {
        return repositoryClientId;
    }
    public void setRepositoryClientId(String repositoryClientId) {
        this.repositoryClientId = repositoryClientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public boolean isHasLogo() {
        return hasLogo;
    }
    public void setHasLogo(boolean hasLogo) {
        this.hasLogo = hasLogo;
    }
}

