package org.opencdmp.commons.types.planblueprint.migration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import java.util.UUID;

/**
 * @deprecated Use {@link FieldEntityMigration} with {@link ReferenceTypeFieldEntityMigration} as {@code data} instead.
 *             Kept for migration tool backward compat.
 */
@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceTypeFieldEntityMigration extends FieldEntityMigration {

    @XmlAttribute(name="referenceTypeId")
    private UUID referenceTypeId;

    @XmlAttribute(name = "multipleSelect")
    private Boolean multipleSelect;
    public UUID getReferenceTypeId() {
        return this.referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public Boolean getMultipleSelect() {
        return this.multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }
}
