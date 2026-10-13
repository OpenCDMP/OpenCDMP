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
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.WebhookSubscriptionEntity;
import org.opencdmp.model.webhook.WebhookSubscription;
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
public class WebhookSubscriptionQuery extends QueryBase<WebhookSubscriptionEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Boolean enabled;

    private TenantQuery tenantQuery;

    /** null: any row; false: only rows without a tenant (the default tenant's shared rows); true: only tenant-owned rows. */
    private Boolean tenantIsSet;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public WebhookSubscriptionQuery like(String value) {
        this.like = value;
        return this;
    }

    public WebhookSubscriptionQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public WebhookSubscriptionQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public WebhookSubscriptionQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public WebhookSubscriptionQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public WebhookSubscriptionQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public WebhookSubscriptionQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public WebhookSubscriptionQuery enabled(Boolean value) {
        this.enabled = value;
        return this;
    }

    public WebhookSubscriptionQuery tenantIsSet(Boolean value) {
        this.tenantIsSet = value;
        return this;
    }

    public WebhookSubscriptionQuery tenantSubQuery(TenantQuery tenantQuery) {
        this.tenantQuery = tenantQuery;
        return this;
    }

    public WebhookSubscriptionQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public WebhookSubscriptionQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public WebhookSubscriptionQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    public WebhookSubscriptionQuery(QueryUtilsService queryUtilsService, TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.queryUtilsService = queryUtilsService;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    protected EntityManager entityManager() {
        return this.tenantEntityManagerFactory.getInstance().getEntityManager();
    }

    @Override
    protected Class<WebhookSubscriptionEntity> entityClass() {
        return WebhookSubscriptionEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(WebhookSubscriptionEntity._label), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(WebhookSubscriptionEntity._url), this.like)
            ));
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookSubscriptionEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookSubscriptionEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.enabled != null) {
            predicates.add(queryContext.CriteriaBuilder.equal(queryContext.Root.get(WebhookSubscriptionEntity._enabled), this.enabled));
        }

        if (this.tenantIsSet != null) {
            predicates.add(this.tenantIsSet ? queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(WebhookSubscriptionEntity._tenantId)) : queryContext.CriteriaBuilder.isNull(queryContext.Root.get(WebhookSubscriptionEntity._tenantId)));
        }

        if (this.tenantQuery != null) {
            QueryContext<TenantEntity, UUID> subQuery = this.applySubQuery(this.tenantQuery, queryContext, UUID.class, tenantEntityRoot -> tenantEntityRoot.get(TenantEntity._id));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookSubscriptionEntity._tenantId)).value(subQuery.Query));
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected WebhookSubscriptionEntity convert(Tuple tuple, Set<String> columns) {
        WebhookSubscriptionEntity item = new WebhookSubscriptionEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._label, String.class));
        item.setUrl(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._url, String.class));
        item.setSecret(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._secret, String.class));
        item.setEventTypes(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._eventTypes, String.class));
        item.setEnabled(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._enabled, Boolean.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, WebhookSubscriptionEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(WebhookSubscriptionEntity._id))
            return WebhookSubscriptionEntity._id;
        else if (item.match(WebhookSubscriptionEntity._label))
            return WebhookSubscriptionEntity._label;
        else if (item.match(WebhookSubscriptionEntity._url))
            return WebhookSubscriptionEntity._url;
        else if (item.match(WebhookSubscriptionEntity._secret))
            return WebhookSubscriptionEntity._secret;
        else if (item.match(WebhookSubscriptionEntity._eventTypes))
            return WebhookSubscriptionEntity._eventTypes;
        else if (item.match(WebhookSubscriptionEntity._enabled))
            return WebhookSubscriptionEntity._enabled;
        else if (item.match(WebhookSubscriptionEntity._createdAt))
            return WebhookSubscriptionEntity._createdAt;
        else if (item.match(WebhookSubscriptionEntity._updatedAt))
            return WebhookSubscriptionEntity._updatedAt;
        else if (item.match(WebhookSubscriptionEntity._isActive))
            return WebhookSubscriptionEntity._isActive;
        else if (item.match(WebhookSubscription._belongsToCurrentTenant))
            return WebhookSubscriptionEntity._tenantId;
        else
            return null;
    }
}
