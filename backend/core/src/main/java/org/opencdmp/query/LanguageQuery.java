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
import org.opencdmp.data.LanguageEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.Language;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LanguageQuery extends QueryBase<LanguageEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<String> codes;

    private Collection<UUID> excludedIds;
    private Collection<UUID> tenantIds;
    private Boolean tenantIsSet;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public LanguageQuery like(String value) {
        this.like = value;
        return this;
    }

    public LanguageQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public LanguageQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public LanguageQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public LanguageQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public LanguageQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public LanguageQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public LanguageQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public LanguageQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public LanguageQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public LanguageQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public LanguageQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public LanguageQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public LanguageQuery clearTenantIds() {
        this.tenantIds = null;
        return this;
    }

    public LanguageQuery tenantIds(UUID value) {
        this.tenantIds = List.of(value);
        return this;
    }

    public LanguageQuery tenantIds(UUID... value) {
        this.tenantIds = Arrays.asList(value);
        return this;
    }

    public LanguageQuery tenantIds(Collection<UUID> values) {
        this.tenantIds = values;
        return this;
    }

    public LanguageQuery tenantIsSet(Boolean value) {
        this.tenantIsSet = value;
        return this;
    }

    public LanguageQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public LanguageQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public LanguageQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public LanguageQuery(
		    QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<LanguageEntity> entityClass() {
        return LanguageEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.codes) || this.isEmpty(this.tenantIds);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LanguageEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(LanguageEntity._code), this.like));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LanguageEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LanguageEntity._code));
            for (String item : this.codes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LanguageEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.tenantIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(LanguageEntity._tenantId));
            for (UUID item : this.tenantIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantIsSet != null) {
            if (this.tenantIsSet) predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(LanguageEntity._tenantId)));
            else predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(LanguageEntity._tenantId)));
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected LanguageEntity convert(Tuple tuple, Set<String> columns) {
        LanguageEntity item = new LanguageEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, LanguageEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, LanguageEntity._tenantId, UUID.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, LanguageEntity._code, String.class));
        item.setPayload(QueryBase.convertSafe(tuple, columns, LanguageEntity._payload, String.class));
        item.setOrdinal(QueryBase.convertSafe(tuple, columns, LanguageEntity._ordinal, Integer.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, LanguageEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, LanguageEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, LanguageEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(Language._id)) return LanguageEntity._id;
        else if (item.match(Language._code)) return LanguageEntity._code;
        else if (item.match(Language._payload)) return LanguageEntity._payload;
        else if (item.match(Language._ordinal)) return LanguageEntity._ordinal;
        else if (item.match(Language._createdAt)) return LanguageEntity._createdAt;
        else if (item.match(Language._updatedAt)) return LanguageEntity._updatedAt;
        else if (item.match(Language._hash)) return LanguageEntity._updatedAt;
        else if (item.match(Language._isActive)) return LanguageEntity._isActive;
        else if (item.match(Language._belongsToCurrentTenant)) return LanguageEntity._tenantId;
        else return null;
    }

}
