package org.opencdmp.model;

import java.util.List;

public class PublicPlanProperties {

    private List<PublicPlanBlueprintValue> planBlueprintValues;

    public static final String _planBlueprintValues = "planBlueprintValues";

    private List<PublicPlanContact> contacts;

    public static final String _contacts = "contacts";

    public List<PublicPlanBlueprintValue> getPlanBlueprintValues() {
        return planBlueprintValues;
    }

    public void setPlanBlueprintValues(List<PublicPlanBlueprintValue> planBlueprintValues) {
        this.planBlueprintValues = planBlueprintValues;
    }

    public List<PublicPlanContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<PublicPlanContact> contacts) {
        this.contacts = contacts;
    }

}
