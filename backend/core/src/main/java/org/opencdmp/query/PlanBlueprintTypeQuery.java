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
import org.opencdmp.commons.enums.PlanBlueprintTypeStatus;
import org.opencdmp.data.PlanBlueprintTypeEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.model.PlanBlueprintType;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintTypeQuery extends QueryBase<PlanBlueprintTypeEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<PlanBlueprintTypeStatus> statuses;

    private Collection<String> codes;

    private Collection<UUID> excludedIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanBlueprintTypeQuery like(String value) {
        this.like = value;
        return this;
    }

    public PlanBlueprintTypeQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanBlueprintTypeQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintTypeQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanBlueprintTypeQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanBlueprintTypeQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintTypeQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanBlueprintTypeQuery statuses(PlanBlueprintTypeStatus value) {
        this.statuses = List.of(value);
        return this;
    }

    public PlanBlueprintTypeQuery statuses(PlanBlueprintTypeStatus... value) {
        this.statuses = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintTypeQuery statuses(Collection<PlanBlueprintTypeStatus> values) {
        this.statuses = values;
        return this;
    }

    public PlanBlueprintTypeQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public PlanBlueprintTypeQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintTypeQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public PlanBlueprintTypeQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PlanBlueprintTypeQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PlanBlueprintTypeQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintTypeQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanBlueprintTypeQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanBlueprintTypeQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    public PlanBlueprintTypeQuery(
		    QueryUtilsService queryUtilsService, TenantEntityManagerFactory tenantEntityManagerFactory) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }
    
    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManagerFactory.getInstance().getEntityManager();
    }
    
    @Override
    protected Class<PlanBlueprintTypeEntity> entityClass() {
        return PlanBlueprintTypeEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.statuses);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintTypeEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanBlueprintTypeEntity._code), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanBlueprintTypeEntity._name), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintTypeEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.statuses != null) {
            CriteriaBuilder.In<PlanBlueprintTypeStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintTypeEntity._status));
            for (PlanBlueprintTypeStatus item : this.statuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintTypeEntity._code));
            for (String item : this.codes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintTypeEntity._id));
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
    protected PlanBlueprintTypeEntity convert(Tuple tuple, Set<String> columns) {
        PlanBlueprintTypeEntity item = new PlanBlueprintTypeEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._tenantId, UUID.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._code, String.class));
        item.setName(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._name, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._isActive, IsActive.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, PlanBlueprintTypeEntity._status, PlanBlueprintTypeStatus.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(PlanBlueprintType._id)) return PlanBlueprintTypeEntity._id;
        else if (item.match(PlanBlueprintType._code)) return PlanBlueprintTypeEntity._code;
        else if (item.match(PlanBlueprintType._name)) return PlanBlueprintTypeEntity._name;
        else if (item.match(PlanBlueprintType._createdAt)) return PlanBlueprintTypeEntity._createdAt;
        else if (item.match(PlanBlueprintType._updatedAt)) return PlanBlueprintTypeEntity._updatedAt;
        else if (item.match(PlanBlueprintType._hash)) return PlanBlueprintTypeEntity._updatedAt;
        else if (item.match(PlanBlueprintType._isActive)) return PlanBlueprintTypeEntity._isActive;
        else if (item.match(PlanBlueprintType._status)) return PlanBlueprintTypeEntity._status;
        else if (item.match(PlanBlueprintType._belongsToCurrentTenant)) return PlanBlueprintTypeEntity._tenantId;
        else return null;
    }

}
