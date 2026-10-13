package org.opencdmp.commons.types.planblueprint.migration;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.iconconfiguration.IconConfigurationEntity;
import org.opencdmp.commons.types.planblueprint.PlanCreationConsentEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @deprecated Kept for migration tool backward compat.
 */
@Deprecated
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefinitionEntityMigration {
	@XmlElementWrapper(name = "sections")
	@XmlElement(name = "section")
	private List<SectionEntityMigration> sections;

	@XmlElementWrapper(name = "pluginConfigurations")
	@XmlElement(name = "pluginConfiguration")
	private List<PluginConfigurationEntity> pluginConfigurations;

	@XmlElement(name = "planCreationConsent")
	private PlanCreationConsentEntity planCreationConsent;

	@XmlElement(name = "iconconfiguration")
	private IconConfigurationEntity iconConfiguration;

	public List<SectionEntityMigration> getSections() {
		return this.sections;
	}

	public void setSections(List<SectionEntityMigration> sections) {
		this.sections = sections;
	}

	public List<PluginConfigurationEntity> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationEntity> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

	public List<FieldEntityMigration> getAllField(){
		List<FieldEntityMigration> fieldEntities = new ArrayList<>();
		if (this.getSections() != null){
			for (SectionEntityMigration sectionEntity: this.getSections()) {
				fieldEntities.addAll(sectionEntity.getAllField());
			}
		}
		return fieldEntities;
	}

	public List<FieldEntityMigration> getFieldById(UUID id) {
		return this.getAllField().stream().filter(x-> id.equals(x.getId())).toList();
	}

	public IconConfigurationEntity getIconConfiguration() {
		return iconConfiguration;
	}

	public void setIconConfiguration(IconConfigurationEntity iconConfiguration) {
		this.iconConfiguration = iconConfiguration;
	}

	public PlanCreationConsentEntity getPlanCreationConsent() {
		return planCreationConsent;
	}

	public void setPlanCreationConsent(PlanCreationConsentEntity planCreationConsent) {
		this.planCreationConsent = planCreationConsent;
	}
}
