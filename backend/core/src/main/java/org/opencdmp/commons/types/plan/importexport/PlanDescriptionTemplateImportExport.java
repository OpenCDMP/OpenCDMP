package org.opencdmp.commons.types.plan.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class PlanDescriptionTemplateImportExport {

    @XmlElement(name = "descriptionTemplateGroupId")
    private UUID descriptionTemplateGroupId;

    @XmlElement(name = "descriptionTemplateCode")
    private String descriptionTemplateCode;

    @XmlElement(name = "sectionId")
    private UUID  sectionId;

    public UUID getDescriptionTemplateGroupId() {
        return this.descriptionTemplateGroupId;
    }

    public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) {
        this.descriptionTemplateGroupId = descriptionTemplateGroupId;
    }

    public UUID getSectionId() {
        return this.sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public String getDescriptionTemplateCode() {
        return descriptionTemplateCode;
    }

    public void setDescriptionTemplateCode(String descriptionTemplateCode) {
        this.descriptionTemplateCode = descriptionTemplateCode;
    }
}
