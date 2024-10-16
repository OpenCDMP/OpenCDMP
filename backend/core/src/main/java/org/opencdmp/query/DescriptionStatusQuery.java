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
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusQuery extends QueryBase<DescriptionStatusEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<org.opencdmp.commons.enums.DescriptionStatus> internalStatuses;

    private Collection<UUID> excludeIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public DescriptionStatusQuery like(String value) {
        this.like = value;
        return this;
    }

    public DescriptionStatusQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DescriptionStatusQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DescriptionStatusQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DescriptionStatusQuery excludeIds(UUID value) {
        this.excludeIds = List.of(value);
        return this;
    }

    public DescriptionStatusQuery excludeIds(UUID... value) {
        this.excludeIds = Arrays.asList(value);
        return this;
    }

    public DescriptionStatusQuery excludeIds(Collection<UUID> values) {
        this.excludeIds = values;
        return this;
    }

    public DescriptionStatusQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DescriptionStatusQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DescriptionStatusQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DescriptionStatusQuery internalStatuses(org.opencdmp.commons.enums.DescriptionStatus value) {
        this.internalStatuses = List.of(value);
        return this;
    }

    public DescriptionStatusQuery internalStatuses(org.opencdmp.commons.enums.DescriptionStatus... value) {
        this.internalStatuses = Arrays.asList(value);
        return this;
    }

    public DescriptionStatusQuery internalStatuses(Collection<org.opencdmp.commons.enums.DescriptionStatus> values) {
        this.internalStatuses = values;
        return this;
    }

    public DescriptionStatusQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DescriptionStatusQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    public DescriptionStatusQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager entityManager;

    public DescriptionStatusQuery(
            QueryUtilsService queryUtilsService, TenantEntityManager entityManager) {
        this.queryUtilsService = queryUtilsService;
        this.entityManager = entityManager;
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
    protected Class<DescriptionStatusEntity> entityClass() { return DescriptionStatusEntity.class; }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();

        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionStatusEntity._id));
            for (UUID id : this.ids)
                inClause.value(id);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionStatusEntity._name), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionStatusEntity._description), this.like)));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionStatusEntity._isActive));
            for (IsActive isActive : this.isActives)
                inClause.value(isActive);
            predicates.add(inClause);
        }
        if (this.internalStatuses != null) {
            CriteriaBuilder.In<org.opencdmp.commons.enums.DescriptionStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionStatusEntity._internalStatus));
            for (org.opencdmp.commons.enums.DescriptionStatus internalStatus : this.internalStatuses)
                inClause.value(internalStatus);
            predicates.add(inClause);
        }
        if (this.excludeIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionStatusEntity._id));
            for (UUID id : this.excludeIds) {
                notInClause.value(id);
            }
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
        if (item.match(DescriptionStatus._id))
            return DescriptionStatusEntity._id;
        else if (item.match(DescriptionStatus._name))
            return DescriptionStatusEntity._name;
        else if (item.match(DescriptionStatus._description))
            return DescriptionStatusEntity._description;
        else if (item.match(DescriptionStatus._createdAt))
            return DescriptionStatusEntity._createdAt;
        else if (item.match(DescriptionStatus._updatedAt))
            return DescriptionStatusEntity._updatedAt;
        else if (item.match(DescriptionStatus._isActive))
            return DescriptionStatusEntity._isActive;
        else if (item.match(DescriptionStatus._belongsToCurrentTenant))
            return DescriptionStatusEntity._tenantId;
        else if (item.match(DescriptionStatus._internalStatus))
            return DescriptionStatusEntity._internalStatus;
        else if (item.match(DescriptionStatusEntity._definition))
            return DescriptionStatusEntity._definition;
        else
            return null;
    }

    @Override
    protected DescriptionStatusEntity convert(Tuple tuple, Set<String> columns) {
        DescriptionStatusEntity item = new DescriptionStatusEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._id, UUID.class));
        item.setName(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._name, String.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._description, String.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._tenantId, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._isActive, IsActive.class));
        item.setInternalStatus(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._internalStatus, org.opencdmp.commons.enums.DescriptionStatus.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, DescriptionStatusEntity._definition, String.class));
        return item;
    }
}
