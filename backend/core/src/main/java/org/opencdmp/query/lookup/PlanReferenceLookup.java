package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.elastic.query.NestedReferenceElasticQuery;
import org.opencdmp.query.PlanReferenceQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class PlanReferenceLookup extends Lookup {

    @Schema(description = SwaggerHelpers.PlanReference.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.PlanReference.planIds_description)
    private List<UUID> planIds;

    @Schema(description = SwaggerHelpers.PlanReference.referenceIds_description)
    private List<UUID> referenceIds;

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getPlanIds() {
        return planIds;
    }

    public void setPlanIds(List<UUID> planIds) {
        this.planIds = planIds;
    }

    public List<UUID> getReferenceIds() {
        return referenceIds;
    }

    public void setReferenceIds(List<UUID> referenceIds) {
        this.referenceIds = referenceIds;
    }

    public PlanReferenceQuery enrich(QueryFactory queryFactory) {
        PlanReferenceQuery query = queryFactory.query(PlanReferenceQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.planIds != null) query.planIds(this.planIds);
        if (this.referenceIds != null) query.referenceIds(this.referenceIds);

        this.enrichCommon(query);

        return query;
    }

    public NestedReferenceElasticQuery enrichElasticInner(QueryFactory queryFactory) {
        NestedReferenceElasticQuery query = queryFactory.query(NestedReferenceElasticQuery.class);
        if (this.ids != null) throw new UnsupportedOperationException("");
        if (this.planIds != null) throw new UnsupportedOperationException("");
        if (this.referenceIds != null) query.ids(this.referenceIds);

        this.enrichCommon(query);

        return query;
    }
}
