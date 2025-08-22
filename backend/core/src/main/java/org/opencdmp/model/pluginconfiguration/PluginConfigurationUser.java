package org.opencdmp.model.pluginconfiguration;

import org.opencdmp.commons.enums.PluginType;

import java.util.List;


public class PluginConfigurationUser {

    private String pluginCode;
    public final static String _pluginCode = "pluginCode";

    private PluginType pluginType;
    public final static String _pluginType = "pluginType";

    private List<PluginConfigurationUserField> userFields;
    public final static String _userFields = "userFields";

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

    public List<PluginConfigurationUserField> getUserFields() {
        return userFields;
    }

    public void setUserFields(List<PluginConfigurationUserField> userFields) {
        this.userFields = userFields;
    }
}
