package org.opencdmp.commons.types.planblueprint;

import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class ExtraFieldEntity extends FieldEntity {

    @XmlAttribute(name="type")
    private PlanBlueprintExtraFieldDataType type;

    public PlanBlueprintExtraFieldDataType getType() {
        return type;
    }
    public void setType(PlanBlueprintExtraFieldDataType type) {
        this.type = type;
    }

}
