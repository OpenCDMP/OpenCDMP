package org.opencdmp.data.tenant;


import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.tenant.TenantScoped;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public class TenantListener {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantListener.class));
	private final TenantScope tenantScope;

	private final ErrorThesaurusProperties errors;
	private final TenantEntityManager tenantEntityManager;


	@Autowired
	public TenantListener(
			TenantScope tenantScope, ErrorThesaurusProperties errors, TenantEntityManager tenantEntityManager
	) {
		this.tenantScope = tenantScope;
		this.errors = errors;
		this.tenantEntityManager = tenantEntityManager;
	}

	@PrePersist
	public void setTenantOnCreate(TenantScoped entity) throws InvalidApplicationException {
		if (this.tenantEntityManager.isTenantFiltersDisabled()) return;
		if (this.tenantScope.isMultitenant()) {
			if (entity.getTenantId() != null && (this.tenantScope.isDefaultTenant() || entity.getTenantId().compareTo(this.tenantScope.getTenant()) != 0)) {
				logger.error("somebody tried to set not login tenant");
				throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
			}
			if (!this.tenantScope.isDefaultTenant()) {
				final UUID tenantId = this.tenantScope.getTenant();
				entity.setTenantId(tenantId);
			}
		} else {
			entity.setTenantId(null);
		}
	}

	@PreUpdate
	@PreRemove
	public void setTenantOnUpdate(TenantScoped entity) throws InvalidApplicationException {
		if (this.tenantEntityManager.isTenantFiltersDisabled()) return;
		if (this.tenantScope.isMultitenant()) {
			if (!this.tenantScope.isDefaultTenant()) {
				if (entity.getTenantId() == null) {
					logger.error("somebody tried to set null tenant");
					throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
				}
				if (entity.getTenantId().compareTo(this.tenantScope.getTenant()) != 0) {
					logger.error("somebody tried to change an entries tenant");
					throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
				}

				final UUID tenantId = this.tenantScope.getTenant();
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

