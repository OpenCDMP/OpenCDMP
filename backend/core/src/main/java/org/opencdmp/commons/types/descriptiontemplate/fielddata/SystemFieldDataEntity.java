package org.opencdmp.commons.types.descriptiontemplate.fielddata;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import org.opencdmp.commons.enums.SystemFieldType;

@XmlAccessorType(XmlAccessType.FIELD)
public class SystemFieldDataEntity extends BaseFieldDataEntity {

    public static final String XmlElementName = "systemFieldData";

    @XmlAttribute(name = "systemFieldType")
    private SystemFieldType systemFieldType;

    public SystemFieldType getSystemFieldType() {
        return systemFieldType;
    }

    public void setSystemFieldType(SystemFieldType systemFieldType) {
        this.systemFieldType = systemFieldType;
    }
}
