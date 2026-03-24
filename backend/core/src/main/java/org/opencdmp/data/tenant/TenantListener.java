package org.opencdmp.data.tenant;


import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.tenant.TenantScoped;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public class TenantListener {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantListener.class));
	private final TenantScopeFactory tenantScopeFactory;

	private final ErrorThesaurusProperties errors;
	private final TenantEntityManagerFactory tenantEntityManagerFactory;


	@Autowired
	public TenantListener(
			TenantScopeFactory tenantScopeFactory, ErrorThesaurusProperties errors, TenantEntityManagerFactory tenantEntityManagerFactory
	) {
		this.tenantScopeFactory = tenantScopeFactory;
		this.errors = errors;
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
	}

	@PrePersist
	public void setTenantOnCreate(TenantScoped entity) throws InvalidApplicationException {
		if (this.tenantEntityManagerFactory.getInstance().isTenantFiltersDisabled()) return;
		if (this.tenantScopeFactory.getInstance().isMultitenant()) {
			if (entity.getTenantId() != null && (this.tenantScopeFactory.getInstance().isDefaultTenant() || entity.getTenantId().compareTo(this.tenantScopeFactory.getInstance().getTenant()) != 0)) {
				logger.error("somebody tried to set not login tenant");
				throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
			}
			if (!this.tenantScopeFactory.getInstance().isDefaultTenant()) {
				final UUID tenantId = this.tenantScopeFactory.getInstance().getTenant();
				entity.setTenantId(tenantId);
			}
		} else {
			entity.setTenantId(null);
		}
	}

	@PreUpdate
	@PreRemove
	public void setTenantOnUpdate(TenantScoped entity) throws InvalidApplicationException {
		if (this.tenantEntityManagerFactory.getInstance().isTenantFiltersDisabled()) return;
		if (this.tenantScopeFactory.getInstance().isMultitenant()) {
			if (!this.tenantScopeFactory.getInstance().isDefaultTenant()) {
				if (entity.getTenantId() == null) {
					logger.error("somebody tried to set null tenant");
					throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
				}
				if (entity.getTenantId().compareTo(this.tenantScopeFactory.getInstance().getTenant()) != 0) {
					logger.error("somebody tried to change an entries tenant");
					throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
				}

				final UUID tenantId = this.tenantScopeFactory.getInstance().getTenant();
				entity.setTenantId(tenantId);
			} else {
				if (entity.getTenantId() != null) {
					logger.error("somebody tried to set null tenant");
					throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
				}
			}
		} else {
			if (entity.getTenantId() != null) {
				logger.error("somebody tried to change an entries tenant");
				throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
			}
		}

	}
}

