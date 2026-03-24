package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.*;
import org.opencdmp.query.PlanUpdateRequestQuery;

import java.util.List;
import java.util.UUID;

public class PlanUpdateRequestLookup extends Lookup {

    private List<IsActive> isActive;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private List<UUID> planIds;

    private List<PlanUpdateRequestStatus> status;

    private List<UUID> approvedByIds;

    private List<PlanUpdateRequestActionType> actionTypes;

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

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<UUID> getPlanIds() {
        return planIds;
    }

    public void setPlanIds(List<UUID> planIds) {
        this.planIds = planIds;
    }

    public List<PlanUpdateRequestStatus> getStatus() {
        return status;
    }

    public void setStatus(List<PlanUpdateRequestStatus> status) {
        this.status = status;
    }

    public List<UUID> getApprovedByIds() {
        return approvedByIds;
    }

    public void setApprovedByIds(List<UUID> approvedByIds) {
        this.approvedByIds = approvedByIds;
    }

    public List<PlanUpdateRequestActionType> getActionTypes() {
        return actionTypes;
    }

    public void setActionTypes(List<PlanUpdateRequestActionType> actionTypes) {
        this.actionTypes = actionTypes;
    }

    public PlanUpdateRequestQuery enrich(QueryFactory queryFactory) {
        PlanUpdateRequestQuery query = queryFactory.query(PlanUpdateRequestQuery.class);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.planIds != null) query.planIds(this.planIds);
        if (this.status != null) query.status(this.status);
        if (this.approvedByIds != null) query.approvedByIds(this.approvedByIds);
        if (this.actionTypes != null) query.actionTypes(this.actionTypes);

        this.enrichCommon(query);

        return query;
    }

}
