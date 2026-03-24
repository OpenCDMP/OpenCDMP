package org.opencdmp.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.data.PlanUpdateRequestEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.model.planupdaterequest.PlanUpdateRequest;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanUpdateRequestQuery extends QueryBase<PlanUpdateRequestEntity> {

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UUID> excludedIds;

    private Collection<UUID> planIds;

    private Collection<PlanUpdateRequestStatus> status;

    private Collection<UUID> approvedByIds;

    private Collection<PlanUpdateRequestActionType> actionTypes;

    private final AuthorizationService authService;

    private final UserScopeFactory userScopeFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanUpdateRequestQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanUpdateRequestQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanUpdateRequestQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanUpdateRequestQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanUpdateRequestQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanUpdateRequestQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanUpdateRequestQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PlanUpdateRequestQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PlanUpdateRequestQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PlanUpdateRequestQuery planIds(UUID value) {
        this.planIds = List.of(value);
        return this;
    }

    public PlanUpdateRequestQuery planIds(UUID... value) {
        this.planIds = Arrays.asList(value);
        return this;
    }

    public PlanUpdateRequestQuery planIds(Collection<UUID> values) {
        this.planIds = values;
        return this;
    }

    public PlanUpdateRequestQuery status(PlanUpdateRequestStatus value) {
        this.status = List.of(value);
        return this;
    }

    public PlanUpdateRequestQuery status(PlanUpdateRequestStatus... value) {
        this.status = Arrays.asList(value);
        return this;
    }

    public PlanUpdateRequestQuery status(Collection<PlanUpdateRequestStatus> values) {
        this.status = values;
        return this;
    }

    public PlanUpdateRequestQuery approvedByIds(UUID value) {
        this.approvedByIds = List.of(value);
        return this;
    }

    public PlanUpdateRequestQuery approvedByIds(UUID... value) {
        this.approvedByIds = Arrays.asList(value);
        return this;
    }

    public PlanUpdateRequestQuery approvedByIds(Collection<UUID> values) {
        this.approvedByIds = values;
        return this;
    }

    public PlanUpdateRequestQuery actionTypes(PlanUpdateRequestActionType value) {
        this.actionTypes = List.of(value);
        return this;
    }

    public PlanUpdateRequestQuery actionTypes(PlanUpdateRequestActionType... value) {
        this.actionTypes = Arrays.asList(value);
        return this;
    }

    public PlanUpdateRequestQuery actionTypes(Collection<PlanUpdateRequestActionType> values) {
        this.actionTypes = values;
        return this;
    }

    public PlanUpdateRequestQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanUpdateRequestQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanUpdateRequestQuery disableTracking() {
        this.noTracking = true;
        return this;
    }


    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    public PlanUpdateRequestQuery(
            AuthorizationService authService, UserScopeFactory userScopeFactory, QueryUtilsService queryUtilsService, TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.authService = authService;
        this.userScopeFactory = userScopeFactory;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManagerFactory.getInstance().getEntityManager();
    }

    @Override
    protected Class<PlanUpdateRequestEntity> entityClass() {
        return PlanUpdateRequestEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowsePlanUpdateRequest))
            return null;

        UUID userId = null;
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated))
            userId = this.userScopeFactory.getInstance().getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    queryContext.CriteriaBuilder.and(
                            queryContext.CriteriaBuilder.equal(queryContext.Root.get(PlanUpdateRequestEntity._submitterId), userId),
                            queryContext.CriteriaBuilder.equal(queryContext.Root.get(PlanUpdateRequestEntity._actionType), PlanUpdateRequestActionType.CreatePlan)
                    ),
                    queryContext.CriteriaBuilder.and(
                            queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._planId)).value(this.queryUtilsService.buildPlanUserAuthWithRoleZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, PlanUserRole.Owner)),
                            queryContext.CriteriaBuilder.equal(queryContext.Root.get(PlanUpdateRequestEntity._actionType), PlanUpdateRequestActionType.UpdatePlan)
                    )
            ));
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return queryContext.CriteriaBuilder.or(); //Creates a false query
        }
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.planIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._planId));
            for (UUID item : this.planIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.status != null) {
            CriteriaBuilder.In<PlanUpdateRequestStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._status));
            for (PlanUpdateRequestStatus item : this.status)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.approvedByIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._approvedBy));
            for (UUID item : this.approvedByIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.actionTypes != null) {
            CriteriaBuilder.In<PlanUpdateRequestActionType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUpdateRequestEntity._actionType));
            for (PlanUpdateRequestActionType item : this.actionTypes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected PlanUpdateRequestEntity convert(Tuple tuple, Set<String> columns) {
        PlanUpdateRequestEntity item = new PlanUpdateRequestEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._id, UUID.class));
        item.setPlanId(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._planId, UUID.class));
        item.setSubmitterId(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._submitterId, UUID.class));
        item.setSourceCode(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._sourceCode, String.class));
        item.setActionType(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._actionType, PlanUpdateRequestActionType.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._status, PlanUpdateRequestStatus.class));
        item.setData(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._data, String.class));
        item.setRequestAt(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._requestAt, Instant.class));
        item.setApprovedBy(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._approvedBy, UUID.class));
        item.setApprovedAt(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._approvedAt, Instant.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._isActive, IsActive.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanUpdateRequestEntity._tenantId, UUID.class));


        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(PlanUpdateRequest._id)) return PlanUpdateRequestEntity._id;
        else if (item.match(PlanUpdateRequest._plan)) return PlanUpdateRequestEntity._planId;
        else if (item.match(PlanUpdateRequest._submitter)) return PlanUpdateRequestEntity._submitterId;
        else if (item.match(PlanUpdateRequest._actionType)) return PlanUpdateRequestEntity._actionType;
        else if (item.match(PlanUpdateRequest._status)) return PlanUpdateRequestEntity._status;
        else if (item.match(PlanUpdateRequest._sourceCode)) return PlanUpdateRequestEntity._sourceCode;
        else if (item.prefix(PlanUpdateRequest._data)) return PlanUpdateRequestEntity._data;
        else if (item.match(PlanUpdateRequest._requestAt)) return PlanUpdateRequestEntity._requestAt;
        else if (item.match(PlanUpdateRequest._approvedBy)) return PlanUpdateRequestEntity._approvedBy;
        else if (item.match(PlanUpdateRequest._approvedAt)) return PlanUpdateRequestEntity._approvedAt;
        else if (item.match(PlanUpdateRequest._createdAt)) return PlanUpdateRequestEntity._createdAt;
        else if (item.match(PlanUpdateRequest._updatedAt)) return PlanUpdateRequestEntity._updatedAt;
        else if (item.match(PlanUpdateRequest._isActive)) return PlanUpdateRequestEntity._isActive;
        else if (item.match(PlanUpdateRequest._hash)) return PlanUpdateRequestEntity._updatedAt;
        else if (item.match(PlanUpdateRequest._belongsToCurrentTenant)) return PlanUpdateRequestEntity._tenantId;
        else if (item.match(PlanUpdateRequestEntity._tenantId)) return PlanUpdateRequestEntity._tenantId;
        else return null;
    }

}
