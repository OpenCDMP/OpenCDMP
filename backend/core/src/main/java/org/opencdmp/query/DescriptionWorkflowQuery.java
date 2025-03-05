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
import org.opencdmp.data.DescriptionWorkflowEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.descriptionworkflow.DescriptionWorkflow;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionWorkflowQuery extends QueryBase<DescriptionWorkflowEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UUID> excludedIds;

    private Collection<UUID> tenantIds;

    private Boolean tenantIsSet;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    protected boolean noTracking;


    public DescriptionWorkflowQuery like(String value) {
        this.like = like;
        return this;
    }

    public DescriptionWorkflowQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DescriptionWorkflowQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DescriptionWorkflowQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DescriptionWorkflowQuery isActives(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DescriptionWorkflowQuery isActives(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DescriptionWorkflowQuery isActives(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DescriptionWorkflowQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public DescriptionWorkflowQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public DescriptionWorkflowQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public DescriptionWorkflowQuery tenantIds(UUID value) {
        this.tenantIds = List.of(value);
        return this;
    }

    public DescriptionWorkflowQuery tenantIds(UUID... value) {
        this.tenantIds = Arrays.asList(value);
        return this;
    }

    public DescriptionWorkflowQuery tenantIds(Collection<UUID> values) {
        this.tenantIds = values;
        return this;
    }

    public DescriptionWorkflowQuery clearTenantIds() {
        this.tenantIds = null;
        return this;
    }

    public DescriptionWorkflowQuery tenantIsSet(Boolean values) {
        this.tenantIsSet = values;
        return this;
    }

    public DescriptionWorkflowQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionWorkflowQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DescriptionWorkflowQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;

    private final TenantEntityManager entityManager;

    public DescriptionWorkflowQuery(QueryUtilsService queryUtilsService, TenantEntityManager entityManager) {
        this.queryUtilsService = queryUtilsService;
        this.entityManager = entityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.entityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives);
    }

    @Override
    protected Class<DescriptionWorkflowEntity> entityClass() {
        return DescriptionWorkflowEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionWorkflowEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(
               this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionWorkflowEntity._name), this.like),
               this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionWorkflowEntity._description), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionWorkflowEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionWorkflowEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.tenantIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionWorkflowEntity._tenantId));
            for (UUID item : this.tenantIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantIsSet != null) {
            if (this.tenantIsSet) predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(DescriptionWorkflowEntity._tenantId)));
            else predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(DescriptionWorkflowEntity._tenantId)));
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
        if (item.match(DescriptionWorkflow._id))
            return DescriptionWorkflowEntity._id;
        else if (item.match(DescriptionWorkflow._name))
            return DescriptionWorkflowEntity._name;
        else if (item.match(DescriptionWorkflow._description))
            return DescriptionWorkflowEntity._description;
        else if (item.match(DescriptionWorkflow._createdAt))
            return DescriptionWorkflowEntity._createdAt;
        else if (item.match(DescriptionWorkflow._updatedAt))
            return DescriptionWorkflowEntity._updatedAt;
        else if (item.match(DescriptionWorkflow._isActive))
            return DescriptionWorkflowEntity._isActive;
        else if (item.match(DescriptionWorkflow._definition))
            return DescriptionWorkflowEntity._definition;
        else if (item.match(DescriptionWorkflow._hash))
            return DescriptionWorkflowEntity._updatedAt;
        else if (item.match(DescriptionWorkflow._belongsToCurrentTenant))
            return DescriptionWorkflowEntity._tenantId;
        return null;
    }

    @Override
    protected DescriptionWorkflowEntity convert(Tuple tuple, Set<String> columns) {
        DescriptionWorkflowEntity item = new DescriptionWorkflowEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._id, UUID.class));
        item.setName(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._name, String.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._description, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._isActive, IsActive.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._tenantId, UUID.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, DescriptionWorkflowEntity._definition, String.class));
        return item;
    }
}
