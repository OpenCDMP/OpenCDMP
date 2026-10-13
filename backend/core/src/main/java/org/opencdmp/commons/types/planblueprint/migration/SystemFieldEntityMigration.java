package org.opencdmp.commons.types.planblueprint.migration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SystemFieldDataEntity;

/**
 * @deprecated Kept for migration tool backward compat.
 */
@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class SystemFieldEntityMigration extends FieldEntityMigration {

    @XmlAttribute(name = "type")
    private PlanBlueprintSystemFieldType type;

    public PlanBlueprintSystemFieldType getType() {
        return type;
    }
    public void setType(PlanBlueprintSystemFieldType type) {
        this.type = type;
    }
}
