package org.opencdmp.service.planblueprinttype;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintTypeEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.EventBroker;
import org.opencdmp.event.PlanBlueprintTypeTouchedEvent;
import org.opencdmp.model.PlanBlueprintType;
import org.opencdmp.model.builder.PlanBlueprintTypeBuilder;
import org.opencdmp.model.deleter.PlanBlueprintTypeDeleter;
import org.opencdmp.model.persist.PlanBlueprintTypePersist;
import org.opencdmp.query.PlanBlueprintTypeQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.usagelimit.UsageLimitServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PlanBlueprintTypeServiceImpl implements PlanBlueprintTypeService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintTypeServiceImpl.class));

    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private final ConventionService conventionService;

    private final ErrorThesaurusProperties errors;

    private final MessageSource messageSource;

    private final EventBroker eventBroker;

    private final UsageLimitServiceImpl usageLimitService;

    private final AccountingService accountingService;

    @Autowired
    public PlanBlueprintTypeServiceImpl(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            QueryFactory queryFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            EventBroker eventBroker, UsageLimitServiceImpl usageLimitService, AccountingService accountingService) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.eventBroker = eventBroker;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
    }

    public PlanBlueprintType persist(PlanBlueprintTypePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data planBlueprintType").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPlanBlueprintType);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanBlueprintTypeEntity data;
        if (isUpdate) {
            data = this.tenantEntityManagerFactory.getInstance().find(PlanBlueprintTypeEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanBlueprintType.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash()))
                throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.BLUEPRINT_TYPE_COUNT);
            data = new PlanBlueprintTypeEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setCode(model.getCode());
        data.setName(model.getName());
        data.setStatus(model.getStatus());
        data.setUpdatedAt(Instant.now());


        if (isUpdate) {
            this.tenantEntityManagerFactory.getInstance().merge(data);
        } else {
            this.tenantEntityManagerFactory.getInstance().persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_TYPE_COUNT.getValue());
        }

        this.tenantEntityManagerFactory.getInstance().flush();

        if (!isUpdate) {
            Long planBlueprintTypeCodes = this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .codes(model.getCode())
                    .count();

            if (planBlueprintTypeCodes > 0)
                throw new MyValidationException(this.errors.getPlanBlueprintTypeCodeExists().getCode(), this.errors.getPlanBlueprintTypeCodeExists().getMessage());
        }

        this.eventBroker.emit(new PlanBlueprintTypeTouchedEvent(data.getId()));
        return this.builderFactory.builder(PlanBlueprintTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, PlanBlueprintType._id), data);
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting planBlueprintType: {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePlanBlueprintType);

        this.deleterFactory.deleter(PlanBlueprintTypeDeleter.class).deleteAndSaveByIds(List.of(id));
    }

}

