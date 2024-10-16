package org.opencdmp.model.descriptionworkflow;

import org.opencdmp.model.descriptionstatus.DescriptionStatus;

public class DescriptionWorkflowDefinitionTransition {
    public final static String _fromStatus = "fromStatus";
    private DescriptionStatus fromStatus;

    public final static String _toStatus = "toStatus";
    private DescriptionStatus toStatus;


    public DescriptionStatus getFromStatus() { return this.fromStatus; }

    public void setFromStatus(DescriptionStatus fromStatus) { this.fromStatus = fromStatus; }

    public DescriptionStatus getToStatus() { return this.toStatus; }

    public void setToStatus(DescriptionStatus toStatus) { this.toStatus = toStatus; }

}
