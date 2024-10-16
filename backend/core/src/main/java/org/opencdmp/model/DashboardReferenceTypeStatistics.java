package org.opencdmp.model;

public class DashboardReferenceTypeStatistics {

    private long count;

    public static final String _count = "count";

    private PublicReferenceType referenceType;

    public static final String _referenceType = "referenceType";
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public PublicReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(PublicReferenceType referenceType) {
        this.referenceType = referenceType;
    }
}
