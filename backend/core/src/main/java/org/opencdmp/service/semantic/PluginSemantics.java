package org.opencdmp.service.semantic;

import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.commonmodels.models.semantics.SemanticsManifest;

/** The semantic keys one of the tenant's plugins reads, as its manifest publishes them. */
public class PluginSemantics {

    private PluginType pluginType;
    private String pluginId;
    private SemanticsManifest semantics;

    public PluginSemantics() {
    }

    public PluginSemantics(PluginType pluginType, String pluginId, SemanticsManifest semantics) {
        this.pluginType = pluginType;
        this.pluginId = pluginId;
        this.semantics = semantics;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public SemanticsManifest getSemantics() {
        return semantics;
    }

    public void setSemantics(SemanticsManifest semantics) {
        this.semantics = semantics;
    }
}
