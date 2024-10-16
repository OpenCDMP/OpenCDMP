package org.opencdmp.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.*;
import org.opencdmp.model.UsageLimit;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UsageLimitQuery extends QueryBase<UsageLimitEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<UsageLimitTargetMetric> usageLimitTargetMetrics;

    private Collection<UUID> excludedIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public UsageLimitQuery like(String value) {
        this.like = value;
        return this;
    }

    public UsageLimitQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public UsageLimitQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public UsageLimitQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public UsageLimitQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public UsageLimitQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public UsageLimitQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public UsageLimitQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public UsageLimitQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public UsageLimitQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public UsageLimitQuery usageLimitTargetMetrics(UsageLimitTargetMetric value) {
        this.usageLimitTargetMetrics = List.of(value);
        return this;
    }

    public UsageLimitQuery usageLimitTargetMetrics(UsageLimitTargetMetric... value) {
        this.usageLimitTargetMetrics = Arrays.asList(value);
        return this;
    }

    public UsageLimitQuery usageLimitTargetMetrics(Collection<UsageLimitTargetMetric> values) {
        this.usageLimitTargetMetrics = values;
        return this;
    }

    public UsageLimitQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public UsageLimitQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public UsageLimitQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public UsageLimitQuery(
		    UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<UsageLimitEntity> entityClass() {
        return UsageLimitEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.usageLimitTargetMetrics);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UsageLimitEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(UsageLimitEntity._label), this.like));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UsageLimitEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.usageLimitTargetMetrics != null) {
            CriteriaBuilder.In<UsageLimitTargetMetric> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UsageLimitEntity._targetMetric));
            for (UsageLimitTargetMetric item : this.usageLimitTargetMetrics)
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
    protected UsageLimitEntity convert(Tuple tuple, Set<String> columns) {
        UsageLimitEntity item = new UsageLimitEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._label, String.class));
        item.setTargetMetric(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._targetMetric, UsageLimitTargetMetric.class));
        item.setValue(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._value, Long.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._definition, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, UsageLimitEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(UsageLimit._id))
            return UsageLimitEntity._id;
        else if (item.match(UsageLimit._label))
            return UsageLimitEntity._label;
        else if (item.match(UsageLimit._targetMetric))
            return UsageLimitEntity._targetMetric;
        else if (item.match(UsageLimit._value))
            return UsageLimitEntity._value;
        else if (item.prefix(UsageLimit._definition))
            return UsageLimitEntity._definition;
        else if (item.match(UsageLimit._createdAt))
            return UsageLimitEntity._createdAt;
        else if (item.match(UsageLimit._updatedAt))
            return UsageLimitEntity._updatedAt;
        else if (item.match(UsageLimit._hash))
            return UsageLimitEntity._updatedAt;
        else if (item.match(UsageLimit._isActive))
            return UsageLimitEntity._isActive;
        else if (item.match(UsageLimit._belongsToCurrentTenant))
            return UsageLimitEntity._tenantId;
        else
            return null;
    }

}
