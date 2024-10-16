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
import org.opencdmp.data.PrefillingSourceEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrefillingSourceQuery extends QueryBase<PrefillingSourceEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UUID> excludedIds;
    private Collection<String> codes;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    public PrefillingSourceQuery like(String value) {
        this.like = value;
        return this;
    }

    public PrefillingSourceQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PrefillingSourceQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PrefillingSourceQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PrefillingSourceQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public PrefillingSourceQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public PrefillingSourceQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public PrefillingSourceQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PrefillingSourceQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PrefillingSourceQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PrefillingSourceQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PrefillingSourceQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PrefillingSourceQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PrefillingSourceQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PrefillingSourceQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PrefillingSourceQuery disableTracking() {
        this.noTracking = true;
        return this;
    }


    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public PrefillingSourceQuery(
		    TenantEntityManager tenantEntityManager, QueryUtilsService queryUtilsService) {
	    this.tenantEntityManager = tenantEntityManager;
	    this.queryUtilsService = queryUtilsService;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<PrefillingSourceEntity> entityClass() {
        return PrefillingSourceEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.codes) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PrefillingSourceEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PrefillingSourceEntity._code), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PrefillingSourceEntity._label), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PrefillingSourceEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PrefillingSourceEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PrefillingSourceEntity._code));
            for (String item : this.codes)
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
    protected PrefillingSourceEntity convert(Tuple tuple, Set<String> columns) {
        PrefillingSourceEntity item = new PrefillingSourceEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._id, UUID.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._code, String.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._label, String.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._definition, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PrefillingSourceEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(PrefillingSource._id)) return PrefillingSourceEntity._id;
        else if (item.match(PrefillingSource._label)) return PrefillingSourceEntity._label;
        else if (item.match(PrefillingSource._code)) return PrefillingSourceEntity._code;
        else if (item.prefix(PrefillingSource._definition)) return PrefillingSourceEntity._definition;
        else if (item.match(PrefillingSource._createdAt)) return PrefillingSourceEntity._createdAt;
        else if (item.match(PrefillingSource._updatedAt)) return PrefillingSourceEntity._updatedAt;
        else if (item.match(PrefillingSource._hash)) return PrefillingSourceEntity._updatedAt;
        else if (item.match(PrefillingSource._isActive)) return PrefillingSourceEntity._isActive;
        else if (item.match(PrefillingSource._belongsToCurrentTenant)) return PrefillingSourceEntity._tenantId;
        else return null;
    }

}
