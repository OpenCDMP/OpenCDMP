package org.opencdmp.commons.types.pluginconfiguration.importexport;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.PluginType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class PluginConfigurationImportExport {

        @XmlAttribute(name="pluginCode")
        private String pluginCode;

        @XmlAttribute(name="pluginType")
        private PluginType pluginType;

        @XmlElementWrapper(name = "fields")
        @XmlElement(name = "field")
        private List<PluginConfigurationFieldImportExport> fields;

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

        public List<PluginConfigurationFieldImportExport> getFields() {
            return fields;
        }

        public void setFields(List<PluginConfigurationFieldImportExport> fields) {
            this.fields = fields;
        }


}
