package org.opencdmp.model.pluginconfiguration;

import org.opencdmp.commons.enums.PluginType;

import java.util.List;


public class PluginConfiguration {

    private String pluginCode;
    public final static String _pluginCode = "pluginCode";

    private PluginType pluginType;
    public final static String _pluginType = "pluginType";

    private List<PluginConfigurationField> fields;
    public final static String _fields = "fields";

    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public List<PluginConfigurationField> getFields() {
        return fields;
    }

    public void setFields(List<PluginConfigurationField> fields) {
        this.fields = fields;
    }
}
