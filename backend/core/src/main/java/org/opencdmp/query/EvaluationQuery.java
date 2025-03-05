package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.EvaluationStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.EvaluationEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.evaluation.Evaluation;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationQuery extends QueryBase<EvaluationEntity> {


    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UUID> excludedIds;

    private Collection<UUID> entityIds;

    private Collection<EvaluationStatus> status;

    private Collection<UUID> createdByIds;

    private Collection<EntityType> entityTypes;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public EvaluationQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public EvaluationQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public EvaluationQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public EvaluationQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public EvaluationQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public EvaluationQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public EvaluationQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public EvaluationQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public EvaluationQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public EvaluationQuery entityIds(UUID value) {
        this.entityIds = List.of(value);
        return this;
    }

    public EvaluationQuery entityIds(UUID... value) {
        this.entityIds = Arrays.asList(value);
        return this;
    }

    public EvaluationQuery entityIds(Collection<UUID> values) {
        this.entityIds = values;
        return this;
    }

    public EvaluationQuery status(EvaluationStatus value) {
        this.status = List.of(value);
        return this;
    }

    public EvaluationQuery status(EvaluationStatus... value) {
        this.status = Arrays.asList(value);
        return this;
    }

    public EvaluationQuery status(Collection<EvaluationStatus> values) {
        this.status = values;
        return this;
    }

    public EvaluationQuery createdByIds(UUID value) {
        this.createdByIds = List.of(value);
        return this;
    }

    public EvaluationQuery createdByIds(UUID... value) {
        this.createdByIds = Arrays.asList(value);
        return this;
    }

    public EvaluationQuery createdByIds(Collection<UUID> values) {
        this.createdByIds = values;
        return this;
    }

    public EvaluationQuery entityTypes(EntityType value) {
        this.entityTypes = List.of(value);
        return this;
    }

    public EvaluationQuery entityTypes(EntityType... value) {
        this.entityTypes = Arrays.asList(value);
        return this;
    }

    public EvaluationQuery entityTypes(Collection<EntityType> values) {
        this.entityTypes = values;
        return this;
    }

    public EvaluationQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public EvaluationQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public EvaluationQuery disableTracking() {
        this.noTracking = true;
        return this;
    }


    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public EvaluationQuery(
		    QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<EvaluationEntity> entityClass() {
        return EvaluationEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EvaluationEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EvaluationEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EvaluationEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.entityIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EvaluationEntity._entityId));
            for (UUID item : this.entityIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.status != null) {
            CriteriaBuilder.In<EvaluationStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EvaluationEntity._status));
            for (EvaluationStatus item : this.status)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.createdByIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EvaluationEntity._createdById));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.entityTypes != null) {
            CriteriaBuilder.In<EntityType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EvaluationEntity._entityType));
            for (EntityType item : this.entityTypes)
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
    protected EvaluationEntity convert(Tuple tuple, Set<String> columns) {
        EvaluationEntity item = new EvaluationEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, EvaluationEntity._id, UUID.class));
        item.setEntityType(QueryBase.convertSafe(tuple, columns, EvaluationEntity._entityType, EntityType.class));
        item.setEntityId(QueryBase.convertSafe(tuple, columns, EvaluationEntity._entityId, UUID.class));
        item.setEvaluatedAt(QueryBase.convertSafe(tuple, columns, EvaluationEntity._evaluatedAt, Instant.class));
        item.setData(QueryBase.convertSafe(tuple, columns, EvaluationEntity._data, String.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, EvaluationEntity._status, EvaluationStatus.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, EvaluationEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, EvaluationEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, EvaluationEntity._isActive, IsActive.class));
        item.setCreatedById(QueryBase.convertSafe(tuple, columns, EvaluationEntity._createdById, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, EvaluationEntity._tenantId, UUID.class));


        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(Evaluation._id)) return EvaluationEntity._id;
        else if (item.match(Evaluation._entityType)) return EvaluationEntity._entityType;
        else if (item.match(Evaluation._entityId)) return EvaluationEntity._entityId;
        else if (item.match(Evaluation._evaluatedAt)) return EvaluationEntity._evaluatedAt;
        else if (item.match(Evaluation._data)) return EvaluationEntity._data;
        else if (item.prefix(Evaluation._data)) return EvaluationEntity._data;
        else if (item.match(Evaluation._status)) return EvaluationEntity._status;
        else if (item.match(Evaluation._createdAt)) return EvaluationEntity._createdAt;
        else if (item.match(Evaluation._updatedAt)) return EvaluationEntity._updatedAt;
        else if (item.match(Evaluation._isActive)) return EvaluationEntity._isActive;
        else if (item.match(Evaluation._createdById)) return EvaluationEntity._createdById;
        else if (item.match(Evaluation._hash)) return EvaluationEntity._updatedAt;
        else if (item.match(Evaluation._belongsToCurrentTenant)) return EvaluationEntity._tenantId;
        else if (item.match(EvaluationEntity._tenantId)) return EvaluationEntity._tenantId;
        else return null;
    }

}
