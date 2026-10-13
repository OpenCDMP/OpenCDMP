package org.opencdmp.commons.types.planblueprint.migration;

import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * @deprecated Kept for migration tool backward compat.
 */
@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtraFieldEntityMigration extends FieldEntityMigration {

    @XmlAttribute(name="type")
    private PlanBlueprintExtraFieldDataType type;

    public PlanBlueprintExtraFieldDataType getType() {
        return type;
    }
    public void setType(PlanBlueprintExtraFieldDataType type) {
        this.type = type;
    }

}
