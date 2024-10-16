package org.opencdmp.commons.types.descriptiontemplate;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefinitionEntity {
    @XmlElementWrapper(name = "pages")
    @XmlElement(name = "page")
    private List<PageEntity> pages;

    public List<PageEntity> getPages() {
        return pages;
    }

    public void setPages(List<PageEntity> pageEntities) {
        this.pages = pageEntities;
    }

    public List<FieldEntity> getAllField(){
        List<FieldEntity> fieldEntities = new ArrayList<>();
        if (this.getPages() != null){
            for (PageEntity sectionEntity: this.getPages()) {
                fieldEntities.addAll(sectionEntity.getAllField());
            }
        }
        return fieldEntities;
    }

    public List<FieldSetEntity> getAllFieldSets(){
        List<FieldSetEntity> fieldSetsEntities = new ArrayList<>();
        if (this.getPages() != null){
            for (PageEntity sectionEntity: this.getPages()) {
                fieldSetsEntities.addAll(sectionEntity.getAllFieldSets());
            }
        }
        return fieldSetsEntities;
    }

    public List<FieldSetEntity> getFieldSetById(String id) {
        return this.getAllFieldSets().stream().filter(x-> id.equals(x.getId())).toList();
    }

    public List<FieldEntity> getFieldById(String id) {
        return this.getAllField().stream().filter(x-> id.equals(x.getId())).toList();
    }
}
