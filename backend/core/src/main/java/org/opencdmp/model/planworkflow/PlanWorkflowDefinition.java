package org.opencdmp.model.planworkflow;

import org.opencdmp.model.planstatus.PlanStatus;

import java.util.List;

public class PlanWorkflowDefinition {

    public final static String _startingStatus = "startingStatus";
    private PlanStatus startingStatus;

    public final static String _statusTransitions = "statusTransitions";
    private List<PlanWorkflowDefinitionTransition> statusTransitions;


    public PlanStatus getStartingStatus() { return this.startingStatus; }

    public void setStartingStatus(PlanStatus startingStatus) { this.startingStatus = startingStatus; }

    public List<PlanWorkflowDefinitionTransition> getStatusTransitions() { return this.statusTransitions; }

    public void setStatusTransitions(List<PlanWorkflowDefinitionTransition> statusTransitions) { this.statusTransitions = statusTransitions; }
}
