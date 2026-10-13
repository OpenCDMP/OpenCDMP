package org.opencdmp.model.pluginintegration;

import org.opencdmp.commons.enums.PluginType;

/**
 * Connection details to try before saving them. With only {@code pluginType} and {@code id} set, the source already in
 * effect for the tenant is tested. A blank {@code clientSecret} reuses the secret stored for a tenant source with the
 * same id, so the administrator does not have to type it again.
 */
public class PluginConnectionTestRequest {

    private PluginType pluginType;
    private String id;
    private String url;
    private String issuerUrl;
    private String clientId;
    private String clientSecret;
    private String scope;

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
