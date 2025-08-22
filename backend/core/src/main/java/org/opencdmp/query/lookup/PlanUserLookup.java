package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.NestedCollaboratorElasticQuery;
import org.opencdmp.query.PlanUserQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class PlanUserLookup extends Lookup {

    @Schema(description = SwaggerHelpers.PlanUser.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.PlanUser.planIds_description)
    private List<UUID> planIds;

    @Schema(description = SwaggerHelpers.PlanUser.userIds_description)
    private List<UUID> userIds;

    @Schema(description = SwaggerHelpers.PlanUser.sectionIds_description)
    private List<UUID> sectionIds;

    @Schema(description = SwaggerHelpers.PlanUser.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.PlanUser.userRoles_description)
    private List<PlanUserRole> userRoles;

    public List<UUID> getIds() {
        return this.ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getPlanIds() {
        return this.planIds;
    }

    public void setPlanIds(List<UUID> planIds) {
        this.planIds = planIds;
    }

    public List<UUID> getUserIds() {
        return this.userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public List<UUID> getSectionIds() {
        return this.sectionIds;
    }

    public void setSectionIds(List<UUID> sectionIds) {
        this.sectionIds = sectionIds;
    }

    public List<IsActive> getIsActive() {
        return this.isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<PlanUserRole> getUserRoles() {
        return this.userRoles;
    }

    public void setUserRoles(List<PlanUserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public PlanUserQuery enrich(QueryFactory queryFactory) {
        PlanUserQuery query = queryFactory.query(PlanUserQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.planIds != null) query.planIds(this.planIds);
        if (this.userIds != null) query.userIds(this.userIds);
        if (this.userRoles != null) query.userRoles(this.userRoles);
        if (this.sectionIds != null) query.sectionIds(this.sectionIds);
        if (this.isActive != null) query.isActives(this.isActive);

        this.enrichCommon(query);

        return query;
    }

    public NestedCollaboratorElasticQuery enrichElasticInner(QueryFactory queryFactory) {
        NestedCollaboratorElasticQuery query = queryFactory.query(NestedCollaboratorElasticQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.userIds != null) query.userIds(this.userIds);
        if (this.userRoles != null) query.userRoles(this.userRoles);
        if (this.sectionIds != null) throw new UnsupportedOperationException(""); 

        this.enrichCommon(query);

        return query;
    }
}
