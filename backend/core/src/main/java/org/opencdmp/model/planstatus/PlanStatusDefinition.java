package org.opencdmp.model.planstatus;

import org.opencdmp.commons.enums.PlanStatusAvailableActionType;
import org.opencdmp.model.StorageFile;

import java.util.List;
import java.util.UUID;

public class PlanStatusDefinition {

    public final static String _authorization = "authorization";
    private PlanStatusDefinitionAuthorization authorization;

    public final static String _availableActions = "availableActions";
    private List<PlanStatusAvailableActionType> availableActions;

    public final static String _matIconName = "matIconName";
    private String matIconName;

    public final static String _storageFile = "storageFile";
    private StorageFile storageFile;

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
