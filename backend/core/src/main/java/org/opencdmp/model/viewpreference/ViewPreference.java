package org.opencdmp.model.viewpreference;

import org.opencdmp.model.referencetype.ReferenceType;


public class ViewPreference {

    private ReferenceType referenceType;
    public final static String _referenceType = "referenceType";

    private Integer ordinal;
    public final static String _ordinal = "ordinal";

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }
}
