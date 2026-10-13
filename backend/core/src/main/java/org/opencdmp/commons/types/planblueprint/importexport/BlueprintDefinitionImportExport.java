package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.iconconfiguration.importexport.IconConfigurationImportExport;
import org.opencdmp.commons.types.pluginconfiguration.importexport.PluginConfigurationImportExport;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintDefinitionImportExport {

    @XmlElementWrapper(name = "sections")
    @XmlElement(name = "section")
    private List<BlueprintSectionImportExport> sections;

    @XmlElementWrapper(name = "pluginConfigurations")
    @XmlElement(name = "pluginConfiguration")
    private List<PluginConfigurationImportExport> pluginConfigurations;

    @XmlElement(name = "planCreationConsent")
    private BlueprintPlanCreationConsentImportExport planCreationConsent;

    @XmlElement(name = "icon")
    private IconConfigurationImportExport iconConfiguration;

    public List<BlueprintSectionImportExport> getSections() {
        return this.sections;
    }

    public void setSections(List<BlueprintSectionImportExport> sections) {
        this.sections = sections;
    }

    public List<PluginConfigurationImportExport> getPluginConfigurations() {
        return pluginConfigurations;
    }

    public void setPluginConfigurations(List<PluginConfigurationImportExport> pluginConfigurations) {
        this.pluginConfigurations = pluginConfigurations;
    }

    public BlueprintPlanCreationConsentImportExport getPlanCreationConsent() {
        return planCreationConsent;
    }

    public void setPlanCreationConsent(BlueprintPlanCreationConsentImportExport planCreationConsent) {
        this.planCreationConsent = planCreationConsent;
    }

    public IconConfigurationImportExport getIconConfiguration() {
        return iconConfiguration;
    }

    public void setIconConfiguration(IconConfigurationImportExport iconConfiguration) {
        this.iconConfiguration = iconConfiguration;
    }
}