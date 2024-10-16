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
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.DescriptionTagEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.DescriptionTag;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTagQuery extends QueryBase<DescriptionTagEntity> {

    private Collection<UUID> ids;

    private Collection<UUID> tagIds;

    private Collection<UUID> excludedIds;

    private Collection<UUID> descriptionIds;

    private Collection<IsActive> isActives;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    public DescriptionTagQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DescriptionTagQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DescriptionTagQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DescriptionTagQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public DescriptionTagQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public DescriptionTagQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTagQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DescriptionTagQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DescriptionTagQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DescriptionTagQuery tagIds(UUID value) {
        this.tagIds = List.of(value);
        return this;
    }

    public DescriptionTagQuery tagIds(UUID... value) {
        this.tagIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTagQuery tagIds(Collection<UUID> values) {
        this.tagIds = values;
        return this;
    }

    public DescriptionTagQuery descriptionIds(UUID value) {
        this.descriptionIds = List.of(value);
        return this;
    }

    public DescriptionTagQuery descriptionIds(UUID... value) {
        this.descriptionIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTagQuery descriptionIds(Collection<UUID> values) {
        this.descriptionIds = values;
        return this;
    }

    public DescriptionTagQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionTagQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DescriptionTagQuery disableTracking() {
        this.noTracking = true;
        return this;
    }


    private final TenantEntityManager tenantEntityManager;
    public DescriptionTagQuery(TenantEntityManager tenantEntityManager) {
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<DescriptionTagEntity> entityClass() {
        return DescriptionTagEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.excludedIds) || this.isEmpty(this.isActives) ||this.isEmpty(this.tagIds) || this.isEmpty(this.descriptionIds);
    }


    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTagEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.tagIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTagEntity._tagId));
            for (UUID item : this.tagIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.descriptionIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTagEntity._descriptionId));
            for (UUID item : this.descriptionIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTagEntity._isActive));
            for (IsActive item : this.isActives)
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
    protected DescriptionTagEntity convert(Tuple tuple, Set<String> columns) {
        DescriptionTagEntity item = new DescriptionTagEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DescriptionTagEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DescriptionTagEntity._tenantId, UUID.class));
        item.setTagId(QueryBase.convertSafe(tuple, columns, DescriptionTagEntity._tagId, UUID.class));
        item.setDescriptionId(QueryBase.convertSafe(tuple, columns, DescriptionTagEntity._descriptionId, UUID.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DescriptionTagEntity._isActive, IsActive.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DescriptionTagEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DescriptionTagEntity._updatedAt, Instant.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(DescriptionTag._id)) return DescriptionTagEntity._id;
        else if (item.prefix(DescriptionTag._tag)) return DescriptionTagEntity._tagId;
        else if (item.prefix(DescriptionTag._description)) return DescriptionTagEntity._descriptionId;
        else if (item.match(DescriptionTag._isActive)) return DescriptionTagEntity._isActive;
        else if (item.match(DescriptionTag._createdAt)) return DescriptionTagEntity._createdAt;
        else if (item.match(DescriptionTag._updatedAt)) return DescriptionTagEntity._updatedAt;
        else if (item.match(DescriptionTag._hash)) return DescriptionTagEntity._updatedAt;
        else if (item.match(DescriptionTag._belongsToCurrentTenant)) return DescriptionTagEntity._tenantId;
        else return null;
    }

}
