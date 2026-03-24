package org.opencdmp.model.deleter;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.data.PlanReferenceEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.query.PlanReferenceQuery;
import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
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
public class PlanReferenceDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanReferenceDeleter.class));
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;

    private final KpiService kpiService;

    @Autowired
    public PlanReferenceDeleter(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            QueryFactory queryFactory,
            DeleterFactory deleterFactory, KpiService kpiService
    ) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.queryFactory = queryFactory;
        this.deleterFactory = deleterFactory;
        this.kpiService = kpiService;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<PlanReferenceEntity> data = this.queryFactory.query(PlanReferenceQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<PlanReferenceEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.tenantEntityManagerFactory.getInstance().flush();
        logger.trace("changes saved");
    }

    public void delete(List<PlanReferenceEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        for (PlanReferenceEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            logger.trace("updating item");
            item.setUpdatedAt(Instant.now());
            item.setIsActive(IsActive.Inactive);
            this.tenantEntityManagerFactory.getInstance().merge(item);
            logger.trace("updated item");
            this.kpiService.sendIndicatorPointReferenceEntry(KpiDirectionType.Decrease, item.getReferenceId());
        }
    }

}
