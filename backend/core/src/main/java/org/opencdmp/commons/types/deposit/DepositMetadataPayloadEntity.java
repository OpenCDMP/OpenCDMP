package org.opencdmp.commons.types.deposit;

import org.opencdmp.commonmodels.models.deposit.PreviewGroupValuesModel;
import org.opencdmp.commonmodels.models.deposit.StoredPreviewFieldModel;

import java.util.List;

/**
 * The JSON payload stored in DepositMetadataEntity: the user's confirmed preview
 * overrides (scalar fields with their mapped base values, and repeatable-group
 * snapshots). Opaque to the core — field and group codes are plugin-defined.
 */
public class DepositMetadataPayloadEntity {

    private List<StoredPreviewFieldModel> fields;

    private List<PreviewGroupValuesModel> groups;

    public List<StoredPreviewFieldModel> getFields() {
        return fields;
    }

    public void setFields(List<StoredPreviewFieldModel> fields) {
        this.fields = fields;
    }

    public List<PreviewGroupValuesModel> getGroups() {
        return groups;
    }

    public void setGroups(List<PreviewGroupValuesModel> groups) {
        this.groups = groups;
    }
}
