package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefinitionEntity {
	@XmlElementWrapper(name = "sections")
	@XmlElement(name = "section")
	private List<SectionEntity> sections;

	@XmlElementWrapper(name = "pluginConfigurations")
	@XmlElement(name = "pluginConfiguration")
	private List<PluginConfigurationEntity> pluginConfigurations;

	public List<SectionEntity> getSections() {
		return this.sections;
	}

	public void setSections(List<SectionEntity> sections) {
		this.sections = sections;
	}

	public List<PluginConfigurationEntity> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationEntity> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

	public List<FieldEntity> getAllField(){
		List<FieldEntity> fieldEntities = new ArrayList<>();
		if (this.getSections() != null){
			for (SectionEntity sectionEntity: this.getSections()) {
				fieldEntities.addAll(sectionEntity.getAllField());
			}
		}
		return fieldEntities;
	}

	public List<FieldEntity> getFieldById(UUID id) {
		return this.getAllField().stream().filter(x-> id.equals(x.getId())).toList();
	}
}
