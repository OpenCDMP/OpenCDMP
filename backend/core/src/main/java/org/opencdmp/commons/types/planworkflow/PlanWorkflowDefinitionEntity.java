package org.opencdmp.commons.types.planworkflow;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanWorkflowDefinitionEntity {

    private UUID startingStatusId;
    public final static String _startingStatusId = "startingStatusId";

    private List<PlanWorkflowDefinitionTransitionEntity> statusTransitions;
    public final static String _statusTransitions = "statusTransitions";

    public UUID getStartingStatusId() { return this.startingStatusId; }

    public void setStartingStatusId(UUID startingStatusId) { this.startingStatusId = startingStatusId; }

    public List<PlanWorkflowDefinitionTransitionEntity> getStatusTransitions() { return this.statusTransitions; }

    public void setStatusTransitions(List<PlanWorkflowDefinitionTransitionEntity> statusTransitions) { this.statusTransitions = statusTransitions; }
}
