package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.DescriptionTemplateTypeStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateTypeQuery extends QueryBase<DescriptionTemplateTypeEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<DescriptionTemplateTypeStatus> statuses;

    private Collection<String> codes;

    private Collection<UUID> excludedIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public DescriptionTemplateTypeQuery like(String value) {
        this.like = value;
        return this;
    }

    public DescriptionTemplateTypeQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DescriptionTemplateTypeQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateTypeQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DescriptionTemplateTypeQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DescriptionTemplateTypeQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateTypeQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DescriptionTemplateTypeQuery statuses(DescriptionTemplateTypeStatus value) {
        this.statuses = List.of(value);
        return this;
    }

    public DescriptionTemplateTypeQuery statuses(DescriptionTemplateTypeStatus... value) {
        this.statuses = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateTypeQuery statuses(Collection<DescriptionTemplateTypeStatus> values) {
        this.statuses = values;
        return this;
    }

    public DescriptionTemplateTypeQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public DescriptionTemplateTypeQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateTypeQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public DescriptionTemplateTypeQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public DescriptionTemplateTypeQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public DescriptionTemplateTypeQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateTypeQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionTemplateTypeQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DescriptionTemplateTypeQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public DescriptionTemplateTypeQuery(
		    QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }
    
    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }
    
    @Override
    protected Class<DescriptionTemplateTypeEntity> entityClass() {
        return DescriptionTemplateTypeEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.statuses);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateTypeEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionTemplateTypeEntity._code), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionTemplateTypeEntity._name), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateTypeEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.statuses != null) {
            CriteriaBuilder.In<DescriptionTemplateTypeStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateTypeEntity._status));
            for (DescriptionTemplateTypeStatus item : this.statuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateTypeEntity._code));
            for (String item : this.codes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateTypeEntity._id));
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
    protected DescriptionTemplateTypeEntity convert(Tuple tuple, Set<String> columns) {
        DescriptionTemplateTypeEntity item = new DescriptionTemplateTypeEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._tenantId, UUID.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._code, String.class));
        item.setName(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._name, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._isActive, IsActive.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, DescriptionTemplateTypeEntity._status, DescriptionTemplateTypeStatus.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(DescriptionTemplateType._id)) return DescriptionTemplateTypeEntity._id;
        else if (item.match(DescriptionTemplateType._code)) return DescriptionTemplateTypeEntity._code;
        else if (item.match(DescriptionTemplateType._name)) return DescriptionTemplateTypeEntity._name;
        else if (item.match(DescriptionTemplateType._createdAt)) return DescriptionTemplateTypeEntity._createdAt;
        else if (item.match(DescriptionTemplateType._updatedAt)) return DescriptionTemplateTypeEntity._updatedAt;
        else if (item.match(DescriptionTemplateType._hash)) return DescriptionTemplateTypeEntity._updatedAt;
        else if (item.match(DescriptionTemplateType._isActive)) return DescriptionTemplateTypeEntity._isActive;
        else if (item.match(DescriptionTemplateType._status)) return DescriptionTemplateTypeEntity._status;
        else if (item.match(DescriptionTemplateType._belongsToCurrentTenant)) return DescriptionTemplateTypeEntity._tenantId;
        else return null;
    }

}
