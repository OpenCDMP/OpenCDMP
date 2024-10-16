package org.opencdmp.model.deleter;

import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.data.*;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.elastic.ElasticService;
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

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanDeleter.class));

    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;
    protected final ElasticService elasticService;
    protected final AccountingService accountingService;

    @Autowired
    public PlanDeleter(
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
        List<PlanEntity> data = this.queryFactory.query(PlanQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data, disableElastic);
    }

    public void deleteAndSave(List<PlanEntity> data, boolean disableElastic) throws InvalidApplicationException, IOException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data, disableElastic);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<PlanEntity> data, boolean disableElastic) throws InvalidApplicationException, IOException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        List<UUID> ids = data.stream().map(PlanEntity::getId).distinct().toList();
        {
            logger.debug("checking related - {}", PlanUserEntity.class.getSimpleName());
            List<PlanUserEntity> items = this.queryFactory.query(PlanUserQuery.class).planIds(ids).collect();
            PlanUserDeleter deleter = this.deleterFactory.deleter(PlanUserDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", PlanDescriptionTemplate.class.getSimpleName());
            List<PlanDescriptionTemplateEntity> items = this.queryFactory.query(PlanDescriptionTemplateQuery.class).planIds(ids).collect();
            PlanDescriptionTemplateDeleter deleter = this.deleterFactory.deleter(PlanDescriptionTemplateDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", PlanReference.class.getSimpleName());
            List<PlanReferenceEntity> items = this.queryFactory.query(PlanReferenceQuery.class).planIds(ids).collect();
            PlanReferenceDeleter deleter = this.deleterFactory.deleter(PlanReferenceDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", Description.class.getSimpleName());
            List<DescriptionEntity> items = this.queryFactory.query(DescriptionQuery.class).planIds(ids).collect();
            DescriptionDeleter deleter = this.deleterFactory.deleter(DescriptionDeleter.class);
            deleter.delete(items, false); //We delete elastic entities by bmp deleter 
        }

        Instant now = Instant.now();

        for (PlanEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).types(EntityType.Plan).entityIds(item.getId());
            if (entityDoiQuery.count() > 0) throw new MyApplicationException("Plan is deposited can not deleted");
            if(item.getVersionStatus().equals(PlanVersionStatus.Current)) throw new MyApplicationException("Plan is current can not deleted");
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.entityManager.merge(item);
            logger.trace("updated item");

            if (!disableElastic) this.elasticService.deletePlan(item);
            this.accountingService.decrease(UsageLimitTargetMetric.PLAN_COUNT.getValue());
        }
    }

}
