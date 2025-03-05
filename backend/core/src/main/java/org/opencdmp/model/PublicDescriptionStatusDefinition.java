package org.opencdmp.model;

import org.opencdmp.commons.enums.DescriptionStatusAvailableActionType;

import java.util.List;

public class PublicDescriptionStatusDefinition {


    public final static String _availableActions = "availableActions";
    private List<DescriptionStatusAvailableActionType> availableActions;

    public List<DescriptionStatusAvailableActionType> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<DescriptionStatusAvailableActionType> availableActions) {
        this.availableActions = availableActions;
    }
}
