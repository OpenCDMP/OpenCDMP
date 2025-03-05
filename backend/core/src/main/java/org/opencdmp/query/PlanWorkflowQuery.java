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
import org.opencdmp.data.PlanWorkflowEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.planworkflow.PlanWorkflow;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanWorkflowQuery extends QueryBase<PlanWorkflowEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UUID> excludedIds;

    private Collection<UUID> tenantIds;

    private Boolean tenantIsSet;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    protected boolean noTracking;


    public PlanWorkflowQuery like(String value) {
        this.like = like;
        return this;
    }

    public PlanWorkflowQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanWorkflowQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanWorkflowQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanWorkflowQuery isActives(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanWorkflowQuery isActives(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanWorkflowQuery isActives(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanWorkflowQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PlanWorkflowQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PlanWorkflowQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PlanWorkflowQuery tenantIds(UUID value) {
        this.tenantIds = List.of(value);
        return this;
    }

    public PlanWorkflowQuery tenantIds(UUID... value) {
        this.tenantIds = Arrays.asList(value);
        return this;
    }

    public PlanWorkflowQuery tenantIds(Collection<UUID> values) {
        this.tenantIds = values;
        return this;
    }

    public PlanWorkflowQuery clearTenantIds() {
        this.tenantIds = null;
        return this;
    }

    public PlanWorkflowQuery tenantIsSet(Boolean values) {
        this.tenantIsSet = values;
        return this;
    }

    public PlanWorkflowQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanWorkflowQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanWorkflowQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;

    private final TenantEntityManager entityManager;

    public PlanWorkflowQuery(QueryUtilsService queryUtilsService, TenantEntityManager entityManager) {
        this.queryUtilsService = queryUtilsService;
        this.entityManager = entityManager;
    }

    @Override
    protected EntityManager entityManager(){ return this.entityManager.getEntityManager(); }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives);
    }

    @Override
    protected Class<PlanWorkflowEntity> entityClass() {
        return PlanWorkflowEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanWorkflowEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(
               this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanWorkflowEntity._name), this.like),
               this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanWorkflowEntity._description), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanWorkflowEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanWorkflowEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.tenantIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanWorkflowEntity._tenantId));
            for (UUID item : this.tenantIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantIsSet != null) {
            if (this.tenantIsSet) predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(PlanWorkflowEntity._tenantId)));
            else predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(PlanWorkflowEntity._tenantId)));
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
        if (item.match(PlanWorkflow._id))
            return PlanWorkflowEntity._id;
        else if (item.match(PlanWorkflow._name))
            return PlanWorkflowEntity._name;
        else if (item.match(PlanWorkflow._description))
            return PlanWorkflowEntity._description;
        else if (item.match(PlanWorkflow._createdAt))
            return PlanWorkflowEntity._createdAt;
        else if (item.match(PlanWorkflow._updatedAt))
            return PlanWorkflowEntity._updatedAt;
        else if (item.match(PlanWorkflow._isActive))
            return PlanWorkflowEntity._isActive;
        else if (item.match(PlanWorkflow._definition))
            return PlanWorkflowEntity._definition;
        else if (item.match(PlanWorkflow._hash))
            return PlanWorkflowEntity._updatedAt;
        else if (item.match(PlanWorkflow._belongsToCurrentTenant))
            return PlanWorkflowEntity._tenantId;
        return null;
    }

    @Override
    protected PlanWorkflowEntity convert(Tuple tuple, Set<String> columns) {
        PlanWorkflowEntity item = new PlanWorkflowEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._id, UUID.class));
        item.setName(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._name, String.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._description, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._isActive, IsActive.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._tenantId, UUID.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, PlanWorkflowEntity._definition, String.class));
        return item;
    }
}
