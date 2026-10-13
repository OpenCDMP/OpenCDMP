package org.opencdmp.commons.types.descriptionstatus;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.DescriptionStatusAvailableActionType;
import org.opencdmp.commons.types.iconconfiguration.IconConfigurationEntity;

import java.util.List;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionStatusDefinitionEntity {

    @XmlElement(name = "authorization")
    private DescriptionStatusDefinitionAuthorizationEntity authorization;

    @XmlElementWrapper(name = "availableActions")
    @XmlElement(name = "action")
    private List<DescriptionStatusAvailableActionType> availableActions;

    @XmlElement(name = "iconconfiguration")
    private IconConfigurationEntity iconConfiguration;

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

    public IconConfigurationEntity getIconConfiguration() {
        return iconConfiguration;
    }

    public void setIconConfiguration(IconConfigurationEntity iconConfiguration) {
        this.iconConfiguration = iconConfiguration;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
