package org.opencdmp.model.planstatus;

import org.opencdmp.commons.enums.PlanStatusAvailableActionType;
import org.opencdmp.model.iconconfiguration.IconConfiguration;

import java.util.List;

public class PlanStatusDefinition {

    public final static String _authorization = "authorization";
    private PlanStatusDefinitionAuthorization authorization;

    public final static String _availableActions = "availableActions";
    private List<PlanStatusAvailableActionType> availableActions;

    private IconConfiguration iconConfiguration;
    public final static String _iconConfiguration = "iconConfiguration";

    public final static String _statusColor = "statusColor";
    private String statusColor;

    public PlanStatusDefinitionAuthorization getAuthorization() { return this.authorization; }

    public void setAuthorization(PlanStatusDefinitionAuthorization authorization) { this.authorization = authorization; }

    public List<PlanStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<PlanStatusAvailableActionType> availableActions) {
        this.availableActions = availableActions;
    }

    public IconConfiguration getIconConfiguration() {
        return iconConfiguration;
    }

    public void setIconConfiguration(IconConfiguration iconConfiguration) {
        this.iconConfiguration = iconConfiguration;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
