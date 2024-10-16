package org.opencdmp.query;


import gr.cite.queueinbox.entity.QueueInboxStatus;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.QueueInboxEntity;
import org.opencdmp.data.TenantEntityManager;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QueueInboxQuery extends QueryBase<QueueInboxEntity> {

	private Collection<UUID> ids;
	private Collection<UUID> messageIds;
	private Instant createdAfter;
	private Collection<IsActive> isActives;
	private Collection<String> exchanges;
	private Collection<String> routes;
	private Collection<QueueInboxStatus> status;
	private Integer retryThreshold;
	
	private final TenantEntityManager tenantEntityManager;

	public QueueInboxQuery(TenantEntityManager tenantEntityManager) {
		this.tenantEntityManager = tenantEntityManager;
	}

	public QueueInboxQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public QueueInboxQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public QueueInboxQuery ids(List<UUID> value) {
		this.ids = value;
		return this;
	}

	public QueueInboxQuery messageIds(UUID value) {
		this.messageIds = List.of(value);
		return this;
	}

	public QueueInboxQuery messageIds(UUID... value) {
		this.messageIds = Arrays.asList(value);
		return this;
	}

	public QueueInboxQuery messageIds(List<UUID> value) {
		this.messageIds = value;
		return this;
	}

	public QueueInboxQuery isActives(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public QueueInboxQuery isActives(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public QueueInboxQuery isActives(List<IsActive> value) {
		this.isActives = value;
		return this;
	}

	public QueueInboxQuery exchanges(String value) {
		this.exchanges = List.of(value);
		return this;
	}

	public QueueInboxQuery exchanges(String... value) {
		this.exchanges = Arrays.asList(value);
		return this;
	}

	public QueueInboxQuery exchanges(List<String> value) {
		this.exchanges = value;
		return this;
	}

	public QueueInboxQuery routes(String value) {
		this.routes = List.of(value);
		return this;
	}

	public QueueInboxQuery routes(String... value) {
		this.routes = Arrays.asList(value);
		return this;
	}

	public QueueInboxQuery routes(List<String> value) {
		this.routes = value;
		return this;
	}

	public QueueInboxQuery status(QueueInboxStatus value) {
		this.status = List.of(value);
		return this;
	}

	public QueueInboxQuery status(QueueInboxStatus... value) {
		this.status = Arrays.asList(value);
		return this;
	}

	public QueueInboxQuery status(List<QueueInboxStatus> value) {
		this.status = value;
		return this;
	}

	public QueueInboxQuery createdAfter(Instant value) {
		this.createdAfter = value;
		return this;
	}

	public QueueInboxQuery retryThreshold(Integer value) {
		this.retryThreshold = value;
		return this;
	}

	public QueueInboxQuery ordering(Ordering ordering) {
		this.setOrder(ordering);
		return this;
	}

	public QueueInboxQuery enableTracking() {
		this.noTracking = false;
		return this;
	}

	public QueueInboxQuery disableTracking() {
		this.noTracking = true;
		return this;
	}

	@Override
	protected EntityManager entityManager(){
		return this.tenantEntityManager.getEntityManager();
	}

	@Override
	protected Class<QueueInboxEntity> entityClass() {
		return QueueInboxEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.messageIds) || this.isEmpty(this.isActives) || this.isEmpty(this.exchanges)
				|| this.isEmpty(this.routes) || this.isEmpty(this.status);
	}

	@Override
	protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
		List<Predicate> predicates = new ArrayList<>();
		if (this.ids != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueInboxEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.messageIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueInboxEntity._messageId));
			for (UUID item : this.messageIds) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.createdAfter != null) {
			predicates.add(queryContext.CriteriaBuilder.greaterThan(queryContext.Root.get(QueueInboxEntity._createdAt), this.createdAfter));
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueInboxEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.exchanges != null) {
			CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueInboxEntity._exchange));
			for (String item : this.exchanges) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.routes != null) {
			CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueInboxEntity._route));
			for (String item : this.routes) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.status != null) {
			CriteriaBuilder.In<QueueInboxStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueInboxEntity._status));
			for (QueueInboxStatus item : this.status) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.retryThreshold != null) {
			predicates.add(queryContext.CriteriaBuilder.or(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(QueueInboxEntity._retryCount)),
					queryContext.CriteriaBuilder.lessThanOrEqualTo(queryContext.Root.get(QueueInboxEntity._retryCount), this.retryThreshold)));
		}

		if (!predicates.isEmpty()) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return null;
		}
	}

	@Override
	protected QueueInboxEntity convert(Tuple tuple, Set<String> columns) {
		QueueInboxEntity item = new QueueInboxEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._id, UUID.class));
		item.setExchange(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._exchange, String.class));
		item.setTenantId(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._tenantId, UUID.class));
		item.setRoute(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._route, String.class));
		item.setMessage(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._message, String.class));
		item.setMessageId(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._messageId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._createdAt, Instant.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._isActive, IsActive.class));
		item.setStatus(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._status, QueueInboxStatus.class));
		item.setRetryCount(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._retryCount, Integer.class));
		item.setQueue(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._queue, String.class));
		item.setApplicationId(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._applicationId, String.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, QueueInboxEntity._updatedAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(QueueInboxEntity._id)) return QueueInboxEntity._id;
		else if (item.match(QueueInboxEntity._exchange)) return QueueInboxEntity._exchange;
		else if (item.match(QueueInboxEntity._tenantId)) return QueueInboxEntity._tenantId;
		else if (item.match(QueueInboxEntity._route)) return QueueInboxEntity._route;
		else if (item.match(QueueInboxEntity._message)) return QueueInboxEntity._message;
		else if (item.match(QueueInboxEntity._messageId)) return QueueInboxEntity._messageId;
		else if (item.match(QueueInboxEntity._createdAt)) return QueueInboxEntity._createdAt;
		else if (item.match(QueueInboxEntity._isActive)) return QueueInboxEntity._isActive;
		else if (item.match(QueueInboxEntity._status)) return QueueInboxEntity._status;
		else if (item.match(QueueInboxEntity._retryCount)) return QueueInboxEntity._retryCount;
		else if (item.match(QueueInboxEntity._queue)) return QueueInboxEntity._queue;
		else if (item.match(QueueInboxEntity._applicationId)) return QueueInboxEntity._applicationId;
		else if (item.match(QueueInboxEntity._createdAt)) return QueueInboxEntity._createdAt;
		else if (item.match(QueueInboxEntity._updatedAt)) return QueueInboxEntity._updatedAt;
		else return null;
	}
}
