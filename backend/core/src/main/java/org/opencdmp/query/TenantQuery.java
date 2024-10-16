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
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.Tenant;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantQuery extends QueryBase<TenantEntity> {

    private String like;
    private Collection<UUID> ids;
    private Collection<String> codes;
    private Collection<IsActive> isActives;
    private Collection<UUID> excludedIds;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

	public TenantQuery(QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
		this.queryUtilsService = queryUtilsService;
		this.tenantEntityManager = tenantEntityManager;
	}

	public TenantQuery like(String value) {
        this.like = value;
        return this;
    }

    public TenantQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public TenantQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public TenantQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public TenantQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public TenantQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public TenantQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public TenantQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public TenantQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public TenantQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public TenantQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public TenantQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public TenantQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public TenantQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public TenantQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public TenantQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.codes) ||this.isEmpty(this.isActives);
    }

    @Override
    protected Class<TenantEntity> entityClass() {
        return TenantEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantEntity._id));
            for (UUID item : this.ids) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantEntity._code));
            for (String item : this.codes) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(TenantEntity._code), this.like),
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(TenantEntity._name), this.like)
            ));
        }

        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantEntity._isActive));
            for (IsActive item : this.isActives) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }

        if (predicates.size() > 0) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }

    }

    @Override
    protected TenantEntity convert(Tuple tuple, Set<String> columns) {
        TenantEntity item = new TenantEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, TenantEntity._id, UUID.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, TenantEntity._code, String.class));
        item.setName(QueryBase.convertSafe(tuple, columns, TenantEntity._name, String.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, TenantEntity._description, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, TenantEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, TenantEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, TenantEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(Tenant._id)) return TenantEntity._id;
        else if (item.match(Tenant._code)) return TenantEntity._code;
        else if (item.match(Tenant._name)) return TenantEntity._name;
        else if (item.match(Tenant._description)) return TenantEntity._description;
        else if (item.match(Tenant._createdAt)) return TenantEntity._createdAt;
        else if (item.match(Tenant._updatedAt)) return TenantEntity._updatedAt;
        else if (item.match(Tenant._isActive)) return TenantEntity._isActive;
        else return null;
    }

}
