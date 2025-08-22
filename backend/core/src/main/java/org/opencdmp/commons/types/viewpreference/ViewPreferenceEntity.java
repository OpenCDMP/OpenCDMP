package org.opencdmp.commons.types.viewpreference;

import java.util.UUID;

public class ViewPreferenceEntity {

    private UUID referenceTypeId;

    private Integer ordinal;

    public UUID getReferenceTypeId() {
        return referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }
}
