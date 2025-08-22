package org.opencdmp.model.deleter;

import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.data.*;
import org.opencdmp.query.DescriptionReferenceQuery;
import org.opencdmp.query.PlanReferenceQuery;
import org.opencdmp.query.ReferenceQuery;
import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.query.ReferenceTypeQuery;
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
public class ReferenceDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceDeleter.class));
    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;
    private final AccountingService accountingService;

    @Autowired
    public ReferenceDeleter(
            TenantEntityManager entityManager,
            QueryFactory queryFactory,
            DeleterFactory deleterFactory, AccountingService accountingService
    ) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
        this.deleterFactory = deleterFactory;
        this.accountingService = accountingService;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<ReferenceEntity> data = this.queryFactory.query(ReferenceQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<ReferenceEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<ReferenceEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        List<UUID> ids = data.stream().map(ReferenceEntity::getId).distinct().collect(Collectors.toList());
        {
            logger.debug("checking related - {}", DescriptionReferenceEntity.class.getSimpleName());
            List<DescriptionReferenceEntity> items = this.queryFactory.query(DescriptionReferenceQuery.class).referenceIds(ids).collect();
            DescriptionReferenceDeleter deleter = this.deleterFactory.deleter(DescriptionReferenceDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", PlanReferenceEntity.class.getSimpleName());
            List<PlanReferenceEntity> items = this.queryFactory.query(PlanReferenceQuery.class).referenceIds(ids).collect();
            PlanReferenceDeleter deleter = this.deleterFactory.deleter(PlanReferenceDeleter.class);
            deleter.delete(items);
        }
        Instant now = Instant.now();

        List<ReferenceTypeEntity> referenceTypeEntities = this.queryFactory.query(ReferenceTypeQuery.class).collectAs(new BaseFieldSet().ensure(ReferenceTypeEntity._id).ensure(ReferenceTypeEntity._code));
        for (ReferenceEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.entityManager.merge(item);
            logger.trace("updated item");

            this.accountingService.decrease(UsageLimitTargetMetric.REFERENCE_COUNT.getValue());
            ReferenceTypeEntity referenceTypeEntity = referenceTypeEntities.stream().filter(x -> x.getId().equals(item.getTypeId())).findFirst().orElse(null);
            if (referenceTypeEntity == null) continue;
            this.accountingService.decrease(UsageLimitTargetMetric.REFERENCE_BY_TYPE_COUNT.getValue().replace("{type_code}", referenceTypeEntity.getCode()));
        }
    }

}
