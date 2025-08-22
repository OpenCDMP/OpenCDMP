package org.opencdmp.service.maintenance;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.SectionEntity;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.outbox.accountingentrycreated.AccountingEntryCreatedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.annotationentityremoval.AnnotationEntityRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.annotationentitytouch.AnnotationEntityTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.indicator.*;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEventHandler;
import org.opencdmp.integrationevent.outbox.tenantremoval.TenantRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.tenanttouched.TenantTouchedIntegrationEvent;
import org.opencdmp.integrationevent.outbox.tenanttouched.TenantTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.userremoval.UserRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.Language;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.evaluation.Evaluation;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingProperties;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.kpi.KpiProperties;
import org.opencdmp.service.kpi.KpiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
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
    private final TenantScope tenantScope;
    private final TenantEntityManager tenantEntityManager;
    private final AccountingService accountingService;
    private final IndicatorElasticEventHandler indicatorElasticEventHandler;
    private final IndicatorResetEventHandler indicatorResetEventHandler;
    private final KpiService kpiService;
    private final XmlHandlingService xmlHandlingService;

    public MaintenanceServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService,
            QueryFactory queryFactory, UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler, UserRemovalIntegrationEventHandler userRemovalIntegrationEventHandler, TenantTouchedIntegrationEventHandler tenantTouchedIntegrationEventHandler, TenantRemovalIntegrationEventHandler tenantRemovalIntegrationEventHandler, AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler, AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler, TenantScope tenantScope, TenantEntityManager tenantEntityManager, AccountingProperties accountingProperties, AccountingService accountingService, IndicatorElasticEventHandler indicatorElasticEventHandler, IndicatorResetEventHandler indicatorResetEventHandler, KpiService kpiService, XmlHandlingService xmlHandlingService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
	    this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
	    this.userRemovalIntegrationEventHandler = userRemovalIntegrationEventHandler;
	    this.tenantTouchedIntegrationEventHandler = tenantTouchedIntegrationEventHandler;
	    this.tenantRemovalIntegrationEventHandler = tenantRemovalIntegrationEventHandler;
	    this.annotationEntityRemovalIntegrationEventHandler = annotationEntityRemovalIntegrationEventHandler;
	    this.annotationEntityTouchedIntegrationEventHandler = annotationEntityTouchedIntegrationEventHandler;
        this.tenantScope = tenantScope;
        this.tenantEntityManager = tenantEntityManager;
        this.accountingService = accountingService;
        this.indicatorElasticEventHandler = indicatorElasticEventHandler;
        this.indicatorResetEventHandler = indicatorResetEventHandler;
        this.kpiService = kpiService;
        this.xmlHandlingService = xmlHandlingService;
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
            PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active);
            List<PlanEntity> items = query.collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._isActive).ensure(Plan._status).ensure(Plan._creator).ensure(Plan._accessType).ensure(Plan._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.PLAN_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.PLAN_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }

            // statuses
            List<PlanStatusEntity> planStatusEntities = this.queryFactory.query(PlanStatusQuery.class).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanStatus._id).ensure(PlanStatus._name).ensure(PlanStatus._internalStatus).ensure(PlanStatus._belongsToCurrentTenant));
            if (!planStatusEntities.isEmpty()) {
                for (PlanStatusEntity planStatusEntity: planStatusEntities) {
                    this.calculateReset(UsageLimitTargetMetric.PLAN_BY_STATUS_COUNT.getValue().replace("{status_name}", planStatusEntity.getName().toLowerCase()), items.stream().filter(x -> x.getTenantId() == null && x.getStatusId().equals(planStatusEntity.getId())).count(), null ,this.tenantScope.getDefaultTenantCode());
                    this.calculateReset(UsageLimitTargetMetric.PLAN_PUBLISHED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null && x.getStatusId().equals(planStatusEntity.getId()) && planStatusEntity.getInternalStatus() != null
                            && planStatusEntity.getInternalStatus().equals(org.opencdmp.commons.enums.PlanStatus.Finalized) && x.getAccessType().equals(PlanAccessType.Public)).count(), null ,this.tenantScope.getDefaultTenantCode());
                    for (TenantEntity tenant : tenants) {
                        this.calculateReset(UsageLimitTargetMetric.PLAN_BY_STATUS_COUNT.getValue().replace("{status_name}", planStatusEntity.getName().toLowerCase()), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getStatusId().equals(planStatusEntity.getId())).count(), tenant.getId(), tenant.getCode());
                        this.calculateReset(UsageLimitTargetMetric.PLAN_PUBLISHED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getStatusId().equals(planStatusEntity.getId()) && planStatusEntity.getInternalStatus() != null
                                && planStatusEntity.getInternalStatus().equals(org.opencdmp.commons.enums.PlanStatus.Finalized) && x.getAccessType().equals(PlanAccessType.Public)).count(), null ,this.tenantScope.getDefaultTenantCode());
                    }
                }
            }

            //entity dois
            EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().types(EntityType.Plan).isActive(IsActive.Active);
            query.entityDoiSubQuery(entityDoiQuery);
            query.setDistinct(true);

            items = query.collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._belongsToCurrentTenant));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.PLAN_DOIED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.PLAN_DOIED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
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
            List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._status).ensure(PlanBlueprint._isActive).ensure(PlanBlueprint._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null && x.getStatus().equals(PlanBlueprintStatus.Draft)).count(), null ,this.tenantScope.getDefaultTenantCode());
                this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null && x.getStatus().equals(PlanBlueprintStatus.Finalized)).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                    this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getStatus().equals(PlanBlueprintStatus.Draft)).count(), tenant.getId(), tenant.getCode());
                    this.calculateReset(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getStatus().equals(PlanBlueprintStatus.Finalized)).count(), tenant.getId(), tenant.getCode());
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
            DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active);
            List<DescriptionEntity> items = query.collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._isActive).ensure(Description._status).ensure(Description._createdBy).ensure(Description._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }

            DescriptionStatusQuery descriptionStatusQuery = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().isActive(IsActive.Active);
            List<DescriptionStatusEntity> descriptionStatusEntities = descriptionStatusQuery.collectAs(new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._name).ensure(DescriptionStatus._internalStatus).ensure(DescriptionStatus._belongsToCurrentTenant));
            if (!descriptionStatusEntities.isEmpty()) {
                for (DescriptionStatusEntity descriptionStatusEntity: descriptionStatusEntities) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_BY_STATUS_COUNT.getValue().replace("{status_name}", descriptionStatusEntity.getName().toLowerCase()), items.stream().filter(x -> x.getTenantId() == null && x.getStatusId().equals(descriptionStatusEntity.getId())).count(), null ,this.tenantScope.getDefaultTenantCode());
                    for (TenantEntity tenant : tenants) {
                        this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_BY_STATUS_COUNT.getValue().replace("{status_name}", descriptionStatusEntity.getName().toLowerCase()), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getStatusId().equals(descriptionStatusEntity.getId())).count(), tenant.getId(), tenant.getCode());
                    }
                }
            }

            // published descriptions
            descriptionStatusQuery.internalStatuses(org.opencdmp.commons.enums.DescriptionStatus.Finalized).isActive(IsActive.Active);
            query.disableTracking().descriptionStatusSubQuery(descriptionStatusQuery).isActive(IsActive.Active);

            PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(org.opencdmp.commons.enums.PlanStatus.Finalized).isActives(IsActive.Active);
            PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).disableTracking().planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public);
            query.planSubQuery(planQuery);
            items = query.collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._isActive).ensure(Description._belongsToCurrentTenant));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_PUBLISHED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_PUBLISHED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }

            //entity dois
            planQuery = this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active);
            EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).types(EntityType.Plan).isActive(IsActive.Active);
            planQuery.entityDoiSubQuery(entityDoiQuery);
            query.planSubQuery(planQuery);
            query.setDistinct(true);

            items = query.collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._isActive).ensure(Description._createdBy).ensure(Description._belongsToCurrentTenant));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_DOIED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_DOIED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
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
            List<DescriptionTemplateEntity> items = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._status).ensure(DescriptionTemplate._isActive).ensure(DescriptionTemplate._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null && x.getStatus().equals(DescriptionTemplateStatus.Draft)).count(), null ,this.tenantScope.getDefaultTenantCode());
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null && x.getStatus().equals(DescriptionTemplateStatus.Finalized)).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getStatus().equals(DescriptionTemplateStatus.Draft)).count(), tenant.getId(), tenant.getCode());
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getStatus().equals(DescriptionTemplateStatus.Finalized)).count(), tenant.getId(), tenant.getCode());
                }
            }

            PlanDescriptionTemplateQuery planDescriptionTemplateQuery = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active);
            planDescriptionTemplateQuery.setDistinct(true);

            List<PlanDescriptionTemplateEntity> planDescriptionTemplateEntities = planDescriptionTemplateQuery.collectAs(new BaseFieldSet().ensure(PlanDescriptionTemplate._descriptionTemplateGroupId).ensure(PlanDescriptionTemplate._belongsToCurrentTenant));
            if (!planDescriptionTemplateEntities.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_USED_COUNT.getValue(), planDescriptionTemplateEntities.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_USED_COUNT.getValue(), planDescriptionTemplateEntities.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
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
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
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
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.PREFILLING_SOURCES_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.PREFILLING_SOURCES_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
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
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.REFERENCE_TYPE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.REFERENCE_TYPE_COUNT.getValue(), items.stream().filter(x ->x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
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
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.USER_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.USER_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendPlanStatusAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan status accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PlanStatusEntity> items = this.queryFactory.query(PlanStatusQuery.class).disableTracking().isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanStatus._id).ensure(PlanStatus._isActive).ensure(PlanStatus._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.PLAN_STATUS_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.PLAN_STATUS_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendDescriptionStatusAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description status accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<DescriptionStatusEntity> items = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._isActive).ensure(DescriptionStatus._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_STATUS_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.DESCRIPTION_STATUS_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendEvaluationPlanAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send evaluation plan accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<EvaluationEntity> items = this.queryFactory.query(EvaluationQuery.class).disableTracking().isActive(IsActive.Active).entityTypes(EntityType.Plan).collectAs(new BaseFieldSet().ensure(Evaluation._id).ensure(Evaluation._isActive).ensure(Evaluation._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.EVALUATION_PLAN_EXECUTION_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.EVALUATION_PLAN_EXECUTION_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendEvaluationDescriptionAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send evaluation description accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<EvaluationEntity> items = this.queryFactory.query(EvaluationQuery.class).disableTracking().isActive(IsActive.Active).entityTypes(EntityType.Description).collectAs(new BaseFieldSet().ensure(Evaluation._id).ensure(Evaluation._isActive).ensure(Evaluation._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.EVALUATION_DESCRIPTION_EXECUTION_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.EVALUATION_DESCRIPTION_EXECUTION_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendReferenceAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send reference accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<ReferenceEntity> items = this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._type).ensure(Reference._isActive).ensure(Reference._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.REFERENCE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.REFERENCE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }

            List<ReferenceTypeEntity> referenceTypeEntities = this.queryFactory.query(ReferenceTypeQuery.class).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(ReferenceTypeEntity._id).ensure(ReferenceTypeEntity._code).ensure(Reference._belongsToCurrentTenant));
            if (!referenceTypeEntities.isEmpty()) {
                for (ReferenceTypeEntity referenceTypeEntity: referenceTypeEntities) {
                    this.calculateReset(UsageLimitTargetMetric.REFERENCE_BY_TYPE_COUNT.getValue().replace("{type_code}", referenceTypeEntity.getCode()), items.stream().filter(x -> x.getTenantId() == null && x.getTypeId().equals(referenceTypeEntity.getId())).count(), null ,this.tenantScope.getDefaultTenantCode());
                    for (TenantEntity tenant : tenants) {
                        this.calculateReset(UsageLimitTargetMetric.REFERENCE_BY_TYPE_COUNT.getValue().replace("{type_code}", referenceTypeEntity.getCode()), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getTypeId().equals(referenceTypeEntity.getId())).count(), tenant.getId(), tenant.getCode());
                    }
                }
            }

        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendLanguageAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send language accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<LanguageEntity> items = this.queryFactory.query(LanguageQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Language._id).ensure(Language._isActive).ensure(Language._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            if (!items.isEmpty()) {
                this.calculateReset(UsageLimitTargetMetric.LANGUAGE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() == null).count(), null ,this.tenantScope.getDefaultTenantCode());
                for (TenantEntity tenant : tenants) {
                    this.calculateReset(UsageLimitTargetMetric.LANGUAGE_COUNT.getValue(), items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).count(), tenant.getId(), tenant.getCode());
                }
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    private void calculateReset(String metric, long itemsCount, UUID tenantId ,String tenantCode) throws InvalidApplicationException {
        try {
            this.tenantScope.setTempTenant(this.entityManager, tenantId, tenantCode);
            Integer currentValue = this.accountingService.getCurrentMetricValue(metric, null);
            if (currentValue == 0) this.accountingService.set(metric, tenantId, tenantCode, (int) itemsCount);
            else if (currentValue < itemsCount) {
                if (currentValue < 0) {
                    this.accountingService.set(metric, tenantId, tenantCode, Math.abs(currentValue - (int) itemsCount));
                } else {
                    this.accountingService.set(metric, tenantId, tenantCode, currentValue - (int) itemsCount);
                }
            } else if (currentValue > itemsCount) {
                this.accountingService.set(metric, tenantId, tenantCode, (int) itemsCount - currentValue);
            }
        } finally {
            this.tenantScope.removeTempTenant(this.entityManager);
        }
    }

    @Override
    public void sendIndicatorCreateEntryEvents() throws InvalidApplicationException {
        this.indicatorElasticEventHandler.handle(this.kpiService.createIndicator());
    }

    @Override
    public void sendIndicatorResetEntryEvents() throws InvalidApplicationException {
        this.indicatorResetEventHandler.handle(this.kpiService.resetIndicator());
    }


    @Override
    public void sendIndicatorAccessEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorAccessEntryEvents();
    }

    @Override
    public void sendIndicatorPointPlanEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorPointPlanCountEntryEvents();
    }

    @Override
    public void sendIndicatorPointDescriptionEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorPointDescriptionCountEntryEvents();
    }

    @Override
    public void sendIndicatorPointReferenceEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorPointReferenceCountEntryEvents();
    }

    @Override
    public void sendIndicatorPointUserEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorPointUserCountEntryEvents();
    }

    @Override
    public void sendIndicatorPointPlanBlueprintEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorPointPlanBlueprintCountEntryEvents();
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorPointDescriptionTemplateCountEntryEvents();
    }

    @Override
    public void sendIndicatorPointTenantEntryEvents() throws InvalidApplicationException {
        this.kpiService.sendIndicatorPointTenantCountEntryEvents();
    }

    @Override
    public void setPlanBlueprintCanEditDescriptionTemplates() throws InvalidApplicationException {
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().collect();

            for (PlanBlueprintEntity entity: items) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, entity.getDefinition());
                if (definition != null) {
                    boolean iaCanEditDescriptionTemplatesUpdated = false;
                    for (SectionEntity section : definition.getSections()) {
                        if (section.getCanEditDescriptionTemplates() == null) {
                            section.setCanEditDescriptionTemplates(true);
                            iaCanEditDescriptionTemplatesUpdated = true;
                        }
                    }
                    if (iaCanEditDescriptionTemplatesUpdated) {
                        String newDefinition = this.xmlHandlingService.toXmlSafe(definition);
                        if (newDefinition != null) {
                            entity.setDefinition(newDefinition);
                            this.entityManager.merge(entity);
                        }
                    }
                }
            }
            this.entityManager.flush();
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }


}
