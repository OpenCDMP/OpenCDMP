package org.opencdmp.service.maintenance;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.indicator.IndicatorFieldBaseType;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.outbox.accountingentrycreated.AccountingEntryCreatedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.annotationentityremoval.AnnotationEntityRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.annotationentitytouch.AnnotationEntityTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.indicator.*;
import org.opencdmp.integrationevent.outbox.indicatoraccess.FilterColumnConfig;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessConfig;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEvent;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEvent;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEvent;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEventHandler;
import org.opencdmp.integrationevent.outbox.tenantremoval.TenantRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.tenanttouched.TenantTouchedIntegrationEvent;
import org.opencdmp.integrationevent.outbox.tenanttouched.TenantTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.userremoval.UserRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.TenantUser;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingProperties;
import org.opencdmp.service.accounting.AccountingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(MaintenanceServiceImpl.class));
    private static final Logger log = LoggerFactory.getLogger(MaintenanceServiceImpl.class);
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final QueryFactory queryFactory;
    private final UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler;
    private final UserRemovalIntegrationEventHandler userRemovalIntegrationEventHandler;
    private final TenantTouchedIntegrationEventHandler tenantTouchedIntegrationEventHandler;
    private final TenantRemovalIntegrationEventHandler tenantRemovalIntegrationEventHandler;
    private final AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler;
    private final AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler;
    private final AccountingEntryCreatedIntegrationEventHandler accountingEntryCreatedIntegrationEventHandler;
    private final TenantScope tenantScope;
    private final TenantEntityManager tenantEntityManager;
    private final AccountingProperties accountingProperties;
    private final AccountingService accountingService;
    private final KpiProperties kpiProperties;
    private final IndicatorElasticEventHandler indicatorElasticEventHandler;
    private final IndicatorResetEventHandler indicatorResetEventHandler;
    private final IndicatorAccessEventHandler indicatorAccessEventHandler;
    private final IndicatorPointEventHandler indicatorPointEventHandler;
    private final ValidatorFactory validatorFactory;

    public MaintenanceServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService,
            QueryFactory queryFactory, UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler, UserRemovalIntegrationEventHandler userRemovalIntegrationEventHandler, TenantTouchedIntegrationEventHandler tenantTouchedIntegrationEventHandler, TenantRemovalIntegrationEventHandler tenantRemovalIntegrationEventHandler, AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler, AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler, AccountingEntryCreatedIntegrationEventHandler accountingEntryCreatedIntegrationEventHandler, TenantScope tenantScope, TenantEntityManager tenantEntityManager, AccountingProperties accountingProperties, AccountingService accountingService, KpiProperties kpiProperties, IndicatorElasticEventHandler indicatorElasticEventHandler, IndicatorResetEventHandler indicatorResetEventHandler, IndicatorAccessEventHandler indicatorAccessEventHandler, IndicatorPointEventHandler indicatorPointEventHandler, ValidatorFactory validatorFactory) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
	    this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
	    this.userRemovalIntegrationEventHandler = userRemovalIntegrationEventHandler;
	    this.tenantTouchedIntegrationEventHandler = tenantTouchedIntegrationEventHandler;
	    this.tenantRemovalIntegrationEventHandler = tenantRemovalIntegrationEventHandler;
	    this.annotationEntityRemovalIntegrationEventHandler = annotationEntityRemovalIntegrationEventHandler;
	    this.annotationEntityTouchedIntegrationEventHandler = annotationEntityTouchedIntegrationEventHandler;
        this.accountingEntryCreatedIntegrationEventHandler = accountingEntryCreatedIntegrationEventHandler;
        this.tenantScope = tenantScope;
        this.tenantEntityManager = tenantEntityManager;
        this.accountingProperties = accountingProperties;
        this.accountingService = accountingService;
        this.kpiProperties = kpiProperties;
        this.indicatorElasticEventHandler = indicatorElasticEventHandler;
        this.indicatorResetEventHandler = indicatorResetEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.indicatorPointEventHandler = indicatorPointEventHandler;
        this.validatorFactory = validatorFactory;
    }


    @Override
    public void sendUserTouchEvents() throws InvalidApplicationException {
        logger.debug("send user touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        List<UserEntity> activeUsers;
        List<UserEntity> inactiveUsers;
        try {
            this.entityManager.disableTenantFilters();
            UserQuery userQuery = this.queryFactory.query(UserQuery.class).disableTracking();
            userQuery.isActive(IsActive.Active);
            activeUsers = userQuery.collectAs(new BaseFieldSet().ensure(User._id));
            userQuery.isActive(IsActive.Inactive);
            inactiveUsers = userQuery.collectAs(new BaseFieldSet().ensure(User._id));
            this.entityManager.reloadTenantFilters();

        } finally {
            this.entityManager.reloadTenantFilters();
        }
        for(UserEntity user : activeUsers)
            this.userTouchedIntegrationEventHandler.handle(user.getId());

        for(UserEntity user : inactiveUsers)
            this.userRemovalIntegrationEventHandler.handle(user.getId());
    }
    
    @Override
    public void sendTenantTouchEvents() throws InvalidApplicationException {
        logger.debug("send tenant touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        List<TenantEntity> activeTenants;
        List<TenantEntity> inactiveTenants;
        try {
            this.entityManager.disableTenantFilters();
            TenantQuery tenantQuery = this.queryFactory.query(TenantQuery.class).disableTracking();
            tenantQuery.isActive(IsActive.Active);
            activeTenants = tenantQuery.collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            tenantQuery.isActive(IsActive.Inactive);
            inactiveTenants = tenantQuery.collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        for (TenantEntity tenant : activeTenants) {
            TenantTouchedIntegrationEvent event = new TenantTouchedIntegrationEvent();
            event.setId(tenant.getId());
            event.setCode(tenant.getCode());
            this.tenantTouchedIntegrationEventHandler.handle(event);
        }

        for (TenantEntity tenant : inactiveTenants) {
            this.tenantRemovalIntegrationEventHandler.handle(tenant.getId());
        }
    }
    
    @Override
    public void sendPlanTouchEvents() throws InvalidApplicationException {
        logger.debug("send user touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        List<TenantEntity> tenants = this.getTenants();

	    this.sendTenantPlanTouchEvents(null, this.tenantScope.getDefaultTenantCode());

        if (tenants != null){
            for (TenantEntity tenant : tenants){
                this.sendTenantPlanTouchEvents(tenant.getId(), tenant.getCode());
            }
        }
    }

    private void sendTenantPlanTouchEvents(UUID tenantId, String tenantCode) throws InvalidApplicationException {
        try {
            this.tenantScope.setTempTenant(this.entityManager, tenantId, tenantCode);
            List<PlanEntity> items = this.queryFactory.query(PlanQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._isActive));
            for (PlanEntity item : items) {
                if (item.getIsActive().equals(IsActive.Active)) this.annotationEntityTouchedIntegrationEventHandler.handlePlan(item.getId());
                else this.annotationEntityRemovalIntegrationEventHandler.handlePlan(item.getId());
            }
        } finally {
            this.tenantScope.removeTempTenant(this.entityManager);
        }
    }


    @Override
    public void sendDescriptionTouchEvents() throws InvalidApplicationException {
        logger.debug("send user touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        List<TenantEntity> tenants = this.getTenants();

        this.sendTenantDescriptionTouchEvents(null, this.tenantScope.getDefaultTenantCode());

        if (tenants != null){
            for (TenantEntity tenant : tenants){
                this.sendTenantDescriptionTouchEvents(tenant.getId(), tenant.getCode());
            }
        }
    }

    private void sendTenantDescriptionTouchEvents(UUID tenantId, String tenantCode) throws InvalidApplicationException {
        try {
            this.tenantScope.setTempTenant(this.entityManager, tenantId, tenantCode);
            List<DescriptionEntity> items = this.queryFactory.query(DescriptionQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._isActive));
            for (DescriptionEntity item : items) {
                if (item.getIsActive().equals(IsActive.Active)) this.annotationEntityTouchedIntegrationEventHandler.handleDescription(item.getId());
                else this.annotationEntityRemovalIntegrationEventHandler.handleDescription(item.getId());
            }
        } finally {
            this.tenantScope.removeTempTenant(this.entityManager);
        }
    }

    private List<TenantEntity> getTenants() throws InvalidApplicationException {
        List<TenantEntity> tenants;
        try {
            this.entityManager.disableTenantFilters();
            tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        return tenants;
    }

    @Override
    public void sendPlanAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PlanEntity> items = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._isActive).ensure(Plan._creator).ensure(Plan._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.PLAN_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.PLAN_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendBlueprintAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan blueprint accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._isActive).ensure(PlanBlueprint._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendDescriptionAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send descriptions entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<DescriptionEntity> items = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._isActive).ensure(Description._createdBy).ensure(Description._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendDescriptionTemplateAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description templates accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<DescriptionTemplateEntity> items = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._isActive).ensure(DescriptionTemplate._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendDescriptionTemplateTypeAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description templates types accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<DescriptionTemplateTypeEntity> items = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplateType._id).ensure(DescriptionTemplateType._isActive).ensure(DescriptionTemplateType._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendPrefillingSourceAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send prefilling sources accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PrefillingSourceEntity> items = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PrefillingSource._id).ensure(PrefillingSource._isActive).ensure(PrefillingSource._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.PREFILLING_SOURCES_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.PREFILLING_SOURCES_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendReferenceTypeAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send reference types accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<ReferenceTypeEntity> items = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceType._isActive).ensure(ReferenceType._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.REFERENCE_TYPE_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.REFERENCE_TYPE_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendUserAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send users accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<TenantUserEntity> items = this.queryFactory.query(TenantUserQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(TenantUserEntity._id).ensure(TenantUserEntity._userId).ensure(TenantUserEntity._isActive).ensure(TenantUserEntity._tenantId));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.USER_COUNT, items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.USER_COUNT, items.stream().filter(x -> x.getTenantId() == tenant.getId()).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    private void calculateReset(UsageLimitTargetMetric usageLimitTargetMetric, long itemsCount, UUID tenantId ,String tenantCode) throws InvalidApplicationException {
        try {
            this.tenantScope.setTempTenant(this.entityManager, tenantId, tenantCode);
            Integer currentValue = this.accountingService.getCurrentMetricValue(usageLimitTargetMetric, null, false);
            if (currentValue == 0) this.accountingService.set(usageLimitTargetMetric.getValue(), tenantId, tenantCode, (int) itemsCount);
            else if (currentValue < itemsCount) {
                if (currentValue < 0) {
                    this.accountingService.set(usageLimitTargetMetric.getValue(), tenantId, tenantCode, Math.abs(currentValue - (int) itemsCount));
                } else {
                    this.accountingService.set(usageLimitTargetMetric.getValue(), tenantId, tenantCode, currentValue - (int) itemsCount);
                }
            } else if (currentValue > itemsCount) {
                this.accountingService.set(usageLimitTargetMetric.getValue(), tenantId, tenantCode, (int) itemsCount - currentValue);
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorCreateEntryEvents() throws InvalidApplicationException {
        IndicatorElasticEvent event = new IndicatorElasticEvent();
        event.setId(this.kpiProperties.getId());
        event.setMetadata(this.defineMetadata());
        event.setSchema(this.defineSchema());
        this.validatorFactory.validator(IndicatorElasticEvent.IndicatorElasticEventValidator.class).validateForce(event);
        this.indicatorElasticEventHandler.handle(event);
    }

    @Override
    public void sendIndicatorResetEntryEvents() throws InvalidApplicationException {
        IndicatorResetEvent event = new IndicatorResetEvent();
        event.setId(this.kpiProperties.getId());
        event.setMetadata(this.defineMetadata());
        event.setSchema(this.defineSchema());
        this.validatorFactory.validator(IndicatorResetEvent.IndicatorResetEventValidator.class).validateForce(event);
        this.indicatorResetEventHandler.handle(event);
    }

    private IndicatorMetadata defineMetadata() {
        IndicatorMetadata metadata = new IndicatorMetadata();
        metadata.setCode(this.kpiProperties.getCode());
        metadata.setLabel(this.kpiProperties.getLabel());
        metadata.setDescription(this.kpiProperties.getDescription());
        metadata.setUrl(this.kpiProperties.getUrl());
        metadata.setDate(Instant.now());

        return metadata;
    }

    private IndicatorSchema defineSchema() {
        IndicatorSchema schema = new IndicatorSchema();
        schema.setId(this.kpiProperties.getId());

        List<IndicatorField> fields = new ArrayList<>();

        IndicatorField field = new IndicatorField();
        field.setCode("tenant_code");
        field.setName("Tenant Code");
        field.setBasetype(IndicatorFieldBaseType.Keyword);
        fields.add(field);

        IndicatorField field1 = new IndicatorField();
        field1.setCode("user_id");
        field1.setName("User Id");
        field1.setBasetype(IndicatorFieldBaseType.Keyword);
        fields.add(field1);

        IndicatorField field2 = new IndicatorField();
        field2.setCode("created_at");
        field2.setName("Created at");
        field2.setBasetype(IndicatorFieldBaseType.Date);
        fields.add(field2);

        IndicatorField field3 = new IndicatorField();
        field3.setCode("user_name");
        field3.setName("User Name");
        field3.setBasetype(IndicatorFieldBaseType.String);
        fields.add(field3);

        IndicatorField field4 = new IndicatorField();
        field4.setCode("value");
        field4.setName("Value");
        field4.setBasetype(IndicatorFieldBaseType.Integer);
        fields.add(field4);

        schema.setFields(fields);

        return schema;
    }

    @Override
    public void sendIndicatorAccessEntryEvents() throws InvalidApplicationException {

        try {
            List<String> allowedRoles = new ArrayList<>();
            allowedRoles.addAll(this.kpiProperties.getRoles());
            allowedRoles.addAll(this.kpiProperties.getTenantRoles());

            UserRoleQuery userRoleQuery = this.queryFactory.query(UserRoleQuery.class).disableTracking().roles(allowedRoles);
            this.tenantEntityManager.disableTenantFilters();
            List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).userRoleSubQuery(userRoleQuery).collectAs(new BaseFieldSet().ensure(User._id).ensure(User._name).ensure(User._createdAt));

            if (users != null && !users.isEmpty()) {
                for (UserEntity user: users) {
                    this.indicatorAccessEventHandler.handle(this.createIndicatorAccessEvent(user.getId(), this.tenantScope.getDefaultTenantCode()), null);
                }
                List<TenantUserEntity> tenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().userIds(users.stream().map(UserEntity::getId).distinct().toList()).isActive(IsActive.Active).collect();
                if (tenantUserEntities == null || tenantUserEntities.isEmpty()) return;
                List<TenantEntity> tenantEntities = this.queryFactory.query(TenantQuery.class).disableTracking().ids(tenantUserEntities.stream().map(TenantUserEntity::getTenantId).distinct().toList()).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
                if (tenantEntities != null && !tenantEntities.isEmpty()) {
                    for (TenantUserEntity user: tenantUserEntities) {
                        TenantEntity tenant = tenantEntities.stream().filter(x -> x.getId().equals(user.getTenantId())).findFirst().orElse(null);
                        if (tenant != null){
                            this.indicatorAccessEventHandler.handle(this.createIndicatorAccessEvent(user.getUserId(), tenant.getCode()), tenant.getId());
                        }
                    }
                }
            }

        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    private IndicatorAccessEvent createIndicatorAccessEvent(UUID userId, String tenantCode) {
        IndicatorAccessEvent event = new IndicatorAccessEvent();
        event.setIndicatorId(this.kpiProperties.getId());
        event.setUserId(userId);

        List<FilterColumnConfig> globalFilterColumns = new ArrayList<>();
        FilterColumnConfig filterColumn = new FilterColumnConfig();
        filterColumn.setColumn("tenant_code");
        filterColumn.setValues(List.of(tenantCode));
        globalFilterColumns.add(filterColumn);

        IndicatorAccessConfig config = new IndicatorAccessConfig();
        config.setGlobalFilterColumns(globalFilterColumns);

        event.setConfig(config);

        this.validatorFactory.validator(IndicatorAccessEvent.IndicatorAccessEventValidator.class).validateForce(event);
        return event;
    }

    @Override
    public void sendIndicatorPointEntryEvents() throws InvalidApplicationException {

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(User._id).ensure(User._name).ensure(User._createdAt));
            if (users == null || users.isEmpty()) {
                throw new MyApplicationException("users not found");
            }

            for (UserEntity user: users) {
                this.indicatorPointEventHandler.handle(this.createIndicatorPointEvent(user, user.getCreatedAt(), this.tenantScope.getDefaultTenantCode()));
            }
            List<TenantUserEntity> tenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().collect();
            if (tenantUserEntities != null && !tenantUserEntities.isEmpty()) {
                List<TenantEntity> tenantEntities = this.queryFactory.query(TenantQuery.class).disableTracking().ids(tenantUserEntities.stream().map(TenantUserEntity::getTenantId).distinct().toList()).collect();
                if (tenantEntities != null) {
                    for (TenantUserEntity tenantUser: tenantUserEntities) {
                        TenantEntity tenantEntity = tenantEntities.stream().filter(x -> x.getId().equals(tenantUser.getTenantId())).findFirst().orElse(null);
                        UserEntity userToAdd = users.stream().filter(x -> tenantUser.getUserId().equals(x.getId())).findFirst().orElse(null);
                        if (userToAdd != null && tenantEntity != null) {
                            this.indicatorPointEventHandler.handle(this.createIndicatorPointEvent(userToAdd, tenantUser.getCreatedAt(), tenantEntity.getCode()));
                        }
                    }
                }

            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    private IndicatorPointEvent createIndicatorPointEvent(UserEntity user, Instant createAt, String tenantCode) {
        IndicatorPointEvent event = new IndicatorPointEvent();
        event.setIndicatorId(this.kpiProperties.getId());

        event.add("user_id", user.getId());
        event.add("created_at", createAt);
        event.add("user_name", user.getName());
        event.add("value", 1);
        event.add("tenant_code", tenantCode);

        this.validatorFactory.validator(IndicatorPointEvent.IndicatorPointEventValidator.class).validateForce(event);
        return event;
    }
}
