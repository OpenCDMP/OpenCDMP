package org.opencdmp.model;

import java.util.UUID;

public class PublicPlanDescriptionTemplate {

    private UUID id;

    public static final String _id = "id";

    private PublicPlan plan;

    public static final String _plan = "plan";

    private UUID sectionId;

    public static final String _sectionId= "sectionId";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PublicPlan getPlan() {
        return plan;
    }

    public void setPlan(PublicPlan plan) {
        this.plan = plan;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }
}
