package org.opencdmp.commons.types.descriptiontemplate.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateSectionImportExport {
    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "ordinal")
    private int ordinal;
    @XmlElementWrapper(name = "fieldSets")
    @XmlElement(name = "fieldSet")
    private List<DescriptionTemplateFieldSetImportExport> fieldSets;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "title")
    private String title;
    @XmlElementWrapper(name = "sections")
    @XmlElement(name = "section")
    private List<DescriptionTemplateSectionImportExport> sections;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public List<DescriptionTemplateFieldSetImportExport> getFieldSets() {
        return this.fieldSets;
    }

    public void setFieldSets(List<DescriptionTemplateFieldSetImportExport> fieldSets) {
        this.fieldSets = fieldSets;
    }

     public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DescriptionTemplateSectionImportExport> getSections() {
        return this.sections;
    }

    public void setSections(List<DescriptionTemplateSectionImportExport> sections) {
        this.sections = sections;
    }


    public List<DescriptionTemplateFieldImportExport> getAllField(){
        List<DescriptionTemplateFieldImportExport> fields = new ArrayList<>();
        if (this.getFieldSets() != null){
            for (DescriptionTemplateFieldSetImportExport fieldSet: this.getFieldSets()) {
                fields.addAll(fieldSet.getAllField());
            }
        }
        if (this.getSections() != null){
            for (DescriptionTemplateSectionImportExport section: this.getSections()) {
                fields.addAll(section.getAllField());
            }
        }
        return fields;
    }

    public List<DescriptionTemplateFieldSetImportExport> getAllFieldSets(){
        List<DescriptionTemplateFieldSetImportExport> fieldSets = new ArrayList<>();
        if (this.getFieldSets() != null){
            fieldSets.addAll(this.getFieldSets());
        }
        if (this.getSections() != null){
            for (DescriptionTemplateSectionImportExport section: this.getSections()) {
                fieldSets.addAll(section.getAllFieldSets());
            }
        }
        return fieldSets;
    }
}
