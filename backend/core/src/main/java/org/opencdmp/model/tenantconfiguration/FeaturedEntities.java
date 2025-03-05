package org.opencdmp.model.tenantconfiguration;


import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.planblueprint.PlanBlueprint;

import java.util.List;

public class FeaturedEntities {

    private List<PlanBlueprint> planBlueprints;
    public static final String _planBlueprints = "planBlueprints";

    private List<DescriptionTemplate> descriptionTemplates;
    public static final String _descriptionTemplates = "descriptionTemplates";

    public List<PlanBlueprint> getPlanBlueprints() {
        return planBlueprints;
    }

    public void setPlanBlueprints(List<PlanBlueprint> planBlueprints) {
        this.planBlueprints = planBlueprints;
    }

    public List<DescriptionTemplate> getDescriptionTemplates() {
        return descriptionTemplates;
    }

    public void setDescriptionTemplates(List<DescriptionTemplate> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }
}
