package org.opencdmp.commons.types.planstatus;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.PlanStatusAvailableActionType;

import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanStatusDefinitionEntity {

    @XmlElement(name = "authorization")
    private PlanStatusDefinitionAuthorizationEntity authorization;

    @XmlElementWrapper(name = "availableActions")
    @XmlElement(name = "action")
    private List<PlanStatusAvailableActionType> availableActions;

    @XmlElement(name = "matIconName")
    private String matIconName;

    @XmlElement(name = "storageFileId")
    private UUID storageFileId;

    @XmlElement(name = "statusColor")
    private String statusColor;

    public PlanStatusDefinitionAuthorizationEntity getAuthorization() {
        return this.authorization;
    }

    public void setAuthorization(PlanStatusDefinitionAuthorizationEntity authorization) { this.authorization = authorization; }

    public List<PlanStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<PlanStatusAvailableActionType> availableActions) {
        this.availableActions = availableActions;
    }

    public String getMatIconName() {
        return matIconName;
    }

    public void setMatIconName(String matIconName) {
        this.matIconName = matIconName;
    }

    public UUID getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(UUID storageFileId) {
        this.storageFileId = storageFileId;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
