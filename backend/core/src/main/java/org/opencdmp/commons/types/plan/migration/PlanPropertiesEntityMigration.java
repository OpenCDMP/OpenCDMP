package org.opencdmp.commons.types.plan.migration;

import org.opencdmp.commons.types.plan.PlanContactEntity;

import java.util.List;

/**
 * @deprecated Kept for migration tool backward compat.
 */
@Deprecated
public class PlanPropertiesEntityMigration {

    private List<PlanBlueprintValueEntityMigration> planBlueprintValues;

    private List<PlanContactEntity> contacts;

    public List<PlanBlueprintValueEntityMigration> getPlanBlueprintValues() {
        return this.planBlueprintValues;
    }

    public void setPlanBlueprintValues(List<PlanBlueprintValueEntityMigration> planBlueprintValues) {
        this.planBlueprintValues = planBlueprintValues;
    }

    public List<PlanContactEntity> getContacts() {
        return this.contacts;
    }

    public void setContacts(List<PlanContactEntity> contacts) {
        this.contacts = contacts;
    }

}
