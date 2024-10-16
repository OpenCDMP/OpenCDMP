package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import org.opencdmp.commons.enums.PlanBlueprintSystemFieldType;

@XmlAccessorType(XmlAccessType.FIELD)
public class SystemFieldEntity extends FieldEntity {

    @XmlAttribute(name="type")
    private PlanBlueprintSystemFieldType type;

    public PlanBlueprintSystemFieldType getType() {
        return this.type;
    }
    public void setType(PlanBlueprintSystemFieldType type) {
        this.type = type;
    }
}
