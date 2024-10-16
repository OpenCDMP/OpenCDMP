package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.NestedPlanDescriptionTemplateElasticQuery;
import org.opencdmp.query.PlanDescriptionTemplateQuery;

import java.util.List;
import java.util.UUID;

public class PlanDescriptionTemplateLookup extends Lookup {

    private List<UUID> ids;
    private List<UUID> planIds;
    private List<UUID> descriptionTemplateGroupIds;
    private List<UUID> sectionIds;

    private List<UUID> excludedIds;

    private List<IsActive> isActive;

    public List<UUID> getIds() {
        return this.ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return this.excludedIds;
    }

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<IsActive> getIsActive() {
        return this.isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<UUID> getPlanIds() {
        return this.planIds;
    }

    public void setPlanIds(List<UUID> planIds) {
        this.planIds = planIds;
    }

    public List<UUID> getDescriptionTemplateGroupIds() {
        return this.descriptionTemplateGroupIds;
    }

    public List<UUID> getSectionIds() {
        return this.sectionIds;
    }

    public void setSectionIds(List<UUID> sectionIds) {
        this.sectionIds = sectionIds;
    }

    public void setDescriptionTemplateGroupIds(List<UUID> descriptionTemplateGroupIds) {
        this.descriptionTemplateGroupIds = descriptionTemplateGroupIds;
    }

    public PlanDescriptionTemplateQuery enrich(QueryFactory queryFactory) {
        PlanDescriptionTemplateQuery query = queryFactory.query(PlanDescriptionTemplateQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.planIds != null) query.planIds(this.planIds);
        if (this.descriptionTemplateGroupIds != null) query.descriptionTemplateGroupIds(this.descriptionTemplateGroupIds);
        if (this.sectionIds != null) query.sectionIds(this.sectionIds);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.isActive != null) query.isActive(this.isActive);

        this.enrichCommon(query);

        return query;
    }

    public NestedPlanDescriptionTemplateElasticQuery enrichElasticInner(QueryFactory queryFactory) {
        NestedPlanDescriptionTemplateElasticQuery query = queryFactory.query(NestedPlanDescriptionTemplateElasticQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.planIds != null) query.planIds(this.planIds);
        if (this.descriptionTemplateGroupIds != null) query.descriptionTemplateGroupIds(this.descriptionTemplateGroupIds);
        if (this.sectionIds != null) query.sectionIds(this.sectionIds);
        if (this.excludedIds != null) query.descriptionTemplateGroupIds(this.excludedIds);

        this.enrichCommon(query);

        return query;
    }

}
