package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.WebhookDeliveryStatus;
import org.opencdmp.commons.enums.WebhookEventType;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.WebhookDeliveryEntity;
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
public class WebhookDeliveryQuery extends QueryBase<WebhookDeliveryEntity> {

    private Collection<UUID> ids;

    private Collection<UUID> subscriptionIds;

    private Collection<WebhookDeliveryStatus> statuses;

    private Collection<WebhookEventType> eventTypes;

    private Collection<IsActive> isActives;

    private Instant dueBefore;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public WebhookDeliveryQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public WebhookDeliveryQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public WebhookDeliveryQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public WebhookDeliveryQuery subscriptionIds(UUID value) {
        this.subscriptionIds = List.of(value);
        return this;
    }

    public WebhookDeliveryQuery subscriptionIds(UUID... value) {
        this.subscriptionIds = Arrays.asList(value);
        return this;
    }

    public WebhookDeliveryQuery subscriptionIds(Collection<UUID> values) {
        this.subscriptionIds = values;
        return this;
    }

    public WebhookDeliveryQuery statuses(WebhookDeliveryStatus value) {
        this.statuses = List.of(value);
        return this;
    }

    public WebhookDeliveryQuery statuses(WebhookDeliveryStatus... value) {
        this.statuses = Arrays.asList(value);
        return this;
    }

    public WebhookDeliveryQuery statuses(Collection<WebhookDeliveryStatus> values) {
        this.statuses = values;
        return this;
    }

    public WebhookDeliveryQuery eventTypes(WebhookEventType value) {
        this.eventTypes = List.of(value);
        return this;
    }

    public WebhookDeliveryQuery eventTypes(Collection<WebhookEventType> values) {
        this.eventTypes = values;
        return this;
    }

    public WebhookDeliveryQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public WebhookDeliveryQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    /**
     * Rows the delivery worker should pick up: nextRetryAt is unset (fresh) or has
     * passed by the given moment.
     */
    public WebhookDeliveryQuery dueBefore(Instant value) {
        this.dueBefore = value;
        return this;
    }

    public WebhookDeliveryQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public WebhookDeliveryQuery ordering(Ordering ordering) {
        this.setOrder(ordering);
        return this;
    }

    public WebhookDeliveryQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public WebhookDeliveryQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    public WebhookDeliveryQuery(QueryUtilsService queryUtilsService, TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.queryUtilsService = queryUtilsService;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    protected EntityManager entityManager() {
        return this.tenantEntityManagerFactory.getInstance().getEntityManager();
    }

    @Override
    protected Class<WebhookDeliveryEntity> entityClass() {
        return WebhookDeliveryEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.subscriptionIds) || this.isEmpty(this.statuses) || this.isEmpty(this.eventTypes) || this.isEmpty(this.isActives);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        return this.queryUtilsService.buildTenantFilter(queryContext.CriteriaBuilder, queryContext.Root.get(WebhookDeliveryEntity._tenantId));
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookDeliveryEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.subscriptionIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookDeliveryEntity._subscriptionId));
            for (UUID item : this.subscriptionIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.statuses != null) {
            CriteriaBuilder.In<WebhookDeliveryStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookDeliveryEntity._status));
            for (WebhookDeliveryStatus item : this.statuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.eventTypes != null) {
            CriteriaBuilder.In<WebhookEventType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookDeliveryEntity._eventType));
            for (WebhookEventType item : this.eventTypes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(WebhookDeliveryEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.dueBefore != null) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    queryContext.CriteriaBuilder.isNull(queryContext.Root.get(WebhookDeliveryEntity._nextRetryAt)),
                    queryContext.CriteriaBuilder.lessThanOrEqualTo(queryContext.Root.get(WebhookDeliveryEntity._nextRetryAt), this.dueBefore)));
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected WebhookDeliveryEntity convert(Tuple tuple, Set<String> columns) {
        WebhookDeliveryEntity item = new WebhookDeliveryEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._tenantId, UUID.class));
        item.setSubscriptionId(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._subscriptionId, UUID.class));
        item.setEventType(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._eventType, WebhookEventType.class));
        item.setPayload(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._payload, String.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._status, WebhookDeliveryStatus.class));
        item.setAttempts(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._attempts, Integer.class));
        item.setLastHttpStatus(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._lastHttpStatus, Integer.class));
        item.setLastError(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._lastError, String.class));
        item.setNextRetryAt(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._nextRetryAt, Instant.class));
        item.setDeliveredAt(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._deliveredAt, Instant.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, WebhookDeliveryEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(WebhookDeliveryEntity._id))
            return WebhookDeliveryEntity._id;
        else if (item.match(WebhookDeliveryEntity._subscriptionId))
            return WebhookDeliveryEntity._subscriptionId;
        else if (item.match(WebhookDeliveryEntity._eventType))
            return WebhookDeliveryEntity._eventType;
        else if (item.match(WebhookDeliveryEntity._payload))
            return WebhookDeliveryEntity._payload;
        else if (item.match(WebhookDeliveryEntity._status))
            return WebhookDeliveryEntity._status;
        else if (item.match(WebhookDeliveryEntity._attempts))
            return WebhookDeliveryEntity._attempts;
        else if (item.match(WebhookDeliveryEntity._lastHttpStatus))
            return WebhookDeliveryEntity._lastHttpStatus;
        else if (item.match(WebhookDeliveryEntity._lastError))
            return WebhookDeliveryEntity._lastError;
        else if (item.match(WebhookDeliveryEntity._nextRetryAt))
            return WebhookDeliveryEntity._nextRetryAt;
        else if (item.match(WebhookDeliveryEntity._deliveredAt))
            return WebhookDeliveryEntity._deliveredAt;
        else if (item.match(WebhookDeliveryEntity._createdAt))
            return WebhookDeliveryEntity._createdAt;
        else if (item.match(WebhookDeliveryEntity._updatedAt))
            return WebhookDeliveryEntity._updatedAt;
        else if (item.match(WebhookDeliveryEntity._isActive))
            return WebhookDeliveryEntity._isActive;
        else
            return null;
    }
}
