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
import org.opencdmp.data.DescriptionReferenceEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionReferenceQuery extends QueryBase<DescriptionReferenceEntity> {

    private Collection<UUID> ids;

    private Collection<UUID> excludedIds;

    private Collection<IsActive> isActives;

    private Collection<UUID> descriptionIds;

    private Collection<UUID> referenceIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final UserScope userScope;
    private final AuthorizationService authService;
    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public DescriptionReferenceQuery(UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public DescriptionReferenceQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DescriptionReferenceQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DescriptionReferenceQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DescriptionReferenceQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public DescriptionReferenceQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public DescriptionReferenceQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public DescriptionReferenceQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DescriptionReferenceQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DescriptionReferenceQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DescriptionReferenceQuery descriptionIds(UUID value) {
        this.descriptionIds = List.of(value);
        return this;
    }

    public DescriptionReferenceQuery descriptionIds(UUID... value) {
        this.descriptionIds = Arrays.asList(value);
        return this;
    }

    public DescriptionReferenceQuery descriptionIds(Collection<UUID> values) {
        this.descriptionIds = values;
        return this;
    }

    public DescriptionReferenceQuery referenceIds(UUID value) {
        this.referenceIds = List.of(value);
        return this;
    }

    public DescriptionReferenceQuery referenceIds(UUID... value) {
        this.referenceIds = Arrays.asList(value);
        return this;
    }

    public DescriptionReferenceQuery referenceIds(Collection<UUID> values) {
        this.referenceIds = values;
        return this;
    }

    public DescriptionReferenceQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionReferenceQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DescriptionReferenceQuery disableTracking() {
        this.noTracking = true;
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
                        this.isEmpty(this.isActives) ||
                        this.isEmpty(this.excludedIds) ||
                        this.isEmpty(this.descriptionIds) ||
                        this.isEmpty(this.referenceIds);
    }

    @Override
    protected Class<DescriptionReferenceEntity> entityClass() {
        return DescriptionReferenceEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseDescriptionReference)) return null;
        UUID userId = null;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated)) userId = this.userScope.getUserIdSafe();
        if (this.authorize.contains(AuthorizationFlags.Owner)) userId = this.userScope.getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic ) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionReferenceEntity._descriptionId)).value(this.queryUtilsService.buildDescriptionAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic)));
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
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionReferenceEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionReferenceEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionReferenceEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.descriptionIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionReferenceEntity._descriptionId));
            for (UUID item : this.descriptionIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.referenceIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionReferenceEntity._referenceId));
            for (UUID item : this.referenceIds)
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
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(DescriptionReference._id)) return DescriptionReferenceEntity._id;
        if (item.match(DescriptionReference._description)) return DescriptionReferenceEntity._descriptionId;
        else if (item.prefix(DescriptionReference._description)) return DescriptionReferenceEntity._descriptionId;
        else if (item.match(DescriptionReference._reference)) return DescriptionReferenceEntity._referenceId;
        else if (item.prefix(DescriptionReference._reference)) return DescriptionReferenceEntity._referenceId;
        else if (item.match(DescriptionReference._createdAt)) return DescriptionReferenceEntity._createdAt;
        else if (item.match(DescriptionReference._updatedAt)) return DescriptionReferenceEntity._updatedAt;
        else if (item.prefix(DescriptionReference._data)) return DescriptionReferenceEntity._data;
        else if (item.match(DescriptionReference._hash)) return DescriptionReferenceEntity._updatedAt;
        else if (item.match(DescriptionReference._isActive)) return DescriptionReferenceEntity._isActive;
        else if (item.match(DescriptionReference._belongsToCurrentTenant)) return DescriptionReferenceEntity._tenantId;
        else return null;
    }

    @Override
    protected DescriptionReferenceEntity convert(Tuple tuple, Set<String> columns) {
        DescriptionReferenceEntity item = new DescriptionReferenceEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._tenantId, UUID.class));
        item.setDescriptionId(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._descriptionId, UUID.class));
        item.setReferenceId(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._referenceId, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._updatedAt, Instant.class));
        item.setData(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._data, String.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DescriptionReferenceEntity._isActive, IsActive.class));
        return item;
    }

}
