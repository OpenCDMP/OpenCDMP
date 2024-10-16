package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;

import java.util.List;
import java.util.Map;

public class DatasetProfile implements PropertiesModelBuilder {
    private String description;
    private String language;
    private String type;
    private List<Section> sections;
    private List<Rule> rules;
    private List<Page> pages;
    private int status;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }


    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    @Override
    public void fromJsonObject(Map<String, Object> properties) {
        this.sections.forEach(item -> item.fromJsonObject(properties));

    }

    @Override
    public void fromJsonObject(Map<String, Object> properties, String index) {
        // TODO Auto-generated method stub

    }

}
