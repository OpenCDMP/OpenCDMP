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
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.*;
import org.opencdmp.model.PublicDescription;
import org.opencdmp.model.description.Description;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionQuery extends QueryBase<DescriptionEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<UUID> createdByIds;

    private TenantQuery tenantQuery;

    private PlanDescriptionTemplateQuery planDescriptionTemplateQuery;

    private PlanQuery planQuery;

    private DescriptionTemplateQuery descriptionTemplateQuery;

    private DescriptionReferenceQuery descriptionReferenceQuery;

    private DescriptionTagQuery descriptionTagQuery;

    private Instant createdAfter;

    private Instant createdBefore;

    private Instant finalizedAfter;

    private Instant finalizedBefore;

    private Collection<UUID> excludedIds;

    private Collection<IsActive> isActives;

    private Collection<UUID> statusIds;

    private Collection<UUID> planIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;

    private Collection<UUID> planDescriptionTemplateIds;

    private DescriptionStatusQuery descriptionStatusQuery;

    private final TenantEntityManager tenantEntityManager;
    public DescriptionQuery(UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public DescriptionQuery like(String value) {
        this.like = value;
        return this;
    }

    public DescriptionQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DescriptionQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DescriptionQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DescriptionQuery createdByIds(UUID value) {
        this.createdByIds = List.of(value);
        return this;
    }

    public DescriptionQuery createdByIds(UUID... value) {
        this.createdByIds = Arrays.asList(value);
        return this;
    }

    public DescriptionQuery createdByIds(Collection<UUID> values) {
        this.createdByIds = values;
        return this;
    }

    public DescriptionQuery tenantSubQuery(TenantQuery subQuery) {
        this.tenantQuery = subQuery;
        return this;
    }

    public DescriptionQuery planDescriptionTemplateSubQuery(PlanDescriptionTemplateQuery subQuery) {
        this.planDescriptionTemplateQuery = subQuery;
        return this;
    }

    public DescriptionQuery createdAfter(Instant value) {
        this.createdAfter = value;
        return this;
    }

    public DescriptionQuery createdBefore(Instant value) {
        this.createdBefore = value;
        return this;
    }

    public DescriptionQuery planDescriptionTemplateIds(UUID value) {
        this.planDescriptionTemplateIds = List.of(value);
        return this;
    }

    public DescriptionQuery planDescriptionTemplateIds(UUID... value) {
        this.planDescriptionTemplateIds = Arrays.asList(value);
        return this;
    }

    public DescriptionQuery planDescriptionTemplateIds(Collection<UUID> values) {
        this.planDescriptionTemplateIds = values;
        return this;
    }

    public DescriptionQuery finalizedAfter(Instant value) {
        this.finalizedAfter = value;
        return this;
    }

    public DescriptionQuery finalizedBefore(Instant value) {
        this.finalizedBefore = value;
        return this;
    }

    public DescriptionQuery planSubQuery(PlanQuery subQuery) {
        this.planQuery = subQuery;
        return this;
    }

    public DescriptionQuery descriptionTemplateSubQuery(DescriptionTemplateQuery subQuery) {
        this.descriptionTemplateQuery = subQuery;
        return this;
    }

    public DescriptionQuery descriptionReferenceSubQuery(DescriptionReferenceQuery subQuery) {
        this.descriptionReferenceQuery = subQuery;
        return this;
    }

    public DescriptionQuery descriptionTagSubQuery(DescriptionTagQuery subQuery) {
        this.descriptionTagQuery = subQuery;
        return this;
    }

    public DescriptionQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public DescriptionQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public DescriptionQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public DescriptionQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DescriptionQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DescriptionQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DescriptionQuery statusIds(UUID value) {
        this.statusIds = List.of(value);
        return this;
    }

    public DescriptionQuery statusIds(UUID... value) {
        this.statusIds = Arrays.asList(value);
        return this;
    }

    public DescriptionQuery statusIds(Collection<UUID> values) {
        this.statusIds = values;
        return this;
    }

    public DescriptionQuery planIds(Collection<UUID> values) {
        this.planIds = values;
        return this;
    }

    public DescriptionQuery planIds(UUID value) {
        this.planIds = List.of(value);
        return this;
    }

    public DescriptionQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DescriptionQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    public DescriptionQuery descriptionStatusSubQuery(DescriptionStatusQuery subQuery) {
        this.descriptionStatusQuery = subQuery;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return
                this.isEmpty(this.ids) ||
                        this.isEmpty(this.isActives) || this.isEmpty(this.createdByIds) ||
                        this.isEmpty(this.excludedIds) || this.isFalseQuery(this.planQuery) ||
                        this.isEmpty(this.statusIds) || this.isFalseQuery(this.planDescriptionTemplateQuery);
    }

    @Override
    protected Class<DescriptionEntity> entityClass() {
        return DescriptionEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseDescription))
            return null;
        UUID userId;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated))
            userId = this.userScope.getUserIdSafe();
        else
            userId = null;

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._id)).value(this.queryUtilsService.buildDescriptionAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic)));
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
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionEntity._description), this.like),
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionEntity._label), this.like)
            ));
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantQuery != null) {
            QueryContext<TenantEntity, UUID> subQuery = this.applySubQuery(this.tenantQuery, queryContext, UUID.class, tenantEntityRoot -> tenantEntityRoot.get(TenantEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._tenantId)).value(subQuery.Query));
        }
        if (this.createdByIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._createdById));
            for (UUID item : this.createdByIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.createdAfter != null) {
            predicates.add(queryContext.CriteriaBuilder.greaterThan(queryContext.Root.get(DescriptionEntity._createdAt), this.createdAfter));
        }
        if (this.createdBefore != null) {
            predicates.add(queryContext.CriteriaBuilder.lessThan(queryContext.Root.get(DescriptionEntity._createdAt), this.createdBefore));
        }
        if (this.finalizedAfter != null) {
            predicates.add(queryContext.CriteriaBuilder.greaterThan(queryContext.Root.get(DescriptionEntity._finalizedAt), this.finalizedAfter));
        }
        if (this.finalizedBefore != null) {
            predicates.add(queryContext.CriteriaBuilder.lessThan(queryContext.Root.get(DescriptionEntity._finalizedAt), this.finalizedAfter));
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.planIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._planId));
            for (UUID item : this.planIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.statusIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._statusId));
            for (UUID item : this.statusIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.planDescriptionTemplateQuery != null) {
            QueryContext<PlanDescriptionTemplateEntity, UUID> subQuery = this.applySubQuery(this.planDescriptionTemplateQuery, queryContext, UUID.class, planDescriptionTemplateEntityRoot -> planDescriptionTemplateEntityRoot.get(PlanDescriptionTemplateEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._planDescriptionTemplateId)).value(subQuery.Query));
        }
        if (this.planQuery != null) {
            QueryContext<PlanEntity, UUID> subQuery = this.applySubQuery(this.planQuery, queryContext, UUID.class, planEntityRoot -> planEntityRoot.get(PlanEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._planId)).value(subQuery.Query));
        }
        if (this.descriptionTemplateQuery != null) {
            QueryContext<DescriptionTemplateEntity, UUID> subQuery = this.applySubQuery(this.descriptionTemplateQuery, queryContext, UUID.class, descriptionTemplateEntityRoot -> descriptionTemplateEntityRoot.get(DescriptionTemplateEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._descriptionTemplateId)).value(subQuery.Query));
        }
        if (this.descriptionReferenceQuery != null) {
            QueryContext<DescriptionReferenceEntity, UUID> subQuery = this.applySubQuery(this.descriptionReferenceQuery, queryContext, UUID.class, descriptionReferenceEntityRoot -> descriptionReferenceEntityRoot.get(DescriptionReferenceEntity._descriptionId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._id)).value(subQuery.Query));
        }
        if (this.descriptionTagQuery != null) {
            QueryContext<DescriptionTagEntity, UUID> subQuery = this.applySubQuery(this.descriptionTagQuery, queryContext, UUID.class, descriptionTagEntityRoot -> descriptionTagEntityRoot.get(DescriptionTagEntity._descriptionId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._id)).value(subQuery.Query));
        }
        if (this.planDescriptionTemplateIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._planDescriptionTemplateId));
            for (UUID item : this.planDescriptionTemplateIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.descriptionStatusQuery != null) {
            QueryContext<DescriptionStatusEntity, UUID> subQuery = this.applySubQuery(this.descriptionStatusQuery, queryContext, UUID.class, descriptionStatusEntityRoot -> descriptionStatusEntityRoot.get(DescriptionStatusEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._statusId)).value(subQuery.Query));
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(Description._id) || item.match(PublicDescription._id))
            return DescriptionEntity._id;
        else if (item.match(Description._label) || item.match(PublicDescription._label))
            return DescriptionEntity._label;
        else if (item.prefix(Description._properties))
            return DescriptionEntity._properties;
        else if (item.match(Description._status) || item.match(PublicDescription._status))
            return DescriptionEntity._statusId;
        else if (item.prefix(Description._status) || item.prefix(PublicDescription._status))
            return DescriptionEntity._statusId;
        else if (item.match(Description._description) || item.match(PublicDescription._description))
            return DescriptionEntity._description;
        else if (item.match(Description._createdBy))
            return DescriptionEntity._createdById;
        else if (item.prefix(Description._createdBy))
            return DescriptionEntity._createdById;
        else if (item.match(Description._createdAt) || item.match(PublicDescription._createdAt))
            return DescriptionEntity._createdAt;
        else if (item.match(Description._updatedAt) || item.match(PublicDescription._updatedAt))
            return DescriptionEntity._updatedAt;
        else if (item.match(Description._isActive))
            return DescriptionEntity._isActive;
        else if (item.match(Description._hash))
            return DescriptionEntity._updatedAt;
        else if (item.match(Description._finalizedAt) || item.match(PublicDescription._finalizedAt))
            return DescriptionEntity._finalizedAt;
        else if (item.prefix(Description._planDescriptionTemplate) || item.prefix(PublicDescription._planDescriptionTemplate))
            return DescriptionEntity._planDescriptionTemplateId;
        else if (item.match(Description._planDescriptionTemplate) || item.match(PublicDescription._planDescriptionTemplate))
            return DescriptionEntity._planDescriptionTemplateId;
        else if (item.prefix(Description._descriptionTemplate) || item.prefix(PublicDescription._descriptionTemplate))
            return DescriptionEntity._descriptionTemplateId;
        else if (item.match(Description._descriptionTemplate) || item.match(PublicDescription._descriptionTemplate))
            return DescriptionEntity._descriptionTemplateId;
        else if (item.prefix(Description._plan))
            return DescriptionEntity._planId;
        else if (item.match(Description._plan))
            return DescriptionEntity._planId;
        else if (item.match(Description._belongsToCurrentTenant))
            return DescriptionEntity._tenantId;
        else if (item.match(DescriptionEntity._tenantId))
            return DescriptionEntity._tenantId;
        else
            return null;
    }

    @Override
    protected DescriptionEntity convert(Tuple tuple, Set<String> columns) {
        DescriptionEntity item = new DescriptionEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DescriptionEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DescriptionEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, DescriptionEntity._label, String.class));
        item.setProperties(QueryBase.convertSafe(tuple, columns, DescriptionEntity._properties, String.class));
        item.setStatusId(QueryBase.convertSafe(tuple, columns, DescriptionEntity._statusId, UUID.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, DescriptionEntity._description, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DescriptionEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DescriptionEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DescriptionEntity._isActive, IsActive.class));
        item.setFinalizedAt(QueryBase.convertSafe(tuple, columns, DescriptionEntity._finalizedAt, Instant.class));
        item.setPlanDescriptionTemplateId(QueryBase.convertSafe(tuple, columns, DescriptionEntity._planDescriptionTemplateId, UUID.class));
        item.setDescriptionTemplateId(QueryBase.convertSafe(tuple, columns, DescriptionEntity._descriptionTemplateId, UUID.class));
        item.setPlanId(QueryBase.convertSafe(tuple, columns, DescriptionEntity._planId, UUID.class));
        return item;
    }

}
