package org.opencdmp.model.tenantconfiguration;


import org.opencdmp.model.viewpreference.ViewPreference;

import java.util.List;

public class ViewPreferencesConfiguration {

    private List<ViewPreference> planPreferences;
    public static final String _planPreferences = "planPreferences";

    private List<ViewPreference> descriptionPreferences;
    public static final String _descriptionPreferences = "descriptionPreferences";

    public List<ViewPreference> getPlanPreferences() {
        return planPreferences;
    }

    public void setPlanPreferences(List<ViewPreference> planPreferences) {
        this.planPreferences = planPreferences;
    }

    public List<ViewPreference> getDescriptionPreferences() {
        return descriptionPreferences;
    }

    public void setDescriptionPreferences(List<ViewPreference> descriptionPreferences) {
        this.descriptionPreferences = descriptionPreferences;
    }
}
