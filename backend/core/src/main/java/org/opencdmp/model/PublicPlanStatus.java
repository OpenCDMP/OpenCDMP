package org.opencdmp.model;


import org.opencdmp.commons.enums.PlanStatus;

import java.util.UUID;

public class PublicPlanStatus {

    public final static String _id = "id";
    private UUID id;

    public final static String _name = "name";
    private String name;

    public final static String _internalStatus = "internalStatus";
    private PlanStatus internalStatus;

    public final static String _definition = "definition";
    private PublicPlanStatusDefinition definition;

    public UUID getId() { return this.id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public PlanStatus getInternalStatus() {
        return internalStatus;
    }

    public void setInternalStatus(PlanStatus internalStatus) {
        this.internalStatus = internalStatus;
    }

    public PublicPlanStatusDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(PublicPlanStatusDefinition definition) {
        this.definition = definition;
    }
}
