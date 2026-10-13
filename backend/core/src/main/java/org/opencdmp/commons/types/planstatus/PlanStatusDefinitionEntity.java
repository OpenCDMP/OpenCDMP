package org.opencdmp.commons.types.planstatus;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.PlanStatusAvailableActionType;
import org.opencdmp.commons.types.iconconfiguration.IconConfigurationEntity;

import java.util.List;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanStatusDefinitionEntity {

    @XmlElement(name = "authorization")
    private PlanStatusDefinitionAuthorizationEntity authorization;

    @XmlElementWrapper(name = "availableActions")
    @XmlElement(name = "action")
    private List<PlanStatusAvailableActionType> availableActions;

    @XmlElement(name = "iconconfiguration")
    private IconConfigurationEntity iconConfiguration;

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
