package org.opencdmp.model;

import org.opencdmp.commons.enums.RecentActivityItemType;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;

public class RecentActivityItem {
    private RecentActivityItemType type;
    public final static String _type = "type";
    private Plan plan;
    public final static String _plan = "plan";
    private Description description;

    public final static String _description = "description";

    public RecentActivityItemType getType() {
        return type;
    }

    public void setType(RecentActivityItemType type) {
        this.type = type;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }
}



