package org.opencdmp.commons.types.descriptionworkflow;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.UUID;

@XmlRootElement(name = "statusTransitions")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionWorkflowDefinitionTransitionEntity {

    @XmlElement(name = "fromStatusId")
    private UUID fromStatusId;
    public final static String _fromStatusId = "fromStatusId";

    @XmlElement(name = "toStatusId")
    private UUID toStatusId;
    public final static String _toStatusId = "toStatusId";

    public UUID getFromStatusId() { return fromStatusId; }

    public void setFromStatusId(UUID fromStatusId) { this.fromStatusId = fromStatusId; }

    public UUID getToStatusId() { return toStatusId; }

    public void setToStatusId(UUID toStatusId) { this.toStatusId = toStatusId; }
}
