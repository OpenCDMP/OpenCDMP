package org.opencdmp.model;

import java.util.List;

public class DashboardStatistics {

    private long planCount;

    public static final String _planCount = "planCount";

    private long descriptionCount;
    
    private List<DashboardReferenceTypeStatistics> referenceTypeStatistics;

    public long getPlanCount() {
        return planCount;
    }

    public void setPlanCount(long planCount) {
        this.planCount = planCount;
    }

    public long getDescriptionCount() {
        return descriptionCount;
    }

    public void setDescriptionCount(long descriptionCount) {
        this.descriptionCount = descriptionCount;
    }

    public List<DashboardReferenceTypeStatistics> getReferenceTypeStatistics() {
        return referenceTypeStatistics;
    }

    public void setReferenceTypeStatistics(List<DashboardReferenceTypeStatistics> referenceTypeStatistics) {
        this.referenceTypeStatistics = referenceTypeStatistics;
    }
}
