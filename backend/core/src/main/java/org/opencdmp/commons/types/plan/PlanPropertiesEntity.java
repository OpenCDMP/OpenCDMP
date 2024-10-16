package org.opencdmp.commons.types.plan;

import java.util.List;

public class PlanPropertiesEntity {

    private List<PlanBlueprintValueEntity> planBlueprintValues;

    private List<PlanContactEntity> contacts;

    public List<PlanBlueprintValueEntity> getPlanBlueprintValues() {
        return this.planBlueprintValues;
    }

    public void setPlanBlueprintValues(List<PlanBlueprintValueEntity> planBlueprintValues) {
        this.planBlueprintValues = planBlueprintValues;
    }

    public List<PlanContactEntity> getContacts() {
        return this.contacts;
    }

    public void setContacts(List<PlanContactEntity> contacts) {
        this.contacts = contacts;
    }

}
