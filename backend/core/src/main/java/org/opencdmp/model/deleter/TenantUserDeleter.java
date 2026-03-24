package org.opencdmp.model.deleter;

import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.TenantUserEntity;
import org.opencdmp.event.EventBroker;
import org.opencdmp.event.UserRemovedFromTenantEvent;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.TenantUserQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.kpi.KpiService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantUserDeleter implements Deleter {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantUserDeleter.class));

	private final TenantEntityManagerFactory tenantEntityManagerFactory;
	private final QueryFactory queryFactory;
	private final EventBroker eventBroker;
	private final AccountingService accountingService;
	private final KpiService kpiService;
	private final TenantScopeFactory tenantScopeFactory;

	@Autowired
	public TenantUserDeleter(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            QueryFactory queryFactory, EventBroker eventBroker,
            AccountingService accountingService, KpiService kpiService, TenantScopeFactory tenantScopeFactory) {
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
		this.queryFactory = queryFactory;
		this.eventBroker = eventBroker;
		this.accountingService = accountingService;
        this.kpiService = kpiService;
        this.tenantScopeFactory = tenantScopeFactory;
    }

	public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
		logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(e -> e.size()).orElse(0)).And("ids", ids));
		List<TenantUserEntity> datas = this.queryFactory.query(TenantUserQuery.class).ids(ids).collect();
		logger.trace("retrieved {} items", Optional.ofNullable(datas).map(e -> e.size()).orElse(0));
		this.deleteAndSave(datas);
	}

	public void deleteAndSave(List<TenantUserEntity> datas) throws InvalidApplicationException {
		logger.debug("will delete {} items", Optional.ofNullable(datas).map(e -> e.size()).orElse(0));
		this.delete(datas);
		logger.trace("saving changes");
		this.tenantEntityManagerFactory.getInstance().flush();
		logger.trace("changes saved");
	}

	public void delete(List<TenantUserEntity> datas) throws InvalidApplicationException {
		logger.debug("will delete {} items", Optional.ofNullable(datas).map(x -> x.size()).orElse(0));
		if (datas == null || datas.isEmpty()) return;

		Instant now = Instant.now();

		List<TenantEntity> tenantEntities = new ArrayList<>();
		try {
			this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
			tenantEntities = this.queryFactory.query(TenantQuery.class).disableTracking().ids(datas.stream().map(TenantUserEntity::getTenantId).distinct().toList()).collect();
		} finally {
			this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
		}

		for (TenantUserEntity item : datas) {
			TenantEntity tenant = tenantEntities.stream().filter(x -> x.getId().equals(item.getTenantId())).findFirst().orElse(null);
			if (tenant != null) {
				try {
					this.tenantScopeFactory.getInstance().setTempTenant(this.tenantEntityManagerFactory.getInstance(), tenant.getId(), tenant.getCode());

					logger.trace("deleting item {}", item.getId());
					item.setIsActive(IsActive.Inactive);
					item.setUpdatedAt(now);
					logger.trace("updating item");
					this.tenantEntityManagerFactory.getInstance().merge(item);
					logger.trace("updated item");
					this.eventBroker.emit(new UserRemovedFromTenantEvent(item.getUserId(), item.getTenantId()));

					this.accountingService.decrease(UsageLimitTargetMetric.USER_COUNT.getValue());
					this.kpiService.sendIndicatorPointUserEntry(KpiDirectionType.Decrease);
				} finally {
					this.tenantScopeFactory.getInstance().removeTempTenant(this.tenantEntityManagerFactory.getInstance());
				}
			}

		}
	}
}
