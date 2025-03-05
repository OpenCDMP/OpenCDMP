package org.opencdmp.commons.types.descriptionstatus;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.DescriptionStatusAvailableActionType;

import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionStatusDefinitionEntity {

    @XmlElement(name = "authorization")
    private DescriptionStatusDefinitionAuthorizationEntity authorization;

    @XmlElementWrapper(name = "availableActions")
    @XmlElement(name = "action")
    private List<DescriptionStatusAvailableActionType> availableActions;

    @XmlElement(name = "matIconName")
    private String matIconName;

    @XmlElement(name = "storageFileId")
    private UUID storageFileId;

    @XmlElement(name = "statusColor")
    private String statusColor;

    public DescriptionStatusDefinitionAuthorizationEntity getAuthorization() { return this.authorization; }

    public void setAuthorization(DescriptionStatusDefinitionAuthorizationEntity authorization) { this.authorization = authorization; }

    public List<DescriptionStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<DescriptionStatusAvailableActionType> availableActions) {
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
