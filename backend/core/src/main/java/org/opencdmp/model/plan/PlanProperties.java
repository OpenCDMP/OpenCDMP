package org.opencdmp.model.plan;

import java.util.List;

public class PlanProperties {

    private List<PlanBlueprintValue> planBlueprintValues;

    public static final String _planBlueprintValues = "planBlueprintValues";

    private List<PlanContact> contacts;

    public static final String _contacts = "contacts";

    public List<PlanBlueprintValue> getPlanBlueprintValues() {
        return planBlueprintValues;
    }

    public void setPlanBlueprintValues(List<PlanBlueprintValue> planBlueprintValues) {
        this.planBlueprintValues = planBlueprintValues;
    }

    public List<PlanContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<PlanContact> contacts) {
        this.contacts = contacts;
    }

}
