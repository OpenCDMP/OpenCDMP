package org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.SystemFieldType;

import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class SystemFieldDataImportExport extends BaseFieldDataImportExport {
    public static final String XmlElementName = "systemData";

    @XmlElement(name = "type")
    private SystemFieldType systemFieldType;

    public SystemFieldType getSystemFieldType() {
        return systemFieldType;
    }

    public void setSystemFieldType(SystemFieldType systemFieldType) {
        this.systemFieldType = systemFieldType;
    }
}

