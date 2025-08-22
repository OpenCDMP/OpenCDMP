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
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanReferenceEntity;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.PublicPlanReference;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanReferenceQuery extends QueryBase<PlanReferenceEntity> {

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UUID> planIds;

    private Collection<UUID> referenceIds;
    private PlanQuery planQuery;
    private ReferenceQuery referenceQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    public PlanReferenceQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanReferenceQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanReferenceQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanReferenceQuery isActives(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanReferenceQuery isActives(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanReferenceQuery isActives(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanReferenceQuery planIds(UUID value) {
        this.planIds = List.of(value);
        return this;
    }

    public PlanReferenceQuery planIds(UUID... value) {
        this.planIds = Arrays.asList(value);
        return this;
    }

    public PlanReferenceQuery planIds(Collection<UUID> values) {
        this.planIds = values;
        return this;
    }

    public PlanReferenceQuery referenceIds(UUID value) {
        this.referenceIds = List.of(value);
        return this;
    }

    public PlanReferenceQuery referenceIds(UUID... value) {
        this.referenceIds = Arrays.asList(value);
        return this;
    }

    public PlanReferenceQuery referenceIds(Collection<UUID> values) {
        this.referenceIds = values;
        return this;
    }

    public PlanReferenceQuery planSubQuery(PlanQuery value) {
        this.planQuery = value;
        return this;
    }

    public PlanReferenceQuery referenceSubQuery(ReferenceQuery value) {
        this.referenceQuery = value;
        return this;
    }

    public PlanReferenceQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanReferenceQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanReferenceQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final UserScope userScope;

    private final AuthorizationService authService;
    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public PlanReferenceQuery(
		    UserScope userScope,
		    AuthorizationService authService,
		    QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<PlanReferenceEntity> entityClass() {
        return PlanReferenceEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.planIds) || this.isEmpty(this.referenceIds) || this.isFalseQuery(this.planQuery) || this.isFalseQuery(this.referenceQuery);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowsePlanReference)) return null;
        UUID userId = null;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated)) userId = this.userScope.getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic ) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._planId)).value(this.queryUtilsService.buildPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic)));
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
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.planIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._planId));
            for (UUID item : this.planIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.referenceIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._referenceId));
            for (UUID item : this.referenceIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.planQuery != null) {
            QueryContext<PlanEntity, UUID> subQuery = this.applySubQuery(this.planQuery, queryContext, UUID.class, planEntityRoot -> planEntityRoot.get(PlanEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._planId)).value(subQuery.Query));
        }
        if (this.referenceQuery != null) {
            QueryContext<ReferenceEntity, UUID> subQuery = this.applySubQuery(this.referenceQuery, queryContext, UUID.class, root -> root.get(ReferenceEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._referenceId)).value(subQuery.Query));
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected PlanReferenceEntity convert(Tuple tuple, Set<String> columns) {
        PlanReferenceEntity item = new PlanReferenceEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._tenantId, UUID.class));
        item.setPlanId(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._planId, UUID.class));
        item.setReferenceId(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._referenceId, UUID.class));
        item.setData(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._data, String.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._isActive, IsActive.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanReferenceEntity._updatedAt, Instant.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(PlanReference._id) || item.match(PublicPlanReference._id)) return PlanReferenceEntity._id;
        else if (item.prefix(PlanReference._plan) || item.prefix(PublicPlanReference._plan)) return PlanReferenceEntity._planId;
        else if (item.prefix(PlanReference._reference) || item.prefix(PublicPlanReference._reference)) return PlanReferenceEntity._referenceId;
        else if (item.match(PlanReference._plan) || item.match(PublicPlanReference._plan)) return PlanReferenceEntity._planId;
        else if (item.match(PlanReference._reference) || item.match(PublicPlanReference._reference)) return PlanReferenceEntity._referenceId;
        else if (item.prefix(PlanReference._data)) return PlanReferenceEntity._data;
        else if (item.match(PlanReference._isActive)) return PlanReferenceEntity._isActive;
        else if (item.match(PlanReference._createdAt)) return PlanReferenceEntity._createdAt;
        else if (item.match(PlanReference._updatedAt)) return PlanReferenceEntity._updatedAt;
        else if (item.match(PlanReference._hash)) return PlanReferenceEntity._updatedAt;
        else if (item.match(PlanReference._belongsToCurrentTenant)) return PlanReferenceEntity._tenantId;
        else return null;
    }

}
