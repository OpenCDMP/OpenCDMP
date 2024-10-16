package org.opencdmp.model.deleter;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.DescriptionTemplateTypeQuery;
import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
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
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateTypeDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionTemplateTypeDeleter.class));

    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;

    protected final AccountingService accountingService;

    @Autowired
    public DescriptionTemplateTypeDeleter(
            TenantEntityManager entityManager,
            QueryFactory queryFactory,
            DeleterFactory deleterFactory,
            AccountingService accountingService) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
        this.deleterFactory = deleterFactory;
        this.accountingService = accountingService;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<DescriptionTemplateTypeEntity> data = this.queryFactory.query(DescriptionTemplateTypeQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<DescriptionTemplateTypeEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<DescriptionTemplateTypeEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        List<UUID> ids = data.stream().map(DescriptionTemplateTypeEntity::getId).distinct().collect(Collectors.toList());
        {
            logger.debug("checking related - {}", DescriptionTemplateEntity.class.getSimpleName());
            List<DescriptionTemplateEntity> items = this.queryFactory.query(DescriptionTemplateQuery.class).typeIds(ids).collect();
            DescriptionTemplateDeleter deleter = this.deleterFactory.deleter(DescriptionTemplateDeleter.class);
            deleter.delete(items);
        }
        
        Instant now = Instant.now();

        for (DescriptionTemplateTypeEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.entityManager.merge(item);
            logger.trace("updated item");
            this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT.getValue());
        }
    }

}
