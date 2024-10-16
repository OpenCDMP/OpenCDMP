package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanStatusQuery extends QueryBase<PlanStatusEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<org.opencdmp.commons.enums.PlanStatus> internalStatuses;

    private Collection<UUID> excludedIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanStatusQuery like(String value) {
        this.like = value;
        return this;
    }

    public PlanStatusQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanStatusQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanStatusQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanStatusQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PlanStatusQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PlanStatusQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PlanStatusQuery isActives(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanStatusQuery isActives(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanStatusQuery isActives(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanStatusQuery internalStatuses(org.opencdmp.commons.enums.PlanStatus value) {
        this.internalStatuses = List.of(value);
        return this;
    }

    public PlanStatusQuery internalStatuses(org.opencdmp.commons.enums.PlanStatus... value) {
        this.internalStatuses = Arrays.asList(value);
        return this;
    }

    public PlanStatusQuery internalStatuses(Collection<org.opencdmp.commons.enums.PlanStatus> values) {
        this.internalStatuses = values;
        return this;
    }

    public PlanStatusQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanStatusQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    public PlanStatusQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager entityManager;

    public PlanStatusQuery(
            QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.queryUtilsService = queryUtilsService;
        this.entityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.entityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.internalStatuses);
    }

    @Override
    protected Class<PlanStatusEntity> entityClass() { return PlanStatusEntity.class; }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();

        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanStatusEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanStatusEntity._name), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanStatusEntity._description), this.like)));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanStatusEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.internalStatuses != null) {
            CriteriaBuilder.In<org.opencdmp.commons.enums.PlanStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanStatusEntity._internalStatus));
            for (org.opencdmp.commons.enums.PlanStatus item : this.internalStatuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanStatusEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
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
        if (item.match(PlanStatus._id))
            return PlanStatusEntity._id;
        else if (item.match(PlanStatus._description))
            return PlanStatusEntity._description;
        else if (item.match(PlanStatus._name))
            return PlanStatusEntity._name;
        else if (item.match(PlanStatus._internalStatus))
            return PlanStatusEntity._internalStatus;
        else if (item.match(PlanStatus._definition))
            return PlanStatusEntity._definition;
        else if (item.match(PlanStatus._createdAt))
            return PlanStatusEntity._createdAt;
        else if (item.match(PlanStatus._updatedAt))
            return PlanStatusEntity._updatedAt;
        else if (item.match(PlanStatus._isActive))
            return PlanStatusEntity._isActive;
        else if (item.match(PlanStatus._belongsToCurrentTenant))
            return PlanStatusEntity._tenantId;
        else
            return null;
    }

    @Override
    protected PlanStatusEntity convert(Tuple tuple, Set<String> columns) {
        PlanStatusEntity item = new PlanStatusEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._tenantId, UUID.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._description, String.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._definition, String.class));
        item.setName(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._name, String.class));
        item.setInternalStatus(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._internalStatus, org.opencdmp.commons.enums.PlanStatus.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanStatusEntity._isActive, IsActive.class));
        return item;
    }
}
