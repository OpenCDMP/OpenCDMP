package org.opencdmp.model.reference;

/**
 * A reference together with how many of the plans or descriptions visible to the caller link to it.
 */
public class ReferenceUsage {

    private Reference reference;

    public static final String _reference = "reference";

    private Long count;

    public static final String _count = "count";

    public ReferenceUsage() {
    }

    public ReferenceUsage(Reference reference, Long count) {
        this.reference = reference;
        this.count = count;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
