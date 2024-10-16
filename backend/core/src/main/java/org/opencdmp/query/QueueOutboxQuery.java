package org.opencdmp.query;


import gr.cite.queueoutbox.entity.QueueOutboxNotifyStatus;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.QueueOutboxEntity;
import org.opencdmp.data.TenantEntityManager;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QueueOutboxQuery extends QueryBase<QueueOutboxEntity> {

	private Collection<UUID> ids;
	private Instant createdAfter;
	private Collection<IsActive> isActives;
	private Collection<String> exchanges;
	private Collection<String> routes;
	private Collection<QueueOutboxNotifyStatus> notifyStatus;
	private Integer retryThreshold;
	private Integer confirmTimeout;

	private final TenantEntityManager tenantEntityManager;

	public QueueOutboxQuery(TenantEntityManager tenantEntityManager) {
		this.tenantEntityManager = tenantEntityManager;
	}

	public QueueOutboxQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public QueueOutboxQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public QueueOutboxQuery ids(List<UUID> value) {
		this.ids = value;
		return this;
	}

	public QueueOutboxQuery isActives(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public QueueOutboxQuery isActives(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public QueueOutboxQuery isActives(List<IsActive> value) {
		this.isActives = value;
		return this;
	}

	public QueueOutboxQuery exchanges(String value) {
		this.exchanges = List.of(value);
		return this;
	}

	public QueueOutboxQuery exchanges(String... value) {
		this.exchanges = Arrays.asList(value);
		return this;
	}

	public QueueOutboxQuery exchanges(List<String> value) {
		this.exchanges = value;
		return this;
	}

	public QueueOutboxQuery routes(String value) {
		this.routes = List.of(value);
		return this;
	}

	public QueueOutboxQuery routes(String... value) {
		this.routes = Arrays.asList(value);
		return this;
	}

	public QueueOutboxQuery routes(List<String> value) {
		this.routes = value;
		return this;
	}

	public QueueOutboxQuery notifyStatus(QueueOutboxNotifyStatus value) {
		this.notifyStatus = List.of(value);
		return this;
	}

	public QueueOutboxQuery notifyStatus(QueueOutboxNotifyStatus... value) {
		this.notifyStatus = Arrays.asList(value);
		return this;
	}

	public QueueOutboxQuery notifyStatus(List<QueueOutboxNotifyStatus> value) {
		this.notifyStatus = value;
		return this;
	}

	public QueueOutboxQuery createdAfter(Instant value) {
		this.createdAfter = value;
		return this;
	}

	public QueueOutboxQuery retryThreshold(Integer value) {
		this.retryThreshold = value;
		return this;
	}

	public QueueOutboxQuery confirmTimeout(Integer value) {
		this.confirmTimeout = value;
		return this;
	}

	public QueueOutboxQuery ordering(Ordering ordering) {
		this.setOrder(ordering);
		return this;
	}

	public QueueOutboxQuery enableTracking() {
		this.noTracking = false;
		return this;
	}

	public QueueOutboxQuery disableTracking() {
		this.noTracking = true;
		return this;
	}

	@Override
	protected EntityManager entityManager(){
		return this.tenantEntityManager.getEntityManager();
	}

	@Override
	protected Class<QueueOutboxEntity> entityClass() {
		return QueueOutboxEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.exchanges)
				|| this.isEmpty(this.routes) || this.isEmpty(this.notifyStatus);
	}

	@Override
	protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
		List<Predicate> predicates = new ArrayList<>();
		if (this.ids != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueOutboxEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.createdAfter != null) {
			predicates.add(queryContext.CriteriaBuilder.greaterThan(queryContext.Root.get(QueueOutboxEntity._createdAt), this.createdAfter));
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueOutboxEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.exchanges != null) {
			CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueOutboxEntity._exchange));
			for (String item : this.exchanges) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.routes != null) {
			CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueOutboxEntity._route));
			for (String item : this.routes) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.notifyStatus != null) {
			CriteriaBuilder.In<QueueOutboxNotifyStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(QueueOutboxEntity._notifyStatus));
			for (QueueOutboxNotifyStatus item : this.notifyStatus) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.retryThreshold != null) {
			predicates.add(queryContext.CriteriaBuilder.or(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(QueueOutboxEntity._retryCount)),
					queryContext.CriteriaBuilder.lessThanOrEqualTo(queryContext.Root.get(QueueOutboxEntity._retryCount), this.retryThreshold)));
		}

		if (this.confirmTimeout != null) {
			predicates.add(queryContext.CriteriaBuilder.or(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(QueueOutboxEntity._publishedAt)),
					queryContext.CriteriaBuilder.and(
							queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(QueueOutboxEntity._publishedAt)),
							queryContext.CriteriaBuilder.isNull(queryContext.Root.get(QueueOutboxEntity._confirmedAt)),
							queryContext.CriteriaBuilder.lessThan(queryContext.Root.get(QueueOutboxEntity._publishedAt), Instant.now().minusSeconds(this.confirmTimeout))
					)
			));
		}


		if (predicates.size() > 0) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return null;
		}
	}

	@Override
	protected QueueOutboxEntity convert(Tuple tuple, Set<String> columns) {
		QueueOutboxEntity item = new QueueOutboxEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._id, UUID.class));
		item.setExchange(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._exchange, String.class));
		item.setTenantId(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._tenantId, UUID.class));
		item.setRoute(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._route, String.class));
		item.setMessage(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._message, String.class));
		item.setMessageId(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._messageId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._createdAt, Instant.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._isActive, IsActive.class));
		item.setNotifyStatus(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._notifyStatus, QueueOutboxNotifyStatus.class));
		item.setRetryCount(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._retryCount, Integer.class));
		item.setPublishedAt(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._publishedAt, Instant.class));
		item.setConfirmedAt(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._confirmedAt, Instant.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, QueueOutboxEntity._updatedAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(QueueOutboxEntity._id)) return QueueOutboxEntity._id;
		else if (item.match(QueueOutboxEntity._exchange)) return QueueOutboxEntity._exchange;
		else if (item.match(QueueOutboxEntity._tenantId)) return QueueOutboxEntity._tenantId;
		else if (item.match(QueueOutboxEntity._route)) return QueueOutboxEntity._route;
		else if (item.match(QueueOutboxEntity._message)) return QueueOutboxEntity._message;
		else if (item.match(QueueOutboxEntity._messageId)) return QueueOutboxEntity._messageId;
		else if (item.match(QueueOutboxEntity._createdAt)) return QueueOutboxEntity._createdAt;
		else if (item.match(QueueOutboxEntity._isActive)) return QueueOutboxEntity._isActive;
		else if (item.match(QueueOutboxEntity._notifyStatus)) return QueueOutboxEntity._notifyStatus;
		else if (item.match(QueueOutboxEntity._retryCount)) return QueueOutboxEntity._retryCount;
		else if (item.match(QueueOutboxEntity._publishedAt)) return QueueOutboxEntity._publishedAt;
		else if (item.match(QueueOutboxEntity._confirmedAt)) return QueueOutboxEntity._confirmedAt;
		else if (item.match(QueueOutboxEntity._createdAt)) return QueueOutboxEntity._createdAt;
		else if (item.match(QueueOutboxEntity._updatedAt)) return QueueOutboxEntity._updatedAt;
		else return null;
	}
}
