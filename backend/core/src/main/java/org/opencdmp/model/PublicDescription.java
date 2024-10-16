package org.opencdmp.model;

import org.opencdmp.commons.enums.DescriptionStatus;

import java.time.Instant;
import java.util.UUID;

public class PublicDescription {

    private UUID id;

    public static final String _id = "id";

    private String label;

    public static final String _label = "label";

    private DescriptionStatus status;

    public static final String _status = "status";

    private String description;

    public static final String _description = "description";

    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    private Instant finalizedAt;

    public static final String _finalizedAt = "finalizedAt";

    private PublicPlanDescriptionTemplate planDescriptionTemplate;

    public static final String _planDescriptionTemplate = "planDescriptionTemplate";

    private PublicDescriptionTemplate descriptionTemplate;

    public static final String _descriptionTemplate = "descriptionTemplate";

    private PublicPlan plan;

    public static final String _plan = "plan";


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public DescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(DescriptionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public PublicPlanDescriptionTemplate getPlanDescriptionTemplate() {
        return planDescriptionTemplate;
    }

    public void setPlanDescriptionTemplate(PublicPlanDescriptionTemplate planDescriptionTemplate) {
        this.planDescriptionTemplate = planDescriptionTemplate;
    }

    public PublicDescriptionTemplate getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public void setDescriptionTemplate(PublicDescriptionTemplate descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public PublicPlan getPlan() {
        return plan;
    }

    public void setPlan(PublicPlan plan) {
        this.plan = plan;
    }
}
