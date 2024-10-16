package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.LockEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.Lock;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LockQuery extends QueryBase<LockEntity> {

    private String like;
    private Collection<UUID> ids;

    private Collection<UUID> targetIds;
    private Collection<UUID> lockedByIds;

    private Collection<LockTargetType> targetTypes;

    private Collection<UUID> excludedIds;

    private Collection<UUID> userIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final ConventionService conventionService;

    public LockQuery like(String value) {
        this.like = value;
        return this;
    }

    public LockQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public LockQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public LockQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }
    
    public LockQuery lockedByIds(UUID value) {
        this.lockedByIds = List.of(value);
        return this;
    }

    public LockQuery lockedByIds(UUID... value) {
        this.lockedByIds = Arrays.asList(value);
        return this;
    }

    public LockQuery lockedByIds(Collection<UUID> values) {
        this.lockedByIds = values;
        return this;
    }

    public LockQuery targetIds(UUID value) {
        this.targetIds = List.of(value);
        return this;
    }

    public LockQuery targetIds(UUID... value) {
        this.targetIds = Arrays.asList(value);
        return this;
    }

    public LockQuery targetIds(Collection<UUID> values) {
        this.targetIds = values;
        return this;
    }

    public LockQuery targetTypes(LockTargetType value) {
        this.targetTypes = List.of(value);
        return this;
    }

    public LockQuery targetTypes(LockTargetType... value) {
        this.targetTypes = Arrays.asList(value);
        return this;
    }

    public LockQuery targetTypes(Collection<LockTargetType> values) {
        this.targetTypes = values;
        return this;
    }

    public LockQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public LockQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public LockQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public LockQuery userIds(UUID value) {
        this.userIds = List.of(value);
        return this;
    }

    public LockQuery userIds(UUID... value) {
        this.userIds = Arrays.asList(value);
        return this;
    }

    public LockQuery userIds(Collection<UUID> values) {
        this.userIds = values;
        return this;
    }

    public LockQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public LockQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public LockQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final TenantEntityManager tenantEntityManager;
    public LockQuery(ConventionService conventionService, TenantEntityManager tenantEntityManager) {
        this.conventionService = conventionService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<LockEntity> entityClass() {
        return LockEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.lockedByIds) || this.isEmpty(this.targetIds) || this.isEmpty(this.excludedIds) || this.isEmpty(this.targetTypes);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.like != null && !this.like.isEmpty()) {
            this.like = this.like.replaceAll("%", "");
            if (this.conventionService.isValidUUID(this.like)){
                CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LockEntity._target));
                inClause.value(UUID.fromString(this.like));
                predicates.add(inClause);
            }
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LockEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.lockedByIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LockEntity._lockedBy));
            for (UUID item : this.lockedByIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.targetIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LockEntity._target));
            for (UUID item : this.targetIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.targetTypes != null) {
            CriteriaBuilder.In<LockTargetType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LockEntity._targetType));
            for (LockTargetType item : this.targetTypes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LockEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.userIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LockEntity._lockedBy));
            for (UUID item : this.userIds)
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
    protected LockEntity convert(Tuple tuple, Set<String> columns) {
        LockEntity item = new LockEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, LockEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, LockEntity._tenantId, UUID.class));
        item.setTarget(QueryBase.convertSafe(tuple, columns, LockEntity._target, UUID.class));
        item.setTargetType(QueryBase.convertSafe(tuple, columns, LockEntity._targetType, LockTargetType.class));
        item.setLockedBy(QueryBase.convertSafe(tuple, columns, LockEntity._lockedBy, UUID.class));
        item.setLockedAt(QueryBase.convertSafe(tuple, columns, LockEntity._lockedAt, Instant.class));
        item.setTouchedAt(QueryBase.convertSafe(tuple, columns, LockEntity._touchedAt, Instant.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, LockEntity._tenantId, UUID.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(Lock._id)) return LockEntity._id;
        else if (item.match(Lock._target)) return LockEntity._target;
        else if (item.match(Lock._targetType)) return LockEntity._targetType;
        else if (item.prefix(Lock._lockedBy)) return LockEntity._lockedBy;
        else if (item.match(Lock._lockedAt)) return LockEntity._lockedAt;
        else if (item.match(Lock._touchedAt)) return LockEntity._touchedAt;
        else if (item.match(Lock._hash)) return LockEntity._lockedAt;
        else if (item.match(Lock._belongsToCurrentTenant)) return LockEntity._tenantId;
        else return null;
    }

}
