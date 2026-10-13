package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.commons.enums.MaintenanceRunStatus;
import org.opencdmp.data.MaintenanceRunEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MaintenanceRunQuery extends QueryBase<MaintenanceRunEntity> {

	private Collection<UUID> ids;
	private Collection<MaintenanceRunStatus> statuses;
	private Instant heartbeatBefore;

	private final TenantEntityManagerFactory tenantEntityManagerFactory;

	public MaintenanceRunQuery(TenantEntityManagerFactory tenantEntityManagerFactory) {
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
	}

	public MaintenanceRunQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public MaintenanceRunQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public MaintenanceRunQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public MaintenanceRunQuery statuses(MaintenanceRunStatus value) {
		this.statuses = List.of(value);
		return this;
	}

	public MaintenanceRunQuery statuses(MaintenanceRunStatus... value) {
		this.statuses = Arrays.asList(value);
		return this;
	}

	public MaintenanceRunQuery statuses(Collection<MaintenanceRunStatus> values) {
		this.statuses = values;
		return this;
	}

	public MaintenanceRunQuery heartbeatBefore(Instant value) {
		this.heartbeatBefore = value;
		return this;
	}

	public MaintenanceRunQuery enableTracking() {
		this.noTracking = false;
		return this;
	}

	public MaintenanceRunQuery disableTracking() {
		this.noTracking = true;
		return this;
	}

	@Override
	protected EntityManager entityManager() {
		return this.tenantEntityManagerFactory.getInstance().getEntityManager();
	}

	@Override
	protected Class<MaintenanceRunEntity> entityClass() {
		return MaintenanceRunEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.statuses);
	}

	@Override
	protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
		List<Predicate> predicates = new ArrayList<>();
		if (this.ids != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(MaintenanceRunEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.statuses != null) {
			CriteriaBuilder.In<MaintenanceRunStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(MaintenanceRunEntity._status));
			for (MaintenanceRunStatus item : this.statuses) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.heartbeatBefore != null) {
			predicates.add(queryContext.CriteriaBuilder.lessThan(queryContext.Root.get(MaintenanceRunEntity._heartbeatAt), this.heartbeatBefore));
		}

		if (!predicates.isEmpty()) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return null;
		}
	}

	@Override
	protected MaintenanceRunEntity convert(Tuple tuple, Set<String> columns) {
		MaintenanceRunEntity item = new MaintenanceRunEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._id, UUID.class));
		item.setPreset(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._preset, String.class));
		item.setStatus(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._status, MaintenanceRunStatus.class));
		item.setStopOnFailure(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._stopOnFailure, Boolean.class));
		item.setStopRequested(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._stopRequested, Boolean.class));
		item.setCreatedBy(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._createdBy, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._createdAt, Instant.class));
		item.setFinishedAt(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._finishedAt, Instant.class));
		item.setHeartbeatAt(QueryBase.convertSafe(tuple, columns, MaintenanceRunEntity._heartbeatAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(MaintenanceRunEntity._id)) return MaintenanceRunEntity._id;
		else if (item.match(MaintenanceRunEntity._preset)) return MaintenanceRunEntity._preset;
		else if (item.match(MaintenanceRunEntity._status)) return MaintenanceRunEntity._status;
		else if (item.match(MaintenanceRunEntity._stopOnFailure)) return MaintenanceRunEntity._stopOnFailure;
		else if (item.match(MaintenanceRunEntity._stopRequested)) return MaintenanceRunEntity._stopRequested;
		else if (item.match(MaintenanceRunEntity._createdBy)) return MaintenanceRunEntity._createdBy;
		else if (item.match(MaintenanceRunEntity._createdAt)) return MaintenanceRunEntity._createdAt;
		else if (item.match(MaintenanceRunEntity._finishedAt)) return MaintenanceRunEntity._finishedAt;
		else if (item.match(MaintenanceRunEntity._heartbeatAt)) return MaintenanceRunEntity._heartbeatAt;
		else return null;
	}
}
