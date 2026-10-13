package org.opencdmp.commons.types.descriptiontemplate;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.iconconfiguration.IconConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefinitionEntity {
    @XmlElementWrapper(name = "pages")

    // Pages will me remain ONLY for this class for migration purposes
    @XmlElement(name = "page")
    private List<PageEntity> pages;

    @XmlElementWrapper(name = "sections")
    @XmlElement(name = "section")
    private List<SectionEntity> sections;

    @XmlElementWrapper(name = "pluginConfigurations")
    @XmlElement(name = "pluginConfiguration")
    private List<PluginConfigurationEntity> pluginConfigurations;

    @XmlElement(name = "iconconfiguration")
    private IconConfigurationEntity iconConfiguration;

    public List<PageEntity> getPages() {
        return pages;
    }

    public void setPages(List<PageEntity> pageEntities) {
        this.pages = pageEntities;
    }

    public List<SectionEntity> getSections() {
        return sections;
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

    public List<FieldSetEntity> getAllFieldSets(){
        List<FieldSetEntity> fieldSetsEntities = new ArrayList<>();
        if (this.getSections() != null){
            for (SectionEntity sectionEntity: this.getSections()) {
                fieldSetsEntities.addAll(sectionEntity.getAllFieldSets());
            }
        }
        return fieldSetsEntities;
    }

    public List<FieldSetEntity> getFieldSetById(String id) {
        return this.getAllFieldSets().stream().filter(x-> id != null && id.equals(x.getId())).toList();
    }

    public List<FieldEntity> getFieldById(String id) {
        return this.getAllField().stream().filter(x-> id != null && id.equals(x.getId())).toList();
    }

    public IconConfigurationEntity getIconConfiguration() {
        return iconConfiguration;
    }

    public void setIconConfiguration(IconConfigurationEntity iconConfiguration) {
        this.iconConfiguration = iconConfiguration;
    }
}
