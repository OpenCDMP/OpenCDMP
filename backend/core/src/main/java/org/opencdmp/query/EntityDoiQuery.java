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
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityDoiQuery extends QueryBase<EntityDoiEntity> {

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<EntityType> types;

    private Collection<UUID> excludedIds;

    private Collection<String> repositoryIds;

    private Collection<String> dois;

    private Collection<UUID> entityIds;

    private Instant after;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public EntityDoiQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public EntityDoiQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public EntityDoiQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public EntityDoiQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public EntityDoiQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public EntityDoiQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public EntityDoiQuery types(EntityType value) {
        this.types = List.of(value);
        return this;
    }

    public EntityDoiQuery types(EntityType... value) {
        this.types = Arrays.asList(value);
        return this;
    }

    public EntityDoiQuery types(Collection<EntityType> values) {
        this.types = values;
        return this;
    }

    public EntityDoiQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public EntityDoiQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public EntityDoiQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public EntityDoiQuery dois(Collection<String> values) {
        this.dois = values;
        return this;
    }

    public EntityDoiQuery dois(String value) {
        this.dois = List.of(value);
        return this;
    }

    public EntityDoiQuery dois(String... value) {
        this.dois = Arrays.asList(value);
        return this;
    }

    public EntityDoiQuery repositoryIds(Collection<String> values) {
        this.repositoryIds = values;
        return this;
    }

    public EntityDoiQuery repositoryIds(String value) {
        this.repositoryIds = List.of(value);
        return this;
    }

    public EntityDoiQuery repositoryIds(String... value) {
        this.repositoryIds = Arrays.asList(value);
        return this;
    }

    public EntityDoiQuery entityIds(Collection<UUID> values) {
        this.entityIds = values;
        return this;
    }

    public EntityDoiQuery entityIds(UUID value) {
        this.entityIds = List.of(value);
        return this;
    }

    public EntityDoiQuery entityIds(UUID... value) {
        this.entityIds = Arrays.asList(value);
        return this;
    }

    public EntityDoiQuery after(Instant value) {
        this.after = value;
        return this;
    }

    public EntityDoiQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public EntityDoiQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public EntityDoiQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public EntityDoiQuery(
		    UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
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
    protected Class<EntityDoiEntity> entityClass() {
        return EntityDoiEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.repositoryIds) || this.isEmpty(this.excludedIds) || this.isEmpty(this.types);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseUser))
            return null;
        UUID userId;
        if (this.authorize.contains(AuthorizationFlags.Owner))
            userId = this.userScope.getUserIdSafe();
        else
            userId = null;

        List<Predicate> predicates = new ArrayList<>();
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (userId != null || usePublic) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._entityId)).value(this.queryUtilsService.buildPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic)));
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
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.types != null) {
            CriteriaBuilder.In<EntityType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._entityType));
            for (EntityType item : this.types)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.dois != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._doi));
            for (String item : this.dois)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.repositoryIds != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._repositoryId));
            for (String item : this.repositoryIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.entityIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EntityDoiEntity._entityId));
            for (UUID item : this.entityIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.after != null) {
            Predicate afterClause = queryContext.CriteriaBuilder.greaterThanOrEqualTo(queryContext.Root.get(PlanEntity._createdAt), this.after);
            predicates.add(afterClause);
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected EntityDoiEntity convert(Tuple tuple, Set<String> columns) {
        EntityDoiEntity item = new EntityDoiEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._tenantId, UUID.class));
        item.setDoi(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._doi, String.class));
        item.setRepositoryId(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._repositoryId, String.class));
        item.setEntityId(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._entityId, UUID.class));
        item.setEntityType(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._entityType, EntityType.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, EntityDoiEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(EntityDoi._id))
            return EntityDoiEntity._id;
        else if (item.match(EntityDoi._doi))
            return EntityDoiEntity._doi;
        else if (item.match(EntityDoi._repositoryId))
            return EntityDoiEntity._repositoryId;
        else if (item.match(EntityDoi._entityId))
            return EntityDoiEntity._entityId;
        else if (item.match(EntityDoi._entityType))
            return EntityDoiEntity._entityType;
        else if (item.match(EntityDoi._createdAt))
            return EntityDoiEntity._createdAt;
        else if (item.match(EntityDoi._updatedAt))
            return EntityDoiEntity._updatedAt;
        else if (item.match(EntityDoi._hash))
            return EntityDoiEntity._updatedAt;
        else if (item.match(EntityDoi._isActive))
            return EntityDoiEntity._isActive;
        else if (item.match(EntityDoi._belongsToCurrentTenant))
            return EntityDoiEntity._tenantId;
        else
            return null;
    }

}
