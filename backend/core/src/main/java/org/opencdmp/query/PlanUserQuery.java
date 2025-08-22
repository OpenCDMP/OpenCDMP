package org.opencdmp.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.PublicPlanUser;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanUserQuery extends QueryBase<PlanUserEntity> {

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UUID> planIds;

    private Collection<UUID> descriptionIds;

    private Collection<UUID> userIds;

    private Collection<PlanUserRole> userRoles;

    private Collection<UUID> sectionIds;
    private Boolean sectionIsEmpty;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    
    public PlanUserQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanUserQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanUserQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanUserQuery isActives(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanUserQuery isActives(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanUserQuery isActives(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanUserQuery planIds(UUID value) {
        this.planIds = List.of(value);
        return this;
    }

    public PlanUserQuery planIds(UUID... value) {
        this.planIds = Arrays.asList(value);
        return this;
    }

    public PlanUserQuery planIds(Collection<UUID> values) {
        this.planIds = values;
        return this;
    }

    public PlanUserQuery descriptionIds(UUID value) {
        this.descriptionIds = List.of(value);
        return this;
    }

    public PlanUserQuery descriptionIds(UUID... value) {
        this.descriptionIds = Arrays.asList(value);
        return this;
    }

    public PlanUserQuery descriptionIds(Collection<UUID> values) {
        this.descriptionIds = values;
        return this;
    }

    public PlanUserQuery userRoles(PlanUserRole value) {
        this.userRoles = List.of(value);
        return this;
    }

    public PlanUserQuery userRoles(PlanUserRole... value) {
        this.userRoles = Arrays.asList(value);
        return this;
    }

    public PlanUserQuery userRoles(Collection<PlanUserRole> values) {
        this.userRoles = values;
        return this;
    }

    public PlanUserQuery userIds(UUID value) {
        this.userIds = List.of(value);
        return this;
    }

    public PlanUserQuery userIds(UUID... value) {
        this.userIds = Arrays.asList(value);
        return this;
    }

    public PlanUserQuery userIds(Collection<UUID> values) {
        this.userIds = values;
        return this;
    }

    public PlanUserQuery sectionIds(UUID value) {
        this.sectionIds = List.of(value);
        return this;
    }

    public PlanUserQuery sectionIds(UUID... value) {
        this.sectionIds = Arrays.asList(value);
        return this;
    }

    public PlanUserQuery sectionIds(Collection<UUID> values) {
        this.sectionIds = values;
        return this;
    }
    
    public PlanUserQuery sectionIsEmpty(Boolean sectionIsEmpty) {
        this.sectionIsEmpty = sectionIsEmpty;
        return this;
    }

    public PlanUserQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanUserQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanUserQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    
    public PlanUserQuery(
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
    protected Class<PlanUserEntity> entityClass() {
        return PlanUserEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.planIds) || this.isEmpty(this.descriptionIds) || this.isEmpty(this.userIds);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowsePlan)) return null;
        UUID userId = null;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated)) userId = this.userScope.getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic ) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    usePublic ? queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._planId)).value(this.queryUtilsService.buildPublicPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, usePublic)) : queryContext.CriteriaBuilder.or(),  //Creates a false query
                    userId != null ?  queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._planId)).value(this.queryUtilsService.buildPlanUserAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId)) : queryContext.CriteriaBuilder.or()  //Creates a false query
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
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.planIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._planId));
            for (UUID item : this.planIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.sectionIsEmpty != null){
            if(this.sectionIsEmpty) predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(PlanUserEntity._sectionId)));
            else predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(PlanUserEntity._sectionId)));
        }
        if (this.descriptionIds != null) {
            Subquery<UUID> descriptionSubquery = this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(
                    new BuildSubQueryInput.Builder<>(DescriptionEntity.class, UUID.class, queryContext)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(DescriptionEntity._planId))
                            .filterFunc((subQueryRoot, cb) ->  {
                                CriteriaBuilder.In<UUID> inClause = cb.in(subQueryRoot.get(PlanUserEntity._id));
                                for (UUID item : this.descriptionIds)
                                    inClause.value(item);
                                return inClause;
                            })
            ));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._planId)).value(descriptionSubquery));
        }
        if (this.userIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._userId));
            for (UUID item : this.userIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.sectionIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._sectionId));
            for (UUID item : this.sectionIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.userRoles != null) {
            CriteriaBuilder.In<PlanUserRole> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanUserEntity._role));
            for (PlanUserRole item : this.userRoles)
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
    protected PlanUserEntity convert(Tuple tuple, Set<String> columns) {
        PlanUserEntity item = new PlanUserEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanUserEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanUserEntity._tenantId, UUID.class));
        item.setPlanId(QueryBase.convertSafe(tuple, columns, PlanUserEntity._planId, UUID.class));
        item.setUserId(QueryBase.convertSafe(tuple, columns, PlanUserEntity._userId, UUID.class));
        item.setSectionId(QueryBase.convertSafe(tuple, columns, PlanUserEntity._sectionId, UUID.class));
        item.setOrdinal(QueryBase.convertSafe(tuple, columns, PlanUserEntity._ordinal, Integer.class));
        item.setRole(QueryBase.convertSafe(tuple, columns, PlanUserEntity._role, PlanUserRole.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanUserEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanUserEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanUserEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(PlanUser._id) || item.match(PublicPlanUser._id)) return PlanUserEntity._id;
        else if (item.prefix(PlanUser._plan) || item.prefix(PublicPlanUser._plan)) return PlanUserEntity._planId;
        else if (item.prefix(PlanUser._user) || item.prefix(PublicPlanUser._user)) return PlanUserEntity._userId;
        else if (item.match(PlanUser._role) || item.match(PublicPlanUser._role)) return PlanUserEntity._role;
        else if (item.match(PlanUser._sectionId)) return PlanUserEntity._sectionId;
        else if (item.match(PlanUser._ordinal)) return PlanUserEntity._ordinal;
        else if (item.match(PlanUser._createdAt)) return PlanUserEntity._createdAt;
        else if (item.match(PlanUser._updatedAt)) return PlanUserEntity._updatedAt;
        else if (item.match(PlanUser._isActive)) return PlanUserEntity._isActive;
        else if (item.match(PlanUser._hash)) return PlanUserEntity._updatedAt;
        else if (item.match(PlanUser._plan)) return PlanUserEntity._planId;
        else if (item.match(PlanUser._user)) return PlanUserEntity._userId;
        else if (item.match(PlanUser._belongsToCurrentTenant)) return PlanUserEntity._tenantId;
        else return null;
    }
    
}
