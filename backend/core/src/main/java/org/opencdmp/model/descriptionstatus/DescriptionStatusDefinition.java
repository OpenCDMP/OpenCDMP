package org.opencdmp.model.descriptionstatus;

import org.opencdmp.commons.enums.DescriptionStatusAvailableActionType;
import org.opencdmp.model.iconconfiguration.IconConfiguration;

import java.util.List;

public class DescriptionStatusDefinition {

    public final static String _authorization = "authorization";
    private DescriptionStatusDefinitionAuthorization authorization;

    public final static String _availableActions = "availableActions";
    private List<DescriptionStatusAvailableActionType> availableActions;

    private IconConfiguration iconConfiguration;
    public final static String _iconConfiguration = "iconConfiguration";;

    public final static String _statusColor = "statusColor";
    private String statusColor;

    public DescriptionStatusDefinitionAuthorization getAuthorization() { return this.authorization; }

    public void setAuthorization(DescriptionStatusDefinitionAuthorization authorization) { this.authorization = authorization; }

    public List<DescriptionStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<DescriptionStatusAvailableActionType> availableActions) {
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
