package org.opencdmp.commons.types.descriptiontemplate;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class PageEntity {
    @XmlAttribute(name="id")
    private String id;
    @XmlAttribute(name="ordinal")
    private int ordinal;
    @XmlAttribute(name="title")
    private String title;

    @XmlElementWrapper(name = "sections")
    @XmlElement(name = "section")
    private List<SectionEntity> sections;

    public List<SectionEntity> getSections() {
        return this.sections;
    }

    public void setSections(List<SectionEntity> sections) {
        this.sections = sections;
    }

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
}
