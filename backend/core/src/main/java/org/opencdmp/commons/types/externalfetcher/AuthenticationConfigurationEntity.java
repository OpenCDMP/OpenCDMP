package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;
import org.opencdmp.service.externalfetcher.config.entities.AuthenticationConfiguration;
import jakarta.xml.bind.annotation.XmlElement;

public class AuthenticationConfigurationEntity implements AuthenticationConfiguration {

    private Boolean enabled;
    private String authUrl;
    private ExternalFetcherApiHTTPMethodType authMethod;
    private String authTokenPath;
    private String authRequestBody;
    private String type;

    public String getAuthUrl() {
        return authUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    @XmlElement(name = "enabled")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @XmlElement(name = "authUrl")
    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public ExternalFetcherApiHTTPMethodType getAuthMethod() {
        return authMethod;
    }

    @XmlElement(name = "authUrlMethod")
    public void setAuthMethod(ExternalFetcherApiHTTPMethodType authMethod) {
        this.authMethod = authMethod;
    }

    public String getAuthTokenPath() {
        return authTokenPath;
    }

    @XmlElement(name = "authTokenPath")
    public void setAuthTokenPath(String authTokenPath) {
        this.authTokenPath = authTokenPath;
    }

    public String getAuthRequestBody() {
        return authRequestBody;
    }

    @XmlElement(name = "authUrlBody")
    public void setAuthRequestBody(String authRequestBody) {
        this.authRequestBody = authRequestBody;
    }

    public String getType() {
        return type;
    }

    @XmlElement(name = "authType")
    public void setType(String type) {
        this.type = type;
    }
}
