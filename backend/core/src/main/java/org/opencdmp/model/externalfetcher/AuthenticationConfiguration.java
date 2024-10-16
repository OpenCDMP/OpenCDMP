package org.opencdmp.model.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;

public class AuthenticationConfiguration {

    public final static String _enabled = "enabled";
    private Boolean enabled;

    public final static String _authUrl = "authUrl";
    private String authUrl;

    public final static String _authMethod = "authMethod";
    private ExternalFetcherApiHTTPMethodType authMethod;

    public final static String _authTokenPath = "authTokenPath";
    private String authTokenPath;

    public final static String _authRequestBody = "authRequestBody";
    private String authRequestBody;

    public final static String _type = "type";
    private String type;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getAuthUrl() {
        return authUrl;
    }


    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public ExternalFetcherApiHTTPMethodType getAuthMethod() {
        return authMethod;
    }


    public void setAuthMethod(ExternalFetcherApiHTTPMethodType authMethod) {
        this.authMethod = authMethod;
    }

    public String getAuthTokenPath() {
        return authTokenPath;
    }


    public void setAuthTokenPath(String authTokenPath) {
        this.authTokenPath = authTokenPath;
    }

    public String getAuthRequestBody() {
        return authRequestBody;
    }


    public void setAuthRequestBody(String authRequestBody) {
        this.authRequestBody = authRequestBody;
    }

    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }
}
