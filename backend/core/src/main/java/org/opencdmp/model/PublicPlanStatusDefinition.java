package org.opencdmp.model;

import org.opencdmp.commons.enums.PlanStatusAvailableActionType;

import java.util.List;

public class PublicPlanStatusDefinition {


    public final static String _availableActions = "availableActions";
    private List<PlanStatusAvailableActionType> availableActions;


    public List<PlanStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<PlanStatusAvailableActionType> availableActions) {
        this.availableActions = availableActions;
    }
}
