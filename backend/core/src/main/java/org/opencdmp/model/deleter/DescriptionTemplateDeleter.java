package org.opencdmp.model.deleter;

import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.UserDescriptionTemplateEntity;
import org.opencdmp.model.description.Description;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.UserDescriptionTemplateQuery;
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
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionTemplateDeleter.class));

    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;

    protected final AccountingService accountingService;

    @Autowired
    public DescriptionTemplateDeleter(
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
        List<DescriptionTemplateEntity> data = this.queryFactory.query(DescriptionTemplateQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<DescriptionTemplateEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<DescriptionTemplateEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        List<UUID> ids = data.stream().map(DescriptionTemplateEntity::getId).distinct().collect(Collectors.toList());
        List<UUID> groupIds = data.stream().map(DescriptionTemplateEntity::getGroupId).distinct().collect(Collectors.toList());
        {
            logger.debug("checking related - {}", UserDescriptionTemplateEntity.class.getSimpleName());
            List<UserDescriptionTemplateEntity> items = this.queryFactory.query(UserDescriptionTemplateQuery.class).descriptionTemplateIds(ids).collect();
            UserDescriptionTemplateDeleter deleter = this.deleterFactory.deleter(UserDescriptionTemplateDeleter.class);
            deleter.delete(items);
        }
        List<DescriptionEntity> activeDescriptions = this.queryFactory.query(DescriptionQuery.class).isActive(IsActive.Active).descriptionTemplateSubQuery(this.queryFactory.query(DescriptionTemplateQuery.class).ids(ids)).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._descriptionTemplate));
        
        Instant now = Instant.now();

        for (DescriptionTemplateEntity item : data) {
            if (activeDescriptions.stream().map(DescriptionEntity::getDescriptionTemplateId).anyMatch(x-> x.equals(item.getId()))) throw new MyApplicationException("template " + item.getCode() + " used by descriptions");
            logger.trace("deleting item {}", item.getId());
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.entityManager.merge(item);
            logger.trace("updated item");
            this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT.getValue());
            if (item.getStatus().equals(DescriptionTemplateStatus.Draft)) this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue());
            if (item.getStatus().equals(DescriptionTemplateStatus.Finalized)) this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue());
        }
    }

}
