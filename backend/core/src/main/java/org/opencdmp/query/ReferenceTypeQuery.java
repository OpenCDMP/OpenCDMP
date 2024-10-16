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
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeQuery extends QueryBase<ReferenceTypeEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<String> codes;

    private Collection<UUID> excludedIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public ReferenceTypeQuery like(String value) {
        this.like = value;
        return this;
    }

    public ReferenceTypeQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public ReferenceTypeQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public ReferenceTypeQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public ReferenceTypeQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public ReferenceTypeQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public ReferenceTypeQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public ReferenceTypeQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public ReferenceTypeQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public ReferenceTypeQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public ReferenceTypeQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public ReferenceTypeQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public ReferenceTypeQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public ReferenceTypeQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public ReferenceTypeQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public ReferenceTypeQuery disableTracking() {
        this.noTracking = true;
        return this;
    }


    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public ReferenceTypeQuery(
		    QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<ReferenceTypeEntity> entityClass() {
        return ReferenceTypeEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.codes);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceTypeEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(ReferenceTypeEntity._code), this.like),
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(ReferenceTypeEntity._name), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceTypeEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceTypeEntity._code));
            for (String item : this.codes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._id));
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
    protected ReferenceTypeEntity convert(Tuple tuple, Set<String> columns) {
        ReferenceTypeEntity item = new ReferenceTypeEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._tenantId, UUID.class));
        item.setName(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._name, String.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._code, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._isActive, IsActive.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, ReferenceTypeEntity._definition, String.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(ReferenceType._id)) return ReferenceTypeEntity._id;
        else if (item.match(ReferenceType._name)) return ReferenceTypeEntity._name;
        else if (item.match(ReferenceType._code)) return ReferenceTypeEntity._code;
        else if (item.match(ReferenceType._createdAt)) return ReferenceTypeEntity._createdAt;
        else if (item.match(ReferenceType._updatedAt)) return ReferenceTypeEntity._updatedAt;
        else if (item.match(ReferenceType._hash)) return ReferenceTypeEntity._updatedAt;
        else if (item.match(ReferenceType._isActive)) return ReferenceTypeEntity._isActive;
        else if (item.match(ReferenceType._definition)) return ReferenceTypeEntity._definition;
        else if (item.prefix(ReferenceType._definition)) return ReferenceTypeEntity._definition;
        else if (item.match(ReferenceTypeEntity._tenantId)) return ReferenceTypeEntity._tenantId;
        else if (item.match(ReferenceType._belongsToCurrentTenant)) return ReferenceTypeEntity._tenantId;
        else return null;
    }

}
