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
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.*;
import org.opencdmp.model.PublicPlan;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanQuery extends QueryBase<PlanEntity> {

    private String like;

    private Collection<UUID> ids;

    private TenantQuery tenantQuery;

    private Collection<UUID> creatorIds;

    private Collection<UUID> excludedIds;

    private Collection<IsActive> isActives;

    private Collection<PlanStatus> statuses;

    private Collection<PlanVersionStatus> versionStatuses;

    private Collection<PlanAccessType> accessTypes;

    private Collection<Integer> versions;

    private Collection<UUID> groupIds;

    private Instant after;

    private PlanUserQuery planUserQuery;

    private PlanDescriptionTemplateQuery planDescriptionTemplateQuery;

    private PlanBlueprintQuery planBlueprintQuery;

    private PlanReferenceQuery planReferenceQuery;

    private EntityDoiQuery entityDoiQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public PlanQuery(UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public PlanQuery like(String value) {
        this.like = value;
        return this;
    }

    public PlanQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanQuery tenantSubQuery(TenantQuery tenantQuery) {
        this.tenantQuery = tenantQuery;
        return this;
    }

    public PlanQuery creatorIds(UUID value) {
        this.creatorIds = List.of(value);
        return this;
    }

    public PlanQuery creatorIds(UUID... value) {
        this.creatorIds = Arrays.asList(value);
        return this;
    }

    public PlanQuery creatorIds(Collection<UUID> values) {
        this.creatorIds = values;
        return this;
    }

    public PlanQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PlanQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PlanQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PlanQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanQuery versionStatuses(PlanVersionStatus value) {
        this.versionStatuses = List.of(value);
        return this;
    }

    public PlanQuery versionStatuses(PlanVersionStatus... value) {
        this.versionStatuses = Arrays.asList(value);
        return this;
    }

    public PlanQuery versionStatuses(Collection<PlanVersionStatus> values) {
        this.versionStatuses = values;
        return this;
    }

    public PlanQuery accessTypes(PlanAccessType value) {
        this.accessTypes = List.of(value);
        return this;
    }

    public PlanQuery accessTypes(PlanAccessType... value) {
        this.accessTypes = Arrays.asList(value);
        return this;
    }

    public PlanQuery accessTypes(Collection<PlanAccessType> values) {
        this.accessTypes = values;
        return this;
    }

    public PlanQuery statuses(PlanStatus value) {
        this.statuses = List.of(value);
        return this;
    }

    public PlanQuery statuses(PlanStatus... value) {
        this.statuses = Arrays.asList(value);
        return this;
    }

    public PlanQuery statuses(Collection<PlanStatus> values) {
        this.statuses = values;
        return this;
    }

    public PlanQuery versions(Integer value) {
        this.versions = List.of(value);
        return this;
    }

    public PlanQuery versions(Integer... value) {
        this.versions = Arrays.asList(value);
        return this;
    }

    public PlanQuery versions(Collection<Integer> values) {
        this.versions = values;
        return this;
    }

    public PlanQuery groupIds(UUID value) {
        this.groupIds = List.of(value);
        return this;
    }

    public PlanQuery groupIds(UUID... value) {
        this.groupIds = Arrays.asList(value);
        return this;
    }

    public PlanQuery groupIds(Collection<UUID> values) {
        this.groupIds = values;
        return this;
    }

    public PlanQuery after(Instant value) {
        this.after = value;
        return this;
    }

    public PlanQuery planDescriptionTemplateSubQuery(PlanDescriptionTemplateQuery subQuery) {
        this.planDescriptionTemplateQuery = subQuery;
        return this;
    }

    public PlanQuery planBlueprintSubQuery(PlanBlueprintQuery subQuery) {
        this.planBlueprintQuery = subQuery;
        return this;
    }

    public PlanQuery planUserSubQuery(PlanUserQuery subQuery) {
        this.planUserQuery = subQuery;
        return this;
    }

    public PlanQuery planReferenceSubQuery(PlanReferenceQuery subQuery) {
        this.planReferenceQuery = subQuery;
        return this;
    }

    public PlanQuery entityDoiSubQuery(EntityDoiQuery subQuery) {
        this.entityDoiQuery = subQuery;
        this.entityDoiQuery.types(EntityType.Plan);
        return this;
    }

    public PlanQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.creatorIds) || this.isEmpty(this.isActives) || this.isEmpty(this.versionStatuses) || this.isEmpty(this.excludedIds) || this.isEmpty(this.accessTypes) || this.isEmpty(this.statuses) || this.isFalseQuery(this.planDescriptionTemplateQuery) || this.isFalseQuery(this.planUserQuery);
    }

    @Override
    protected Class<PlanEntity> entityClass() {
        return PlanEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowsePlan))
            return null;
        UUID userId = null;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated))
            userId = this.userScope.getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    usePublic ? queryContext.CriteriaBuilder.and(
                            queryContext.CriteriaBuilder.equal(queryContext.Root.get(PlanEntity._status), PlanStatus.Finalized),
                            queryContext.CriteriaBuilder.equal(queryContext.Root.get(PlanEntity._accessType), PlanAccessType.Public)
                    )
                            : queryContext.CriteriaBuilder.or(),  //Creates a false query
                    userId != null ? queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._id)).value(this.queryUtilsService.buildPlanUserAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId)) : queryContext.CriteriaBuilder.or()  //Creates a false query
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
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanEntity._label), this.like));
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantQuery != null) {
            QueryContext<TenantEntity, UUID> subQuery = this.applySubQuery(this.tenantQuery, queryContext, UUID.class, tenantEntityRoot -> tenantEntityRoot.get(TenantEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._tenantId)).value(subQuery.Query));
        }
        if (this.creatorIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._creatorId));
            for (UUID item : this.creatorIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.statuses != null) {
            CriteriaBuilder.In<PlanStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._status));
            for (PlanStatus item : this.statuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.versionStatuses != null) {
            CriteriaBuilder.In<PlanVersionStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._versionStatus));
            for (PlanVersionStatus item : this.versionStatuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.accessTypes != null) {
            CriteriaBuilder.In<PlanAccessType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._accessType));
            for (PlanAccessType item : this.accessTypes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.versions != null) {
            CriteriaBuilder.In<Integer> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._version));
            for (Integer item : this.versions)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.groupIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._groupId));
            for (UUID item : this.groupIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.after != null) {
            Predicate afterClause = queryContext.CriteriaBuilder.greaterThanOrEqualTo(queryContext.Root.get(PlanEntity._createdAt), this.after);
            predicates.add(afterClause);
        }

        if (this.planUserQuery != null) {
            QueryContext<PlanUserEntity, UUID> subQuery = this.applySubQuery(this.planUserQuery, queryContext, UUID.class, root -> root.get(PlanUserEntity._planId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._id)).value(subQuery.Query));
        }

        if (this.planDescriptionTemplateQuery != null) {
            QueryContext<PlanDescriptionTemplateEntity, UUID> subQuery = this.applySubQuery(this.planDescriptionTemplateQuery, queryContext, UUID.class, planDescriptionTemplateEntityRoot -> planDescriptionTemplateEntityRoot.get(PlanDescriptionTemplateEntity._planId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._id)).value(subQuery.Query));
        }

        if (this.planBlueprintQuery != null) {
            QueryContext<PlanBlueprintEntity, UUID> subQuery = this.applySubQuery(this.planBlueprintQuery, queryContext, UUID.class, planBlueprintEntityRoot -> planBlueprintEntityRoot.get(PlanBlueprintEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._blueprintId)).value(subQuery.Query));
        }

        if (this.planReferenceQuery != null) {
            QueryContext<PlanReferenceEntity, UUID> subQuery = this.applySubQuery(this.planReferenceQuery, queryContext, UUID.class, planReferenceEntityRoot -> planReferenceEntityRoot.get(PlanReferenceEntity._planId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._id)).value(subQuery.Query));
        }

        if (this.entityDoiQuery != null) {
            QueryContext<EntityDoiEntity, UUID> subQuery = this.applySubQuery(this.entityDoiQuery, queryContext, UUID.class, entityDoiEntityRoot -> entityDoiEntityRoot.get(EntityDoiEntity._entityId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanEntity._id)).value(subQuery.Query));
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
        if (item.match(Plan._id) || item.match(PublicPlan._id))
            return PlanEntity._id;
        else if (item.match(Plan._label) || item.match(PublicPlan._label))
            return PlanEntity._label;
        else if (item.match(Plan._version) || item.match(PublicPlan._version))
            return PlanEntity._version;
        else if (item.match(Plan._status))
            return PlanEntity._status;
        else if (item.match(Plan._properties))
            return PlanEntity._properties;
        else if (item.prefix(Plan._properties))
            return PlanEntity._properties;
        else if (item.match(Plan._groupId))
            return PlanEntity._groupId;
        else if (item.match(Plan._description) || item.match(PublicPlan._description))
            return PlanEntity._description;
        else if (item.match(Plan._createdAt))
            return PlanEntity._createdAt;
        else if (item.match(Plan._updatedAt))
            return PlanEntity._updatedAt;
        else if (item.match(Plan._hash))
            return PlanEntity._updatedAt;
        else if (item.match(Plan._isActive))
            return PlanEntity._isActive;
        else if (item.match(Plan._finalizedAt) || item.match(PublicPlan._finalizedAt))
            return PlanEntity._finalizedAt;
        else if (item.match(Plan._accessType))
            return PlanEntity._accessType;
        else if (item.match(Plan._creator))
            return PlanEntity._creatorId;
        else if (item.prefix(Plan._creator))
            return PlanEntity._blueprintId;
        else if (item.match(Plan._blueprint))
            return PlanEntity._blueprintId;
        else if (item.prefix(Plan._blueprint))
            return PlanEntity._blueprintId;
        else if (item.match(Plan._language))
            return PlanEntity._language;
        else if (item.match(Plan._publicAfter))
            return PlanEntity._publicAfter;
        else if (item.match(Plan._versionStatus))
            return PlanEntity._versionStatus;
        else if (item.match(PlanEntity._tenantId))
            return PlanEntity._tenantId;
        else if (item.match(Plan._belongsToCurrentTenant))
            return PlanEntity._tenantId;
        else
            return null;
    }

    @Override
    protected PlanEntity convert(Tuple tuple, Set<String> columns) {
        PlanEntity item = new PlanEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, PlanEntity._label, String.class));
        item.setVersion(QueryBase.convertSafe(tuple, columns, PlanEntity._version, Short.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, PlanEntity._status, PlanStatus.class));
        item.setVersionStatus(QueryBase.convertSafe(tuple, columns, PlanEntity._versionStatus, PlanVersionStatus.class));
        item.setProperties(QueryBase.convertSafe(tuple, columns, PlanEntity._properties, String.class));
        item.setGroupId(QueryBase.convertSafe(tuple, columns, PlanEntity._groupId, UUID.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, PlanEntity._description, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanEntity._isActive, IsActive.class));
        item.setFinalizedAt(QueryBase.convertSafe(tuple, columns, PlanEntity._finalizedAt, Instant.class));
        item.setAccessType(QueryBase.convertSafe(tuple, columns, PlanEntity._accessType, PlanAccessType.class));
        item.setCreatorId(QueryBase.convertSafe(tuple, columns, PlanEntity._creatorId, UUID.class));
        item.setBlueprintId(QueryBase.convertSafe(tuple, columns, PlanEntity._blueprintId, UUID.class));
        item.setLanguage(QueryBase.convertSafe(tuple, columns, PlanEntity._language, String.class));
        item.setPublicAfter(QueryBase.convertSafe(tuple, columns, PlanEntity._publicAfter, Instant.class));
        return item;
    }

}
