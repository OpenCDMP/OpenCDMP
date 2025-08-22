package org.opencdmp.model.deposit;

import org.opencdmp.commonmodels.models.ConfigurationField;
import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.depositbase.enums.DepositAuthMethod;
import org.opencdmp.depositbase.enums.DepositType;

import java.util.List;

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
    private List<ConfigurationField> configurationFields;
    public static final String _configurationFields = "configurationFields";
    private List<ConfigurationField> userConfigurationFields;
    public static final String _userConfigurationFields = "userConfigurationFields";
    private PluginType pluginType;
    public static final String _pluginType = "pluginType";
    private List<DepositAuthMethod> authMethods;
    public static final String _authMethods = "authMethods";

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

    public List<ConfigurationField> getConfigurationFields() {
        return configurationFields;
    }
    public void setConfigurationFields(List<ConfigurationField> configurationFields) {
        this.configurationFields = configurationFields;
    }

    public List<ConfigurationField> getUserConfigurationFields() {
        return userConfigurationFields;
    }
    public void setUserConfigurationFields(List<ConfigurationField> userConfigurationFields) {
        this.userConfigurationFields = userConfigurationFields;
    }

    public PluginType getPluginType() {
        return pluginType;
    }
    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public List<DepositAuthMethod> getAuthMethods() {
        return authMethods;
    }
    public void setAuthMethods(List<DepositAuthMethod> authMethods) {
        this.authMethods = authMethods;
    }
}

