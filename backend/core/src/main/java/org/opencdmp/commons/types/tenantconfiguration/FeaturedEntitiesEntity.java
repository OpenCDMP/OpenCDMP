package org.opencdmp.commons.types.tenantconfiguration;


import org.opencdmp.commons.types.featured.DescriptionTemplateEntity;
import org.opencdmp.commons.types.featured.PlanBlueprintEntity;

import java.util.List;

public class FeaturedEntitiesEntity {

    private List<PlanBlueprintEntity> planBlueprints;

    private List<DescriptionTemplateEntity> descriptionTemplates;

    public List<PlanBlueprintEntity> getPlanBlueprints() {
        return planBlueprints;
    }

    public void setPlanBlueprints(List<PlanBlueprintEntity> planBlueprints) {
        this.planBlueprints = planBlueprints;
    }

    public List<DescriptionTemplateEntity> getDescriptionTemplates() {
        return descriptionTemplates;
    }

    public void setDescriptionTemplates(List<DescriptionTemplateEntity> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }
}
