package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.data.InstallationEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InstallationQuery extends QueryBase<InstallationEntity> {

	private final TenantEntityManagerFactory tenantEntityManagerFactory;

	public InstallationQuery(TenantEntityManagerFactory tenantEntityManagerFactory) {
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
	}

	@Override
	protected EntityManager entityManager() {
		return this.tenantEntityManagerFactory.getInstance().getEntityManager();
	}

	@Override
	protected Class<InstallationEntity> entityClass() {
		return InstallationEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return false;
	}

	@Override
	protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
		return null;
	}

	@Override
	protected InstallationEntity convert(Tuple tuple, Set<String> columns) {
		InstallationEntity item = new InstallationEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, InstallationEntity._id, UUID.class));
		item.setShareUsage(QueryBase.convertSafe(tuple, columns, InstallationEntity._shareUsage, Boolean.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(InstallationEntity._id)) return InstallationEntity._id;
		else if (item.match(InstallationEntity._shareUsage)) return InstallationEntity._shareUsage;
		else return null;
	}
}
