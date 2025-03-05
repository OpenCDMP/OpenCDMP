package org.opencdmp.model.deleter;

import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.data.DescriptionWorkflowEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.query.DescriptionWorkflowQuery;
import org.opencdmp.service.accounting.AccountingService;
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
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionWorkflowDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionWorkflowDeleter.class));

    private final TenantEntityManager entityManager;
    private final QueryFactory queryFactory;

    @Autowired
    public DescriptionWorkflowDeleter(TenantEntityManager entityManager, QueryFactory queryFactory){
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<DescriptionWorkflowEntity> data = this.queryFactory.query(DescriptionWorkflowQuery.class).ids(ids).collect();
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<DescriptionWorkflowEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<DescriptionWorkflowEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return;

        Instant now = Instant.now();
        for (DescriptionWorkflowEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            item.setUpdatedAt(now);
            item.setIsActive(IsActive.Inactive);
            logger.trace("updating item");
            this.entityManager.merge(item);
            logger.trace("updated item");
        }
    }
}
