package org.opencdmp.commons.types.plan;

import java.util.UUID;

/**
 * Payload of the Plan additional_info column: server-owned metadata that is never
 * rebuilt from client persist models. Only facts read by entity id belong here —
 * anything a query must filter on needs a real column or table instead.
 */
public class PlanAdditionalInfoEntity {

    // Provenance: the plan this one was derived from (clone, public clone, tenant
    // copy, or the base version of a new plan version).
    private UUID clonedFromPlanId;

    public UUID getClonedFromPlanId() {
        return clonedFromPlanId;
    }

    public void setClonedFromPlanId(UUID clonedFromPlanId) {
        this.clonedFromPlanId = clonedFromPlanId;
    }
}
