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
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.data.PlanDescriptionTemplateEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.PublicPlanDescriptionTemplate;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanDescriptionTemplateQuery extends QueryBase<PlanDescriptionTemplateEntity> {

    private Collection<UUID> ids;

    private Collection<UUID> planIds;

    private Collection<UUID> descriptionTemplateGroupIds;

    private Collection<UUID> sectionIds;

    private Collection<IsActive> isActives;

    private Collection<UUID> excludedIds;

    private Instant after;

    private DescriptionTemplateQuery descriptionTemplateQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanDescriptionTemplateQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanDescriptionTemplateQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanDescriptionTemplateQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanDescriptionTemplateQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PlanDescriptionTemplateQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PlanDescriptionTemplateQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PlanDescriptionTemplateQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanDescriptionTemplateQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanDescriptionTemplateQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanDescriptionTemplateQuery sectionIds(UUID value) {
        this.sectionIds = List.of(value);
        return this;
    }

    public PlanDescriptionTemplateQuery sectionIds(UUID... value) {
        this.sectionIds = Arrays.asList(value);
        return this;
    }

    public PlanDescriptionTemplateQuery sectionIds(Collection<UUID> values) {
        this.sectionIds = values;
        return this;
    }

    public PlanDescriptionTemplateQuery descriptionTemplateGroupIds(UUID value) {
        this.descriptionTemplateGroupIds = List.of(value);
        return this;
    }

    public PlanDescriptionTemplateQuery descriptionTemplateGroupIds(UUID... value) {
        this.descriptionTemplateGroupIds = Arrays.asList(value);
        return this;
    }

    public PlanDescriptionTemplateQuery descriptionTemplateGroupIds(Collection<UUID> values) {
        this.descriptionTemplateGroupIds = values;
        return this;
    }

    public PlanDescriptionTemplateQuery planIds(UUID value) {
        this.planIds = List.of(value);
        return this;
    }

    public PlanDescriptionTemplateQuery planIds(UUID... value) {
        this.planIds = Arrays.asList(value);
        return this;
    }

    public PlanDescriptionTemplateQuery planIds(Collection<UUID> values) {
        this.planIds = values;
        return this;
    }

    public PlanDescriptionTemplateQuery after(Instant value) {
        this.after = value;
        return this;
    }

    public PlanDescriptionTemplateQuery descriptionTemplateSubQuery(DescriptionTemplateQuery value) {
        this.descriptionTemplateQuery = value;
        return this;
    }

    public PlanDescriptionTemplateQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanDescriptionTemplateQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanDescriptionTemplateQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public PlanDescriptionTemplateQuery(
		    UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected Class<PlanDescriptionTemplateEntity> entityClass() {
        return PlanDescriptionTemplateEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.excludedIds) || this.isEmpty(this.isActives) || this.isEmpty(this.planIds) || this.isEmpty(this.descriptionTemplateGroupIds);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowsePlanDescriptionTemplate))
            return null;
        UUID userId = null;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated))
            userId = this.userScope.getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._planId)).value(this.queryUtilsService.buildPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic)));
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
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.planIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._planId));
            for (UUID item : this.planIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.descriptionTemplateGroupIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._descriptionTemplateGroupId));
            for (UUID item : this.descriptionTemplateGroupIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.sectionIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._sectionId));
            for (UUID item : this.sectionIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.after != null) {
            Predicate afterClause = queryContext.CriteriaBuilder.greaterThanOrEqualTo(queryContext.Root.get(PlanDescriptionTemplateEntity._createdAt), this.after);
            predicates.add(afterClause);
        }
        if (this.descriptionTemplateQuery != null) {
            QueryContext<DescriptionTemplateEntity, UUID> subQuery = this.applySubQuery(this.descriptionTemplateQuery, queryContext, UUID.class, descriptionTemplateEntityRoot -> descriptionTemplateEntityRoot.get(DescriptionTemplateEntity._groupId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanDescriptionTemplateEntity._descriptionTemplateGroupId)).value(subQuery.Query));
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected PlanDescriptionTemplateEntity convert(Tuple tuple, Set<String> columns) {
        PlanDescriptionTemplateEntity item = new PlanDescriptionTemplateEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._tenantId, UUID.class));
        item.setPlanId(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._planId, UUID.class));
        item.setDescriptionTemplateGroupId(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._descriptionTemplateGroupId, UUID.class));
        item.setSectionId(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._sectionId, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanDescriptionTemplateEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(PlanDescriptionTemplate._id) || item.match(PublicPlanDescriptionTemplate._id))
            return PlanDescriptionTemplateEntity._id;
        else if (item.prefix(PlanDescriptionTemplate._plan) || item.prefix(PublicPlanDescriptionTemplate._plan))
            return PlanDescriptionTemplateEntity._planId;
        else if (item.match(PlanDescriptionTemplate._plan) || item.prefix(PublicPlanDescriptionTemplate._plan))
            return PlanDescriptionTemplateEntity._planId;
        else if (item.prefix(PlanDescriptionTemplate._currentDescriptionTemplate))
            return PlanDescriptionTemplateEntity._descriptionTemplateGroupId;
        else if (item.match(PlanDescriptionTemplate._currentDescriptionTemplate))
            return PlanDescriptionTemplateEntity._descriptionTemplateGroupId;
        else if (item.prefix(PlanDescriptionTemplate._descriptionTemplates))
            return PlanDescriptionTemplateEntity._descriptionTemplateGroupId;
        else if (item.match(PlanDescriptionTemplate._descriptionTemplates))
            return PlanDescriptionTemplateEntity._descriptionTemplateGroupId;
        else if (item.match(PlanDescriptionTemplate._sectionId))
            return PlanDescriptionTemplateEntity._sectionId;
        else if (item.match(PlanDescriptionTemplate._descriptionTemplateGroupId))
            return PlanDescriptionTemplateEntity._descriptionTemplateGroupId;
        else if (item.match(PlanDescriptionTemplate._hash))
            return PlanDescriptionTemplateEntity._updatedAt;
        else if (item.match(PlanDescriptionTemplate._createdAt))
            return PlanDescriptionTemplateEntity._createdAt;
        else if (item.match(PlanDescriptionTemplate._updatedAt))
            return PlanDescriptionTemplateEntity._updatedAt;
        else if (item.match(PlanDescriptionTemplate._isActive))
            return PlanDescriptionTemplateEntity._isActive;
        else if (item.match(PlanDescriptionTemplate._belongsToCurrentTenant))
            return PlanDescriptionTemplateEntity._tenantId;
        else
            return null;
    }

}
