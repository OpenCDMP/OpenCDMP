package org.opencdmp.service.usagelimit;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.types.usagelimit.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.UsageLimitEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.UsageLimit;
import org.opencdmp.model.builder.UsageLimitBuilder;
import org.opencdmp.model.deleter.UsageLimitDeleter;
import org.opencdmp.model.persist.UsageLimitPersist;
import org.opencdmp.model.persist.usagelimit.DefinitionPersist;
import org.opencdmp.query.UsageLimitQuery;
import org.opencdmp.service.accounting.AccountingProperties;
import org.opencdmp.service.accounting.AccountingService;
import org.slf4j.Logger;
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
public class UsageLimitServiceImpl implements UsageLimitService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UsageLimitServiceImpl.class));
    private static final Logger log = LoggerFactory.getLogger(UsageLimitServiceImpl.class);

    private final TenantEntityManager entityManager;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final ErrorThesaurusProperties errors;

    private final MessageSource messageSource;

    private final QueryFactory queryFactory;

    private final TenantEntityManager tenantEntityManager;

    private final XmlHandlingService xmlHandlingService;

    private final AccountingService accountingService;

    private final UsageLimitProperties usageLimitProperties;

    private final AccountingProperties accountingProperties;


    @Autowired
    public UsageLimitServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            QueryFactory queryFactory, TenantEntityManager tenantEntityManager, XmlHandlingService xmlHandlingService, AccountingService accountingService, UsageLimitProperties usageLimitProperties, AccountingProperties accountingProperties) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.queryFactory = queryFactory;
        this.tenantEntityManager = tenantEntityManager;
        this.xmlHandlingService = xmlHandlingService;
        this.accountingService = accountingService;
        this.usageLimitProperties = usageLimitProperties;
        this.accountingProperties = accountingProperties;
    }

    public UsageLimit persist(UsageLimitPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data UsageLimit").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditUsageLimit);

        List<UsageLimitEntity> existingUsageLimits;
        try {
            this.tenantEntityManager.loadExplicitTenantFilters();
            existingUsageLimits = this.queryFactory.query(UsageLimitQuery.class).disableTracking().isActive(IsActive.Active).collectAs((new BaseFieldSet().ensure(UsageLimit._label).ensure(UsageLimit._targetMetric).ensure(UsageLimit._value)));

        } catch (InvalidApplicationException e) {
            log.error(e.getMessage(), e);
            throw new MyApplicationException(e.getMessage());
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        UsageLimitEntity data;
        if (isUpdate) {
            data = this.entityManager.find(UsageLimitEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), UsageLimit.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!data.getTargetMetric().equals(model.getTargetMetric()) && existingUsageLimits != null && !existingUsageLimits.isEmpty() && existingUsageLimits.stream().filter(x -> x.getTargetMetric().equals(model.getTargetMetric())).findFirst().orElse(null) != null) throw new MyValidationException(this.errors.getUsageLimitMetricAlreadyExists().getCode(), this.errors.getUsageLimitMetricAlreadyExists().getMessage());
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            if (existingUsageLimits != null && !existingUsageLimits.isEmpty() && existingUsageLimits.stream().filter(x -> x.getTargetMetric().equals(model.getTargetMetric())).findFirst().orElse(null) != null) throw new MyValidationException(this.errors.getUsageLimitMetricAlreadyExists().getCode(), this.errors.getUsageLimitMetricAlreadyExists().getMessage());

            data = new UsageLimitEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setLabel(model.getLabel());
        data.setTargetMetric(model.getTargetMetric());
        data.setValue(model.getValue());
        data.setUpdatedAt(Instant.now());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition())));

        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();
        return this.builderFactory.builder(UsageLimitBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, UsageLimit._id), data);
    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist) {
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null)
            return data;

        data.setHasPeriodicity(persist.getHasPeriodicity());
        if (persist.getHasPeriodicity()) data.setPeriodicityRange(persist.getPeriodicityRange());

        return data;
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting UsageLimit: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteUsageLimit);

        this.deleterFactory.deleter(UsageLimitDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    public void checkIncrease(UsageLimitTargetMetric metric) throws InvalidApplicationException {

        if (!usageLimitProperties.getEnabled() || !accountingProperties.getEnabled()) return;

        if (metric == null) throw new MyApplicationException("Target Metric not defined");

        try {
            this.tenantEntityManager.loadExplicitTenantFilters();
            UsageLimitEntity usageLimitEntity = this.queryFactory.query(UsageLimitQuery.class).disableTracking().usageLimitTargetMetrics(metric).isActive(IsActive.Active).first();

            if (usageLimitEntity != null) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, usageLimitEntity.getDefinition());
                if (definition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{usageLimitEntity.getId(), DefinitionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                Integer currentValue = this.accountingService.getCurrentMetricValue(metric, definition);
                if (currentValue >= usageLimitEntity.getValue()) throw new MyValidationException(this.errors.getUsageLimitException().getCode(), usageLimitEntity.getLabel());
            }
        } catch (InvalidApplicationException e) {
            log.error(e.getMessage(), e);
            throw new MyApplicationException(e.getMessage());
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }
}

