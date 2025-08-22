package org.opencdmp.commons.types.pluginconfiguration;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.PluginType;

import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class PluginConfigurationUserEntity {

    @XmlAttribute(name="pluginCode")
    private String pluginCode;

    @XmlAttribute(name="pluginType")
    private PluginType pluginType;

    @XmlElementWrapper(name = "userFields")
    @XmlElement(name = "userField")
    private List<PluginConfigurationUserFieldEntity> userFields;

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

    public List<PluginConfigurationUserFieldEntity> getUserFields() {
        return userFields;
    }

    public void setUserFields(List<PluginConfigurationUserFieldEntity> userFields) {
        this.userFields = userFields;
    }
}
