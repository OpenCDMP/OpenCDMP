package org.opencdmp.model.descriptionworkflow;

import org.opencdmp.model.descriptionstatus.DescriptionStatus;

import java.util.List;

public class DescriptionWorkflowDefinition {
    public final static String _startingStatus = "startingStatus";
    private DescriptionStatus startingStatus;

    public final static String _statusTransitions = "statusTransitions";
    private List<DescriptionWorkflowDefinitionTransition> statusTransitions;


    public DescriptionStatus getStartingStatus() { return this.startingStatus; }

    public void setStartingStatus(DescriptionStatus startingStatus) { this.startingStatus = startingStatus; }

    public List<DescriptionWorkflowDefinitionTransition> getStatusTransitions() { return this.statusTransitions; }

    public void setStatusTransitions(List<DescriptionWorkflowDefinitionTransition> statusTransitions) { this.statusTransitions = statusTransitions; }
}
