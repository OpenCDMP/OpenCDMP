package org.opencdmp.model.descriptiontemplate.fielddata;

import org.opencdmp.commons.enums.SystemFieldType;

public class SystemFieldData extends BaseFieldData {

    public final static String _systemFieldType = "systemFieldType";
    private SystemFieldType systemFieldType;

    public SystemFieldType getSystemFieldType() {
        return systemFieldType;
    }

    public void setSystemFieldType(SystemFieldType systemFieldType) {
        this.systemFieldType = systemFieldType;
    }
}
