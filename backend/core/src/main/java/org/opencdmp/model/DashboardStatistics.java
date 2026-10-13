package org.opencdmp.model;

import java.util.List;

public class DashboardStatistics {

    private long planCount;

    public static final String _planCount = "planCount";

    private long descriptionCount;
    
    private List<DashboardReferenceTypeStatistics> referenceTypeStatistics;

    private Long planBlueprintsCount;

    private Long descriptionTemplatesCount;

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

    public Long getPlanBlueprintsCount() {
        return planBlueprintsCount;
    }

    public void setPlanBlueprintsCount(Long planBlueprintsCount) {
        this.planBlueprintsCount = planBlueprintsCount;
    }

    public Long getDescriptionTemplatesCount() {
        return descriptionTemplatesCount;
    }

    public void setDescriptionTemplatesCount(Long descriptionTemplatesCount) {
        this.descriptionTemplatesCount = descriptionTemplatesCount;
    }
}
