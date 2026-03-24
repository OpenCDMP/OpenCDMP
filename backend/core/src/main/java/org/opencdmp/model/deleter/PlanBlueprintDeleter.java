package org.opencdmp.model.deleter;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.enums.kpi.KpiVersionType;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.query.PlanBlueprintQuery;
import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.kpi.KpiService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintDeleter.class));

    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;

    protected final AccountingService accountingService;

    protected final KpiService kpiService;

    @Autowired
    public PlanBlueprintDeleter(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            QueryFactory queryFactory,
            DeleterFactory deleterFactory,
            AccountingService accountingService, KpiService kpiService) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.queryFactory = queryFactory;
        this.deleterFactory = deleterFactory;
        this.accountingService = accountingService;
        this.kpiService = kpiService;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<PlanBlueprintEntity> data = this.queryFactory.query(PlanBlueprintQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<PlanBlueprintEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.tenantEntityManagerFactory.getInstance().flush();
        logger.trace("changes saved");
    }

    public void delete(List<PlanBlueprintEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        Instant now = Instant.now();

        for (PlanBlueprintEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.tenantEntityManagerFactory.getInstance().merge(item);
            logger.trace("updated item");
            this.accountingService.decrease(UsageLimitTargetMetric.BLUEPRINT_COUNT.getValue());
            if (item.getStatus().equals(PlanBlueprintStatus.Draft)) this.accountingService.decrease(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue());
            if (item.getStatus().equals(PlanBlueprintStatus.Finalized)) this.accountingService.decrease(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue());
            if (item.getVersionStatus().equals(PlanBlueprintVersionStatus.Current)) this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Decrease, KpiVersionType.LatestVersion);
            this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Decrease, KpiVersionType.TotalCount);
        }
    }

}
