package org.opencdmp.commons.types.description;

import java.util.UUID;

/**
 * Payload of the Description additional_info column: server-owned metadata that is
 * never rebuilt from client persist models. Only facts read by entity id belong here —
 * anything a query must filter on needs a real column or table instead.
 */
public class DescriptionAdditionalInfoEntity {

    // Provenance: the description this one was cloned from (plan clone, new plan
    // version, tenant copy, or asset clone).
    private UUID clonedFromDescriptionId;

    public UUID getClonedFromDescriptionId() {
        return clonedFromDescriptionId;
    }

    public void setClonedFromDescriptionId(UUID clonedFromDescriptionId) {
        this.clonedFromDescriptionId = clonedFromDescriptionId;
    }
}
