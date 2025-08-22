package org.opencdmp.commons.types.tenantconfiguration;


import org.opencdmp.commons.types.viewpreference.ViewPreferenceEntity;

import java.util.List;

public class ViewPreferencesConfigurationEntity {

    private List<ViewPreferenceEntity> planPreferences;

    private List<ViewPreferenceEntity> descriptionPreferences;

    public List<ViewPreferenceEntity> getPlanPreferences() {
        return planPreferences;
    }

    public void setPlanPreferences(List<ViewPreferenceEntity> planPreferences) {
        this.planPreferences = planPreferences;
    }

    public List<ViewPreferenceEntity> getDescriptionPreferences() {
        return descriptionPreferences;
    }

    public void setDescriptionPreferences(List<ViewPreferenceEntity> descriptionPreferences) {
        this.descriptionPreferences = descriptionPreferences;
    }
}
