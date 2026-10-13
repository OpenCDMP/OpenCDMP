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
import org.opencdmp.data.DepositMetadataEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DepositMetadataQuery extends QueryBase<DepositMetadataEntity> {

    private Collection<UUID> ids;

    private Collection<UUID> planIds;

    private Collection<String> repositoryIds;

    private Collection<IsActive> isActives;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public DepositMetadataQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DepositMetadataQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DepositMetadataQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DepositMetadataQuery planIds(UUID value) {
        this.planIds = List.of(value);
        return this;
    }

    public DepositMetadataQuery planIds(UUID... value) {
        this.planIds = Arrays.asList(value);
        return this;
    }

    public DepositMetadataQuery planIds(Collection<UUID> values) {
        this.planIds = values;
        return this;
    }

    public DepositMetadataQuery repositoryIds(String value) {
        this.repositoryIds = List.of(value);
        return this;
    }

    public DepositMetadataQuery repositoryIds(String... value) {
        this.repositoryIds = Arrays.asList(value);
        return this;
    }

    public DepositMetadataQuery repositoryIds(Collection<String> values) {
        this.repositoryIds = values;
        return this;
    }

    public DepositMetadataQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DepositMetadataQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DepositMetadataQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DepositMetadataQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DepositMetadataQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DepositMetadataQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    public DepositMetadataQuery(QueryUtilsService queryUtilsService, TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.queryUtilsService = queryUtilsService;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    protected EntityManager entityManager() {
        return this.tenantEntityManagerFactory.getInstance().getEntityManager();
    }

    @Override
    protected Class<DepositMetadataEntity> entityClass() {
        return DepositMetadataEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.planIds) || this.isEmpty(this.repositoryIds) || this.isEmpty(this.isActives);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        return this.queryUtilsService.buildTenantFilter(queryContext.CriteriaBuilder, queryContext.Root.get(DepositMetadataEntity._tenantId));
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DepositMetadataEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.planIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DepositMetadataEntity._planId));
            for (UUID item : this.planIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.repositoryIds != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DepositMetadataEntity._repositoryId));
            for (String item : this.repositoryIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DepositMetadataEntity._isActive));
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
    protected DepositMetadataEntity convert(Tuple tuple, Set<String> columns) {
        DepositMetadataEntity item = new DepositMetadataEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._tenantId, UUID.class));
        item.setPlanId(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._planId, UUID.class));
        item.setRepositoryId(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._repositoryId, String.class));
        item.setPayload(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._payload, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DepositMetadataEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(DepositMetadataEntity._id))
            return DepositMetadataEntity._id;
        else if (item.match(DepositMetadataEntity._planId))
            return DepositMetadataEntity._planId;
        else if (item.match(DepositMetadataEntity._repositoryId))
            return DepositMetadataEntity._repositoryId;
        else if (item.match(DepositMetadataEntity._payload))
            return DepositMetadataEntity._payload;
        else if (item.match(DepositMetadataEntity._createdAt))
            return DepositMetadataEntity._createdAt;
        else if (item.match(DepositMetadataEntity._updatedAt))
            return DepositMetadataEntity._updatedAt;
        else if (item.match(DepositMetadataEntity._isActive))
            return DepositMetadataEntity._isActive;
        else
            return null;
    }
}
