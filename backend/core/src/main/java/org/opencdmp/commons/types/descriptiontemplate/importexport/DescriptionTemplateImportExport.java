package org.opencdmp.commons.types.descriptiontemplate.importexport;


import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.descriptiontemplatetype.DescriptionTemplateTypeImportExport;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "descriptionTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "code")
    private String code;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "language")
    private String language;
    @XmlElement(name = "type")
    private DescriptionTemplateTypeImportExport descriptionTemplateType;
    @XmlElement(name = "version")
    private Short version;
    @XmlElement(name = "groupId")
    private UUID groupId;
    @XmlElementWrapper(name = "pages")
    @XmlElement(name = "page")
    private List<DescriptionTemplatePageImportExport> pages;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<DescriptionTemplatePageImportExport> getPages() {
        return this.pages;
    }

    public void setPages(List<DescriptionTemplatePageImportExport> pages) {
        this.pages = pages;
    }


    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public DescriptionTemplateTypeImportExport getDescriptionTemplateType() {
        return descriptionTemplateType;
    }

    public void setDescriptionTemplateType(DescriptionTemplateTypeImportExport descriptionTemplateType) {
        this.descriptionTemplateType = descriptionTemplateType;
    }

    public Short getVersion() {
        return this.version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    public UUID getGroupId() {
        return this.groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public List<DescriptionTemplateFieldSetImportExport> getAllFieldSets(){
        List<DescriptionTemplateFieldSetImportExport> fieldSets = new ArrayList<>();
        if (this.getPages() != null){
            for (DescriptionTemplatePageImportExport page: this.getPages()) {
                fieldSets.addAll(page.getAllFieldSets());
            }
        }
        return fieldSets;
    }

    public List<DescriptionTemplateFieldImportExport> getAllField(){
        List<DescriptionTemplateFieldImportExport> fields = new ArrayList<>();
        if (this.getPages() != null){
            for (DescriptionTemplatePageImportExport page: this.getPages()) {
                fields.addAll(page.getAllField());
            }
        }
        return fields;
    }

    public List<DescriptionTemplateFieldSetImportExport> getFieldSetById(String id) {
        return this.getAllFieldSets().stream().filter(x-> id.equals(x.getId())).toList();
    }

    public List<DescriptionTemplateFieldImportExport> getFieldById(String id) {
        return this.getAllField().stream().filter(x-> id.equals(x.getId())).toList();
    }
}
