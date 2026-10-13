package org.opencdmp.model.pluginintegration;

import org.opencdmp.commons.enums.PluginType;

/**
 * One plugin source as the tenant administrator sees it: where it comes from, whether the tenant has it switched on,
 * and whether it answered the last time its configuration was asked for.
 */
public class PluginSourceOverview {

    /** Declared in the installation's configuration files; the tenant can only switch it off. */
    public static final String PROVIDER_INSTALLATION = "installation";
    /** Added by the tenant itself; its connection details live in the tenant configuration. */
    public static final String PROVIDER_TENANT = "tenant";

    private PluginType pluginType;
    private String id;
    private String provider;
    private boolean enabled;
    /** Null when the source is switched off and was therefore not contacted. */
    private Boolean reachable;
    private String url;

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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getReachable() {
        return reachable;
    }

    public void setReachable(Boolean reachable) {
        this.reachable = reachable;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
