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
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.data.SupportiveMaterialEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.SupportiveMaterial;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SupportiveMaterialQuery extends QueryBase<SupportiveMaterialEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<SupportiveMaterialFieldType> types;

    private Collection<String> languageCodes;

    private Collection<UUID> excludedIds;

    private Collection<UUID> tenantIds;

    private Boolean tenantIsSet;

    private TenantQuery tenantQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public SupportiveMaterialQuery like(String value) {
        this.like = value;
        return this;
    }

    public SupportiveMaterialQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public SupportiveMaterialQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public SupportiveMaterialQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public SupportiveMaterialQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public SupportiveMaterialQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public SupportiveMaterialQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public SupportiveMaterialQuery types(SupportiveMaterialFieldType value) {
        this.types = List.of(value);
        return this;
    }

    public SupportiveMaterialQuery types(SupportiveMaterialFieldType... value) {
        this.types = Arrays.asList(value);
        return this;
    }

    public SupportiveMaterialQuery types(Collection<SupportiveMaterialFieldType> values) {
        this.types = values;
        return this;
    }

    public SupportiveMaterialQuery languageCodes(String value) {
        this.languageCodes = List.of(value);
        return this;
    }

    public SupportiveMaterialQuery languageCodes(String... value) {
        this.languageCodes = Arrays.asList(value);
        return this;
    }

    public SupportiveMaterialQuery languageCodes(Collection<String> values) {
        this.languageCodes = values;
        return this;
    }

    public SupportiveMaterialQuery clearTenantIds() {
        this.tenantIds = null;
        return this;
    }

    public SupportiveMaterialQuery tenantIds(UUID value) {
        this.tenantIds = List.of(value);
        return this;
    }

    public SupportiveMaterialQuery tenantIds(UUID... value) {
        this.tenantIds = Arrays.asList(value);
        return this;
    }

    public SupportiveMaterialQuery tenantIds(Collection<UUID> values) {
        this.tenantIds = values;
        return this;
    }

    public SupportiveMaterialQuery tenantIsSet(Boolean value) {
        this.tenantIsSet = value;
        return this;
    }

    public SupportiveMaterialQuery tenantSubQuery(TenantQuery tenantQuery) {
        this.tenantQuery = tenantQuery;
        return this;
    }

    public SupportiveMaterialQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public SupportiveMaterialQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public SupportiveMaterialQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public SupportiveMaterialQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public SupportiveMaterialQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public SupportiveMaterialQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public SupportiveMaterialQuery(
		    QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<SupportiveMaterialEntity> entityClass() {
        return SupportiveMaterialEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.types) || this.isEmpty(this.languageCodes);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(SupportiveMaterialEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(SupportiveMaterialEntity._languageCode), this.like),
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(SupportiveMaterialEntity._payload), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(SupportiveMaterialEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.types != null) {
            CriteriaBuilder.In<SupportiveMaterialFieldType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(SupportiveMaterialEntity._type));
            for (SupportiveMaterialFieldType item : this.types)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.languageCodes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(SupportiveMaterialEntity._languageCode));
            for (String item : this.languageCodes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(SupportiveMaterialEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.tenantIsSet != null) {
            if (this.tenantIsSet) predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(SupportiveMaterialEntity._tenantId)));
            else predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(SupportiveMaterialEntity._tenantId)));
        }
        if (this.tenantIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(SupportiveMaterialEntity._tenantId));
            for (UUID item : this.tenantIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantQuery != null) {
            QueryContext<TenantEntity, UUID> subQuery = this.applySubQuery(this.tenantQuery, queryContext, UUID.class, tenantEntityRoot -> tenantEntityRoot.get(TenantEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(SupportiveMaterialEntity._tenantId)).value(subQuery.Query));
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected SupportiveMaterialEntity convert(Tuple tuple, Set<String> columns) {
        SupportiveMaterialEntity item = new SupportiveMaterialEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._tenantId, UUID.class));
        item.setType(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._type, SupportiveMaterialFieldType.class));
        item.setLanguageCode(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._languageCode, String.class));
        item.setPayload(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._payload, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, SupportiveMaterialEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(SupportiveMaterial._id)) return SupportiveMaterialEntity._id;
        else if (item.match(SupportiveMaterial._type)) return SupportiveMaterialEntity._type;
        else if (item.match(SupportiveMaterial._languageCode)) return SupportiveMaterialEntity._languageCode;
        else if (item.match(SupportiveMaterial._payload)) return SupportiveMaterialEntity._payload;
        else if (item.match(SupportiveMaterial._createdAt)) return SupportiveMaterialEntity._createdAt;
        else if (item.match(SupportiveMaterial._updatedAt)) return SupportiveMaterialEntity._updatedAt;
        else if (item.match(SupportiveMaterial._hash)) return SupportiveMaterialEntity._updatedAt;
        else if (item.match(SupportiveMaterial._isActive)) return SupportiveMaterialEntity._isActive;
        else if (item.match(SupportiveMaterial._belongsToCurrentTenant)) return SupportiveMaterialEntity._tenantId;
        else return null;
    }

}
