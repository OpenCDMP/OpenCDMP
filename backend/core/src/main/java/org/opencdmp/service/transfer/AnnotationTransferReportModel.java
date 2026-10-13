package org.opencdmp.service.transfer;

/**
 * Mirror of the annotation service's transfer import report payload.
 */
public class AnnotationTransferReportModel {

    private boolean applied;

    private Counts annotations;

    private Counts annotationStatuses;

    private Counts statuses;

    public boolean isApplied() {
        return this.applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public Counts getAnnotations() {
        return this.annotations;
    }

    public void setAnnotations(Counts annotations) {
        this.annotations = annotations;
    }

    public Counts getAnnotationStatuses() {
        return this.annotationStatuses;
    }

    public void setAnnotationStatuses(Counts annotationStatuses) {
        this.annotationStatuses = annotationStatuses;
    }

    public Counts getStatuses() {
        return this.statuses;
    }

    public void setStatuses(Counts statuses) {
        this.statuses = statuses;
    }

    public static class Counts {

        private int total;

        private int created;

        private int existing;

        private int matchedByKey;

        public int getTotal() {
            return this.total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getCreated() {
            return this.created;
        }

        public void setCreated(int created) {
            this.created = created;
        }

        public int getExisting() {
            return this.existing;
        }

        public void setExisting(int existing) {
            this.existing = existing;
        }

        public int getMatchedByKey() {
            return this.matchedByKey;
        }

        public void setMatchedByKey(int matchedByKey) {
            this.matchedByKey = matchedByKey;
        }
    }
}
