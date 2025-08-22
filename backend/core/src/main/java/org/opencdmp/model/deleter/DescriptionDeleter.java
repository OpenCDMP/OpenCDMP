package org.opencdmp.model.deleter;

import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.data.*;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.elastic.ElasticService;
import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionDeleter.class));

    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;
    protected final ElasticService elasticService;
    protected final AccountingService accountingService;
    
    @Autowired
    public DescriptionDeleter(
            TenantEntityManager entityManager,
            QueryFactory queryFactory,
            DeleterFactory deleterFactory,
            ElasticService elasticService,
            AccountingService accountingService) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
        this.deleterFactory = deleterFactory;
        this.elasticService = elasticService;
        this.accountingService = accountingService;
    }

    public void deleteAndSaveByIds(List<UUID> ids, boolean disableElastic) throws InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<DescriptionEntity> data = this.queryFactory.query(DescriptionQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data, disableElastic);
    }

    public void deleteAndSave(List<DescriptionEntity> data, boolean disableElastic) throws InvalidApplicationException, IOException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data, disableElastic);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<DescriptionEntity> data, boolean disableElastic) throws InvalidApplicationException, IOException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        Instant now = Instant.now();

        List<UUID> ids = data.stream().map(DescriptionEntity::getId).distinct().collect(Collectors.toList());
        {
            logger.debug("checking related - {}", DescriptionReferenceEntity.class.getSimpleName());
            List<DescriptionReferenceEntity> items = this.queryFactory.query(DescriptionReferenceQuery.class).descriptionIds(ids).collect();
            DescriptionReferenceDeleter deleter = this.deleterFactory.deleter(DescriptionReferenceDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", DescriptionTagEntity.class.getSimpleName());
            List<DescriptionTagEntity> items = this.queryFactory.query(DescriptionTagQuery.class).descriptionIds(ids).collect();
            DescriptionTagDeleter deleter = this.deleterFactory.deleter(DescriptionTagDeleter.class);
            deleter.delete(items);
        }

        List<DescriptionStatusEntity> descriptionStatusEntities = this.queryFactory.query(DescriptionStatusQuery.class).collectAs(new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._name));
        List<PlanEntity> planEntities = this.queryFactory.query(PlanQuery.class).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._accessType));

        for (DescriptionEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.entityManager.merge(item);
            logger.trace("updated item");

            if (!disableElastic) this.elasticService.deleteDescription(item);
            this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_COUNT.getValue());

            DescriptionStatusEntity descriptionStatusEntity = descriptionStatusEntities.stream().filter(x -> x.getId().equals(item.getStatusId())).findFirst().orElse(null);
            if (descriptionStatusEntity == null) continue;
            this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_BY_STATUS_COUNT.getValue().replace("{status_name}", descriptionStatusEntity.getName().toLowerCase()));
            if (descriptionStatusEntity.getInternalStatus() != null && descriptionStatusEntity.getInternalStatus().equals(org.opencdmp.commons.enums.DescriptionStatus.Finalized)) {
                PlanEntity planEntity = planEntities.stream().filter(x -> x.getId().equals(item.getId())).findFirst().orElse(null);
                if (planEntity != null && planEntity.getAccessType().equals(PlanAccessType.Public)) this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_PUBLISHED_COUNT.getValue());

            }
        }
    }

}
