package org.opencdmp.model;

public class DashboardReferenceTypeStatistics {

    private long count;

    public static final String _count = "count";

    private PublicReferenceType referenceType;

    public static final String _referenceType = "referenceType";

    /** Whether one of the reference type's sources has a logo (served by {@code GET api/reference-type/public/{id}/logo}). */
    private boolean hasLogo;

    public static final String _hasLogo = "hasLogo";

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

    public boolean getHasLogo() {
        return hasLogo;
    }

    public void setHasLogo(boolean hasLogo) {
        this.hasLogo = hasLogo;
    }
}
