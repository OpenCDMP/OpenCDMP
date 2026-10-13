package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.commons.enums.MaintenanceTaskRunStatus;
import org.opencdmp.data.MaintenanceTaskRunEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MaintenanceTaskRunQuery extends QueryBase<MaintenanceTaskRunEntity> {

	private Collection<UUID> runs;
	private Collection<MaintenanceTaskRunStatus> statuses;

	private final TenantEntityManagerFactory tenantEntityManagerFactory;

	public MaintenanceTaskRunQuery(TenantEntityManagerFactory tenantEntityManagerFactory) {
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
	}

	public MaintenanceTaskRunQuery runs(UUID value) {
		this.runs = List.of(value);
		return this;
	}

	public MaintenanceTaskRunQuery runs(UUID... value) {
		this.runs = Arrays.asList(value);
		return this;
	}

	public MaintenanceTaskRunQuery runs(Collection<UUID> values) {
		this.runs = values;
		return this;
	}

	public MaintenanceTaskRunQuery statuses(MaintenanceTaskRunStatus value) {
		this.statuses = List.of(value);
		return this;
	}

	public MaintenanceTaskRunQuery statuses(MaintenanceTaskRunStatus... value) {
		this.statuses = Arrays.asList(value);
		return this;
	}

	public MaintenanceTaskRunQuery statuses(Collection<MaintenanceTaskRunStatus> values) {
		this.statuses = values;
		return this;
	}

	public MaintenanceTaskRunQuery enableTracking() {
		this.noTracking = false;
		return this;
	}

	public MaintenanceTaskRunQuery disableTracking() {
		this.noTracking = true;
		return this;
	}

	@Override
	protected EntityManager entityManager() {
		return this.tenantEntityManagerFactory.getInstance().getEntityManager();
	}

	@Override
	protected Class<MaintenanceTaskRunEntity> entityClass() {
		return MaintenanceTaskRunEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.runs) || this.isEmpty(this.statuses);
	}

	@Override
	protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
		List<Predicate> predicates = new ArrayList<>();
		if (this.runs != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(MaintenanceTaskRunEntity._run));
			for (UUID item : this.runs) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.statuses != null) {
			CriteriaBuilder.In<MaintenanceTaskRunStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(MaintenanceTaskRunEntity._status));
			for (MaintenanceTaskRunStatus item : this.statuses) inClause.value(item);
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
	protected MaintenanceTaskRunEntity convert(Tuple tuple, Set<String> columns) {
		MaintenanceTaskRunEntity item = new MaintenanceTaskRunEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._id, UUID.class));
		item.setRun(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._run, UUID.class));
		item.setTask(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._task, String.class));
		item.setOrdinal(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._ordinal, Integer.class));
		item.setStatus(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._status, MaintenanceTaskRunStatus.class));
		item.setStartedAt(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._startedAt, Instant.class));
		item.setFinishedAt(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._finishedAt, Instant.class));
		item.setError(QueryBase.convertSafe(tuple, columns, MaintenanceTaskRunEntity._error, String.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(MaintenanceTaskRunEntity._id)) return MaintenanceTaskRunEntity._id;
		else if (item.match(MaintenanceTaskRunEntity._run)) return MaintenanceTaskRunEntity._run;
		else if (item.match(MaintenanceTaskRunEntity._task)) return MaintenanceTaskRunEntity._task;
		else if (item.match(MaintenanceTaskRunEntity._ordinal)) return MaintenanceTaskRunEntity._ordinal;
		else if (item.match(MaintenanceTaskRunEntity._status)) return MaintenanceTaskRunEntity._status;
		else if (item.match(MaintenanceTaskRunEntity._startedAt)) return MaintenanceTaskRunEntity._startedAt;
		else if (item.match(MaintenanceTaskRunEntity._finishedAt)) return MaintenanceTaskRunEntity._finishedAt;
		else if (item.match(MaintenanceTaskRunEntity._error)) return MaintenanceTaskRunEntity._error;
		else return null;
	}
}
