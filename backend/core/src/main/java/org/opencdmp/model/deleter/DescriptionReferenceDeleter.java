package org.opencdmp.model.deleter;

import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.DescriptionReferenceEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.model.description.Description;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.DescriptionReferenceQuery;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionReferenceDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionReferenceDeleter.class));

    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;

    private final KpiService kpiService;

    @Autowired
    public DescriptionReferenceDeleter(
            TenantEntityManagerFactory entityManager,
            QueryFactory queryFactory,
            DeleterFactory deleterFactory, KpiService kpiService
    ) {
        this.tenantEntityManagerFactory = entityManager;
        this.queryFactory = queryFactory;
        this.deleterFactory = deleterFactory;
        this.kpiService = kpiService;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<DescriptionReferenceEntity> data = this.queryFactory.query(DescriptionReferenceQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<DescriptionReferenceEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.tenantEntityManagerFactory.getInstance().flush();
        logger.trace("changes saved");
    }

    public void delete(List<DescriptionReferenceEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        Instant now = Instant.now();

        // Resolve description -> plan in one query so each KPI entry carries its plan_id (matches the bulk-resync emission, which the dashboards' "References per Plan" chart aggregates by).
        Set<UUID> descriptionIds = data.stream().map(DescriptionReferenceEntity::getDescriptionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, UUID> descriptionIdToPlanId = descriptionIds.isEmpty()
                ? Map.of()
                : this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionIds).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._plan)).stream()
                        .filter(d -> d.getPlanId() != null)
                        .collect(Collectors.toMap(DescriptionEntity::getId, DescriptionEntity::getPlanId, (a, b) -> a));

        // Collect KPI entries up-front and emit them in one batched outbox event after the merges, instead of per-item lookups + publishes inside the loop.
        List<KpiService.ReferenceIndicatorEntry> kpiEntries = new ArrayList<>(data.size());
        for (DescriptionReferenceEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.tenantEntityManagerFactory.getInstance().merge(item);
            logger.trace("updated item");
            if (item.getReferenceId() != null) kpiEntries.add(new KpiService.ReferenceIndicatorEntry(item.getReferenceId(), descriptionIdToPlanId.get(item.getDescriptionId())));
        }
        this.kpiService.sendIndicatorPointReferenceBatchEntry(KpiDirectionType.Decrease, kpiEntries);
    }

}
