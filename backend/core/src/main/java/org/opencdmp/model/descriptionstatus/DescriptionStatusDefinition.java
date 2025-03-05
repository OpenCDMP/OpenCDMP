package org.opencdmp.model.descriptionstatus;

import org.opencdmp.commons.enums.DescriptionStatusAvailableActionType;
import org.opencdmp.model.StorageFile;

import java.util.List;

public class DescriptionStatusDefinition {

    public final static String _authorization = "authorization";
    private DescriptionStatusDefinitionAuthorization authorization;

    public final static String _availableActions = "availableActions";
    private List<DescriptionStatusAvailableActionType> availableActions;

    public final static String _matIconName = "matIconName";
    private String matIconName;

    public final static String _storageFile = "storageFile";
    private StorageFile storageFile;

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

    public String getMatIconName() {
        return matIconName;
    }

    public void setMatIconName(String matIconName) {
        this.matIconName = matIconName;
    }

    public StorageFile getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(StorageFile storageFile) {
        this.storageFile = storageFile;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
