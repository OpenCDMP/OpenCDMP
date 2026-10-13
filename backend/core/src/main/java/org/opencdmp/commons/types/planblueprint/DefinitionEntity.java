package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.iconconfiguration.IconConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefinitionEntity {
	@XmlElementWrapper(name = "sections")
	@XmlElement(name = "section")
	private List<RootSectionEntity> sections;

	@XmlElementWrapper(name = "pluginConfigurations")
	@XmlElement(name = "pluginConfiguration")
	private List<PluginConfigurationEntity> pluginConfigurations;

	@XmlElement(name = "planCreationConsent")
	private PlanCreationConsentEntity planCreationConsent;

	@XmlElement(name = "iconconfiguration")
	private IconConfigurationEntity iconConfiguration;

	public List<RootSectionEntity> getSections() {
		return this.sections;
	}

	public void setSections(List<RootSectionEntity> sections) {
		this.sections = sections;
	}

	public List<PluginConfigurationEntity> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationEntity> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

	public List<FieldEntity> getAllField() {
		List<FieldEntity> fieldEntities = new ArrayList<>();
		if (this.getSections() != null) {
			for (RootSectionEntity rootSectionEntity : this.getSections()) {
				fieldEntities.addAll(rootSectionEntity.getAllField());
			}
		}
		return fieldEntities;
	}

	public List<FieldEntity> getFieldById(String id) {
		return this.getAllField().stream().filter(x -> id.equals(x.getId())).toList();
	}

	public List<FieldSetEntity> getAllFieldSets() {
		List<FieldSetEntity> result = new ArrayList<>();
		if (this.getSections() != null) {
			for (RootSectionEntity section : this.getSections()) {
				result.addAll(section.getAllFieldSets());
			}
		}
		return result;
	}

	public List<FieldSetEntity> getFieldSetById(String id) {
		return this.getAllFieldSets().stream().filter(x-> id != null && id.equals(x.getId())).toList();
	}

	public List<FieldSetEntity> getFieldSetByFieldId(String fieldId) {
		if (fieldId == null) return new ArrayList<>();

		return this.getAllFieldSets().stream().filter(fieldSet -> fieldSet.getAllField().stream().anyMatch(field -> fieldId.equals(field.getId()))).toList();
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
