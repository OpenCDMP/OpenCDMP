package org.opencdmp.model.planworkflow;

import org.opencdmp.model.planstatus.PlanStatus;

public class PlanWorkflowDefinitionTransition {

    public final static String _fromStatus = "fromStatus";
    private PlanStatus fromStatus;

    public final static String _toStatus = "toStatus";
    private PlanStatus toStatus;


    public PlanStatus getFromStatus() { return this.fromStatus; }

    public void setFromStatus(PlanStatus fromStatus) { this.fromStatus = fromStatus; }

    public PlanStatus getToStatus() { return this.toStatus; }

    public void setToStatus(PlanStatus toStatus) { this.toStatus = toStatus; }
}
