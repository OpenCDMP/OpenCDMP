package org.opencdmp.service.descriptiontemplatetype;

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
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.DescriptionTemplateTypeTouchedEvent;
import org.opencdmp.event.EventBroker;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.builder.DescriptionTemplateTypeBuilder;
import org.opencdmp.model.deleter.DescriptionTemplateTypeDeleter;
import org.opencdmp.model.persist.DescriptionTemplateTypePersist;
import org.opencdmp.query.DescriptionTemplateTypeQuery;
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
public class DescriptionTemplateTypeServiceImpl implements DescriptionTemplateTypeService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionTemplateTypeServiceImpl.class));

    private final TenantEntityManager entityManager;

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
    public DescriptionTemplateTypeServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            QueryFactory queryFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            EventBroker eventBroker, UsageLimitServiceImpl usageLimitService, AccountingService accountingService) {
        this.entityManager = entityManager;
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

    public DescriptionTemplateType persist(DescriptionTemplateTypePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data descriptionTemplateType").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditDescriptionTemplateType);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        DescriptionTemplateTypeEntity data;
        if (isUpdate) {
            data = this.entityManager.find(DescriptionTemplateTypeEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionTemplateType.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash()))
                throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT);
            data = new DescriptionTemplateTypeEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setCode(model.getCode());
        data.setName(model.getName());
        data.setStatus(model.getStatus());
        data.setUpdatedAt(Instant.now());


        if (isUpdate) {
            this.entityManager.merge(data);
        } else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT.getValue());
        }

        this.entityManager.flush();

        if (!isUpdate) {
            Long descriptionTemplateTypeCodes = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .codes(model.getCode())
                    .count();

            if (descriptionTemplateTypeCodes > 0)
                throw new MyValidationException(this.errors.getDescriptionTemplateTypeCodeExists().getCode(), this.errors.getDescriptionTemplateTypeCodeExists().getMessage());
        }

        this.eventBroker.emit(new DescriptionTemplateTypeTouchedEvent(data.getId()));
        return this.builderFactory.builder(DescriptionTemplateTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, DescriptionTemplateType._id), data);
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting descriptionTemplateType: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteDescriptionTemplateType);

        this.deleterFactory.deleter(DescriptionTemplateTypeDeleter.class).deleteAndSaveByIds(List.of(id));
    }

}

