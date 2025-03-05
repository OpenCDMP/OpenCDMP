package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.EvaluationStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.EvaluationQuery;
import org.opencdmp.query.ReferenceTypeQuery;

import java.util.List;
import java.util.UUID;

public class EvaluationLookup extends Lookup {


    private List<IsActive> isActive;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private List<UUID> entityIds;

    private List<EvaluationStatus> status;

    private List<UUID> createdByIds;

    private List<EntityType> entityTypes;



    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public List<UUID> getEntityIds() {
        return entityIds;
    }

    public void setEntityIds(List<UUID> entityIds) {
        this.entityIds = entityIds;
    }

    public List<EvaluationStatus> getStatus() {
        return status;
    }

    public void setStatus(List<EvaluationStatus> status) {
        this.status = status;
    }

    public List<UUID> getCreatedByIds() {
        return createdByIds;
    }

    public void setCreatedByIds(List<UUID> createdByIds) {
        this.createdByIds = createdByIds;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<EntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public EvaluationQuery enrich(QueryFactory queryFactory) {
        EvaluationQuery query = queryFactory.query(EvaluationQuery.class);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.entityIds != null) query.entityIds(this.entityIds);
        if (this.status != null) query.status(this.status);
        if (this.createdByIds != null) query.createdByIds(this.createdByIds);
        if (this.entityTypes != null) query.entityTypes(this.entityTypes);

        this.enrichCommon(query);

        return query;
    }

}
