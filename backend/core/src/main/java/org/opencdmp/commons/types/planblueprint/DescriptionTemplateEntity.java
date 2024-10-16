package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateEntity {

    @XmlAttribute(name="descriptionTemplateGroupId")
    private UUID descriptionTemplateGroupId;
    @XmlAttribute(name="label")
    private String label;
    @XmlAttribute(name="minMultiplicity")
    private Integer minMultiplicity;
    @XmlAttribute(name="maxMultiplicity")
    private Integer maxMultiplicity;

    public UUID getDescriptionTemplateGroupId() {
        return this.descriptionTemplateGroupId;
    }

    public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) {
        this.descriptionTemplateGroupId = descriptionTemplateGroupId;
    }

    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getMinMultiplicity() {
        return this.minMultiplicity;
    }
    public void setMinMultiplicity(Integer minMultiplicity) {
        this.minMultiplicity = minMultiplicity;
    }

    public Integer getMaxMultiplicity() {
        return this.maxMultiplicity;
    }
    public void setMaxMultiplicity(Integer maxMultiplicity) {
        this.maxMultiplicity = maxMultiplicity;
    }

}
