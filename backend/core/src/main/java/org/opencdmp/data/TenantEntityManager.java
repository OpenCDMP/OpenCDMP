package org.opencdmp.data;

import gr.cite.tools.exception.MyForbiddenException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.tenant.TenantScoped;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;

@Service
@RequestScope
public class TenantEntityManager {
	@PersistenceContext
	private EntityManager entityManager;
	private final TenantScope tenantScope;
	private final ErrorThesaurusProperties errors;

	boolean tenantFiltersDisabled;

	public TenantEntityManager(TenantScope tenantScope, ErrorThesaurusProperties errors) {
		this.tenantScope = tenantScope;
		this.errors = errors;
		this.tenantFiltersDisabled = false;
	}


	public void persist(Object entity) {
		this.entityManager.persist(entity);
	}

	public <T> T merge(T entity) throws InvalidApplicationException {
		if (!this.tenantFiltersDisabled && this.tenantScope.isMultitenant() && (entity instanceof TenantScoped tenantScopedEntity)) {
			if (!this.tenantScope.isDefaultTenant()) {
				if (tenantScopedEntity.getTenantId() == null || !tenantScopedEntity.getTenantId().equals(this.tenantScope.getTenant())) throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
			} else if (tenantScopedEntity.getTenantId() != null) {
				throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
			}
		}
		return this.entityManager.merge(entity);
	}

	public void remove(Object entity) throws InvalidApplicationException {
		if (!this.tenantFiltersDisabled && this.tenantScope.isMultitenant() && (entity instanceof TenantScoped tenantScopedEntity)) {
			if (!this.tenantScope.isDefaultTenant()) {
				if (tenantScopedEntity.getTenantId() == null || !tenantScopedEntity.getTenantId().equals(this.tenantScope.getTenant())) throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
			} else if (tenantScopedEntity.getTenantId() != null) {
				throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
			}
		}
		this.entityManager.remove(entity);
	}

	public <T> T find(Class<T> entityClass, Object primaryKey) throws InvalidApplicationException {
		T entity = this.entityManager.find(entityClass, primaryKey);

		if (!this.tenantFiltersDisabled && this.tenantScope.isMultitenant() && (entity instanceof TenantScoped tenantScopedEntity)) {
			if (tenantScopedEntity.getTenantId() != null && !tenantScopedEntity.getTenantId().equals(this.tenantScope.getTenant())) return null;
		}
		return entity;
	}

	public <T> T find(Class<T> entityClass, Object primaryKey, boolean disableTracking) throws InvalidApplicationException {
		T entity = this.entityManager.find(entityClass, primaryKey);

		if (!this.tenantFiltersDisabled && this.tenantScope.isMultitenant() && (entity instanceof TenantScoped tenantScopedEntity)) {
			if (tenantScopedEntity.getTenantId() != null && !tenantScopedEntity.getTenantId().equals(this.tenantScope.getTenant())) return null;
		}
		if (disableTracking) this.entityManager.detach(entity);
		return entity;
	}

	public void flush() {
		this.entityManager.flush();
	}


	public void setFlushMode(FlushModeType flushMode) {
		this.entityManager.setFlushMode(flushMode);

	}

	public FlushModeType getFlushMode() {
		return this.entityManager.getFlushMode();
	}

	public void clear() {
		this.entityManager.clear();
	}

	public void reloadTenantFilters() throws InvalidApplicationException {
		if (!this.entityManager.isOpen()) {
			this.tenantFiltersDisabled = false;
			return;
		}
		this.disableTenantFilters();

		if (!this.tenantScope.isSet()) return;

		if (!this.tenantScope.isDefaultTenant()) {
			this.entityManager
					.unwrap(Session.class)
					.enableFilter(TenantScopedBaseEntity.TENANT_FILTER)
					.setParameter(TenantScopedBaseEntity.TENANT_FILTER_TENANT_PARAM, this.tenantScope.getTenant().toString());
		} else {
			this.entityManager
					.unwrap(Session.class)
					.enableFilter(TenantScopedBaseEntity.DEFAULT_TENANT_FILTER);
		}
		this.tenantFiltersDisabled = false;
	}

	public void loadExplicitTenantFilters() throws InvalidApplicationException {
		if (!this.entityManager.isOpen()) {
			this.tenantFiltersDisabled = false;
			return;
		}
		this.disableTenantFilters();

		if (!this.tenantScope.isSet()) return;

		if (!this.tenantScope.isDefaultTenant()) {
			this.entityManager
					.unwrap(Session.class)
					.enableFilter(TenantScopedBaseEntity.TENANT_FILTER_EXPLICT)
					.setParameter(TenantScopedBaseEntity.TENANT_FILTER_TENANT_PARAM, this.tenantScope.getTenant().toString());
		} else {
			this.entityManager
					.unwrap(Session.class)
					.enableFilter(TenantScopedBaseEntity.DEFAULT_TENANT_FILTER);
		}
		this.tenantFiltersDisabled = false;
	}

	public void disableTenantFilters() {
		if (!this.entityManager.isOpen()) {
			this.tenantFiltersDisabled = true;
			return;
		}
		this.entityManager
				.unwrap(Session.class)
				.disableFilter(TenantScopedBaseEntity.TENANT_FILTER);

		this.entityManager
				.unwrap(Session.class)
				.disableFilter(TenantScopedBaseEntity.DEFAULT_TENANT_FILTER);

		this.entityManager
				.unwrap(Session.class)
				.disableFilter(TenantScopedBaseEntity.TENANT_FILTER_EXPLICT);
		this.tenantFiltersDisabled = true;
	}

	public boolean isTenantFiltersDisabled() {
		return this.tenantFiltersDisabled;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
}

