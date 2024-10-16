package org.opencdmp.commons.types.descriptiontemplate;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SectionEntity{
    @XmlAttribute(name="id")
    private String id;
    @XmlAttribute(name="ordinal")
    private int ordinal;
    @XmlAttribute(name="title")
    private String title;
    @XmlAttribute(name="description")
    private String description;
    @XmlAttribute(name="extendedDescription")
    private String extendedDescription;
    @XmlElementWrapper(name = "sections")
    @XmlElement(name = "section")
    private List<SectionEntity> sections;
    @XmlElementWrapper(name = "fieldSets")
    @XmlElement(name = "fieldSet")
    private List<FieldSetEntity> fieldSets;

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

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SectionEntity> getSections() {
        return this.sections;
    }

    public void setSections(List<SectionEntity> sections) {
        this.sections = sections;
    }

    public List<FieldSetEntity> getFieldSets() {
        return this.fieldSets;
    }

    public void setFieldSets(List<FieldSetEntity> fieldSetEntities) {
        this.fieldSets = fieldSetEntities;
    }

    public String getExtendedDescription() {
        return this.extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public List<FieldEntity> getAllField(){
        List<FieldEntity> fieldEntities = new ArrayList<>();
        if (this.getFieldSets() != null){
            for (FieldSetEntity fieldSetEntity: this.getFieldSets()) {
                fieldEntities.addAll(fieldSetEntity.getAllField());
            }
        }
        if (this.getSections() != null){
            for (SectionEntity sectionEntity: this.getSections()) {
                fieldEntities.addAll(sectionEntity.getAllField());
            }
        }
        return fieldEntities;
    }
    
    public List<FieldSetEntity> getAllFieldSets(){
        List<FieldSetEntity> fieldSetEntities = new ArrayList<>();
        if (this.getFieldSets() != null){
            fieldSetEntities.addAll(this.getFieldSets());
        }
        if (this.getSections() != null){
            for (SectionEntity sectionEntity: this.getSections()) {
                fieldSetEntities.addAll(sectionEntity.getAllFieldSets());
            }
        }
        return fieldSetEntities;
    }
}
