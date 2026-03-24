package org.opencdmp.service.kpi;


import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.enums.kpi.KpiVersionType;
import org.opencdmp.commons.enums.kpi.KpiEntityType;
import org.opencdmp.commons.enums.kpi.MetricType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.outbox.indicator.*;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEvent;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEvent;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.query.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KpiServiceImpl implements KpiService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(KpiServiceImpl.class));
    private final KpiProperties kpiProperties;
    private final ValidatorFactory validatorFactory;
    private final ConventionService conventionService;
    private final IndicatorPointEventHandler indicatorPointEventHandler;
    private final IndicatorAccessEventHandler indicatorAccessEventHandler;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final QueryFactory queryFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final MessageSource messageSource;

    @Autowired
    public KpiServiceImpl(
            KpiProperties kpiProperties, ValidatorFactory validatorFactory, ConventionService conventionService, IndicatorPointEventHandler indicatorPointEventHandler, IndicatorAccessEventHandler indicatorAccessEventHandler, TenantEntityManagerFactory tenantEntityManagerFactory, QueryFactory queryFactory, TenantScopeFactory tenantScopeFactory, MessageSource messageSource) {
        this.kpiProperties = kpiProperties;
        this.validatorFactory = validatorFactory;
        this.conventionService = conventionService;
        this.indicatorPointEventHandler = indicatorPointEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.queryFactory = queryFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.messageSource = messageSource;
    }


    @Override
    public IndicatorElasticEvent createIndicator() {
        IndicatorElasticEvent event = new IndicatorElasticEvent();

        if (this.kpiProperties.getIndicator() != null) {
            event.setId(this.kpiProperties.getIndicator().getId());
            event.setMetadata(this.defineMetadata());
            event.setSchema(this.defineSchema());

            FilterColumnConfig filterColumnConfig = new FilterColumnConfig();
            filterColumnConfig.setCode(KpiSchemaFieldCodes.TenantCode);

            AccessRequestConfig accessRequestConfig = new AccessRequestConfig();
            accessRequestConfig.setFilterColumns(List.of(filterColumnConfig));

            IndicatorConfig config = new IndicatorConfig();
            config.setAccessRequestConfig(accessRequestConfig);

            event.setConfig(config);
        }

        this.validatorFactory.validator(IndicatorElasticEvent.IndicatorElasticEventValidator.class).validateForce(event);
        return event;
    }

    @Override
    public IndicatorResetEvent resetIndicator() {
        IndicatorResetEvent event = new IndicatorResetEvent();

        if (this.kpiProperties.getIndicator() != null) {
            event.setId(this.kpiProperties.getIndicator().getId());
            event.setMetadata(this.defineMetadata());
            event.setSchema(this.defineSchema());
        }

        this.validatorFactory.validator(IndicatorResetEvent.IndicatorResetEventValidator.class).validateForce(event);
        return event;
    }

    private IndicatorMetadata defineMetadata() {
        IndicatorMetadata metadata = new IndicatorMetadata();
        if (this.kpiProperties.getIndicator() == null || this.kpiProperties.getIndicator().getMetadata() == null) return metadata;

        metadata.setCode(this.kpiProperties.getIndicator().getMetadata().getCode());
        metadata.setLabel(this.kpiProperties.getIndicator().getMetadata().getLabel());
        metadata.setDate(Instant.now());

        return metadata;
    }

    private IndicatorSchema defineSchema() {
        IndicatorSchema schema = new IndicatorSchema();
        if (this.kpiProperties.getIndicator() == null || this.kpiProperties.getIndicator().getSchema() == null) return schema;

        schema.setId(this.kpiProperties.getIndicator().getId());
        schema.setFields(this.kpiProperties.getIndicator().getSchema().getFields());

        return schema;
    }

    @Override
    public void sendIndicatorAccessEntryEvents() throws InvalidApplicationException {
        try {
            List<String> allowedRoles = new ArrayList<>();
            allowedRoles.addAll(this.kpiProperties.getIndicator().getRoles());
            allowedRoles.addAll(this.kpiProperties.getIndicator().getTenantRoles());

            UserRoleQuery userRoleQuery = this.queryFactory.query(UserRoleQuery.class).disableTracking().roles(allowedRoles);
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).userRoleSubQuery(userRoleQuery).collectAs(new BaseFieldSet().ensure(User._id).ensure(User._name).ensure(User._createdAt));

            if (users != null && !users.isEmpty()) {
                for (UserEntity user: users) {
                    this.indicatorAccessEventHandler.handle(user.getId());
                }
            }

        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointUserCountEntryEvents() throws InvalidApplicationException {

        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            // default tenant
            List<UserEntity> items = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(User._id).ensure(User._createdAt));
            var groupItems = items.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.User, MetricType.Overtime, null, null, null, null);

            // specific tenants
            List<TenantUserEntity> allTenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().isActive(IsActive.Active).collect();
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            for (TenantEntity tenant: tenants) {
                List<TenantUserEntity> tenantUserEntities = allTenantUserEntities.stream().filter(x -> x.getTenantId().equals(tenant.getId())).toList();
                var groupTenantItems = tenantUserEntities.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.User, MetricType.Overtime, null, null, null, null);
            }

        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointPlanCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<PlanEntity> items = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._createdAt).ensure(Plan._belongsToCurrentTenant).ensure(Plan._status).ensure(Plan._blueprint));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<PlanBlueprintEntity> blueprints = this.queryFactory.query(PlanBlueprintQuery.class).isActive(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._groupId).ensure(PlanBlueprint._belongsToCurrentTenant));
            List<PlanStatusEntity> planStatusEntities = this.queryFactory.query(PlanStatusQuery.class).isActives(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(PlanStatus._id).ensure(PlanStatus._internalStatus).ensure(PlanStatus._name).ensure(PlanStatus._belongsToCurrentTenant));

            List<PlanEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.Plan, MetricType.Overtime, null, null, null, null);

            List<PlanStatusEntity> defaultTenantPlanStatus = planStatusEntities.stream().filter(x -> x.getTenantId() == null).toList();
            List<PlanBlueprintEntity> defaultTenantBlueprints = blueprints.stream().filter(x -> x.getTenantId() == null).toList();
            for(PlanStatusEntity status: defaultTenantPlanStatus) {
                List<PlanEntity> defaultTenantPlansWithStatus = items.stream().filter(x -> x.getTenantId() == null && x.getStatusId().equals(status.getId())).toList().stream().toList();
                for(PlanBlueprintEntity blueprint: defaultTenantBlueprints) {
                    List<PlanEntity> blueprintItems = defaultTenantPlansWithStatus.stream().filter(x -> x.getBlueprintId() != null && blueprint.getId().equals(x.getBlueprintId())).toList();
                    var groupBlueprintItems = blueprintItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    this.sendIndicatorPointEvents(groupBlueprintItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.PlanPerBlueprint, MetricType.Overtime, null, null, blueprint.getGroupId(), status.getId());

                }
            }

            for (TenantEntity tenant: tenants) {
                List<PlanEntity> tenantItems = items.stream().filter(x -> x.getTenantId()!= null && tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.Plan, MetricType.Overtime, null, null, null, null);

                List<PlanStatusEntity> tenantPlanStatus = planStatusEntities.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId()))).toList();
                List<PlanBlueprintEntity> tenantBlueprints = blueprints.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId()))).toList();
                for(PlanStatusEntity status: tenantPlanStatus) {
                    List<PlanEntity> tenantPlansWithStatus = items.stream().filter(x -> x.getTenantId()!= null && tenant.getId().equals(x.getTenantId()) && x.getStatusId().equals(status.getId())).toList().stream().toList();
                    for(PlanBlueprintEntity blueprint: tenantBlueprints) {
                        List<PlanEntity> blueprintTenantItems = tenantPlansWithStatus.stream().filter(x -> x.getBlueprintId() != null && blueprint.getId().equals(x.getBlueprintId())).toList();
                        var groupTenantBlueprintItems = blueprintTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                        this.sendIndicatorPointEvents(groupTenantBlueprintItems, tenant.getCode(), KpiEntityType.PlanPerBlueprint, MetricType.Overtime, null, null, blueprint.getGroupId(), status.getId());
                    }
                }
            }
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointDescriptionCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<DescriptionEntity> items = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._createdAt).ensure(Description._belongsToCurrentTenant).ensure(Description._status).ensure(Description._descriptionTemplate));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<DescriptionTemplateEntity> descriptionTemplates = this.queryFactory.query(DescriptionTemplateQuery.class).isActive(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._groupId).ensure(DescriptionTemplate._belongsToCurrentTenant).ensure(DescriptionTemplate._status).ensure(DescriptionTemplate._versionStatus));
            List<DescriptionStatusEntity> descriptionStatusEntities = this.queryFactory.query(DescriptionStatusQuery.class).isActive(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._internalStatus).ensure(DescriptionStatus._name).ensure(DescriptionStatus._belongsToCurrentTenant));

            List<DescriptionEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.Description, MetricType.Overtime, null, null, null, null);

            List<DescriptionStatusEntity> defaultTenantDescriptionStatus = descriptionStatusEntities.stream().filter(x -> x.getTenantId() == null).toList();
            List<DescriptionTemplateEntity> defaultTenantTemplates = descriptionTemplates.stream().filter(x -> x.getTenantId() == null && x.getStatus().equals(DescriptionTemplateStatus.Finalized) && x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
            for(DescriptionStatusEntity status: defaultTenantDescriptionStatus) {
                List<DescriptionEntity> defaultTenantDescriptionsWithStatus = items.stream().filter(x -> x.getTenantId() == null && x.getStatusId().equals(status.getId())).toList().stream().toList();
                for(DescriptionTemplateEntity template: defaultTenantTemplates) {
                    List<DescriptionEntity> descriptionEntities = new ArrayList<>();
                    List<DescriptionTemplateEntity> allTemplatesWithSameGroupId = descriptionTemplates.stream().filter(x -> x.getGroupId().equals(template.getGroupId())).toList();
                    for(DescriptionTemplateEntity allTemplate: allTemplatesWithSameGroupId){
                        List<DescriptionEntity> descriptionItems = defaultTenantDescriptionsWithStatus.stream().filter(x -> x.getDescriptionTemplateId() != null && allTemplate.getId().equals(x.getDescriptionTemplateId())).toList();
                        descriptionEntities.addAll(descriptionItems);
                    }
                    var groupTemplatesItems = descriptionEntities.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    this.sendIndicatorPointEvents(groupTemplatesItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.DescriptionPerTemplate, MetricType.Overtime, null, null, template.getGroupId(), status.getId());

                }
            }

            for (TenantEntity tenant: tenants) {
                List<DescriptionEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.Description, MetricType.Overtime, null, null, null, null);

                List<DescriptionStatusEntity> tenantDescriptionStatus = descriptionStatusEntities.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId()))).toList();
                List<DescriptionTemplateEntity> tenantTemplates = descriptionTemplates.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId())) && x.getStatus().equals(DescriptionTemplateStatus.Finalized) && x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
                for(DescriptionStatusEntity status: tenantDescriptionStatus) {
                    List<DescriptionEntity> tenantDescriptionsWithStatus = items.stream().filter(x -> x.getTenantId()!= null && tenant.getId().equals(x.getTenantId()) && x.getStatusId().equals(status.getId())).toList().stream().toList();
                    for(DescriptionTemplateEntity template: tenantTemplates) {
                        List<DescriptionEntity> descriptionEntities = new ArrayList<>();
                        List<DescriptionTemplateEntity> allTemplatesWithSameGroupId = descriptionTemplates.stream().filter(x -> x.getGroupId().equals(template.getGroupId())).toList();
                        for(DescriptionTemplateEntity allTemplate: allTemplatesWithSameGroupId){
                            List<DescriptionEntity> descriptionItems = tenantDescriptionsWithStatus.stream().filter(x -> x.getDescriptionTemplateId() != null && allTemplate.getId().equals(x.getDescriptionTemplateId())).toList();
                            descriptionEntities.addAll(descriptionItems);
                        }
                        var groupTenantTemplatesItems = descriptionEntities.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                        this.sendIndicatorPointEvents(groupTenantTemplatesItems, tenant.getCode(), KpiEntityType.DescriptionPerTemplate, MetricType.Overtime, null, null, template.getGroupId(), status.getId());

                    }
                }
            }
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointReferenceCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<PlanReferenceEntity> planReferences = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().isActives(IsActive.Active).collect();
            List<DescriptionReferenceEntity> descriptionReferences = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().isActive(IsActive.Active).collect();
            List<ReferenceEntity> references = this.queryFactory.query(ReferenceQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._type).ensure(Reference._createdAt).ensure(Reference._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<ReferenceTypeEntity> referenceTypes = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(ReferenceType._id));

            for (ReferenceTypeEntity referenceTypeEntity: referenceTypes) {
                List<UUID> defaultTenantReferencesIds = references.stream().filter(x -> x.getTenantId() == null && x.getTypeId().equals(referenceTypeEntity.getId())).toList().stream().map(ReferenceEntity::getId).toList();
                List<PlanReferenceEntity> defaultPlanReferences = planReferences.stream().filter(x -> x.getTenantId() == null && defaultTenantReferencesIds.contains(x.getReferenceId())).toList();
                List<DescriptionReferenceEntity> defaultDescriptionReferences = descriptionReferences.stream().filter(x -> x.getTenantId() == null && defaultTenantReferencesIds.contains(x.getReferenceId())).toList();

                this.calculateAndSendReferenceIndicatorPoints(defaultPlanReferences, defaultDescriptionReferences, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), referenceTypeEntity.getId());

            }

            for (TenantEntity tenant: tenants) {
                for (ReferenceTypeEntity referenceTypeEntity: referenceTypes) {
                    List<UUID> tenantReferencesIds = references.stream().filter(x -> x.getTypeId().equals(referenceTypeEntity.getId())).toList().stream().map(ReferenceEntity::getId).toList();

                    List<PlanReferenceEntity> tenantPlanReferences = planReferences.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && tenantReferencesIds.contains(x.getReferenceId())).toList();
                    List<DescriptionReferenceEntity> tenantDescriptionReferences = descriptionReferences.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && tenantReferencesIds.contains(x.getReferenceId())).toList();

                    this.calculateAndSendReferenceIndicatorPoints(tenantPlanReferences, tenantDescriptionReferences, tenant.getCode(), referenceTypeEntity.getId());
                }

            }
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    private void calculateAndSendReferenceIndicatorPoints(List<PlanReferenceEntity> planReferences, List<DescriptionReferenceEntity> descriptionReferences, String tenantCode, UUID referenceTypeId) {
        Map<LocalDate, Long> groupPlanReferenceItems = new HashMap<>();
        if (!this.conventionService.isListNullOrEmpty(planReferences)) {
            groupPlanReferenceItems = planReferences.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
        }

        Map<LocalDate, Long> groupDescriptionReferenceItems = new HashMap<>();
        if (!this.conventionService.isListNullOrEmpty(descriptionReferences)) {
            groupDescriptionReferenceItems = descriptionReferences.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
        }

        Map<LocalDate, Long> finalGroupItems = groupPlanReferenceItems;
        groupDescriptionReferenceItems.forEach((key, value) ->
                finalGroupItems.merge(key, value, Long::sum)
        );

        this.sendIndicatorPointEvents(finalGroupItems, tenantCode, KpiEntityType.Reference, MetricType.Overtime, referenceTypeId, null, null, null);
    }

    @Override
    public void sendIndicatorPointPlanBlueprintCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._createdAt).ensure(PlanBlueprint._belongsToCurrentTenant).ensure(PlanBlueprint._versionStatus));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<PlanBlueprintEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, this.messageSource.getMessage("kpi.totalCount", new Object[]{}, LocaleContextHolder.getLocale()), null, null);

            List<PlanBlueprintEntity> currentVersionDefaultTenantItems = defaultTenantItems.stream().filter(x -> x.getVersionStatus().equals(PlanBlueprintVersionStatus.Current)).toList();
            var groupCurrentVersionItems = currentVersionDefaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupCurrentVersionItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, this.messageSource.getMessage("kpi.latestVersion", new Object[]{}, LocaleContextHolder.getLocale()), null, null);


            for (TenantEntity tenant: tenants) {
                List<PlanBlueprintEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, this.messageSource.getMessage("kpi.totalCount", new Object[]{}, LocaleContextHolder.getLocale()), null, null);

                List<PlanBlueprintEntity> currentVersionTenantItems = tenantItems.stream().filter(x -> x.getVersionStatus().equals(PlanBlueprintVersionStatus.Current)).toList();
                var groupTenantCurrentVersionItems = currentVersionTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantCurrentVersionItems, tenant.getCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, this.messageSource.getMessage("kpi.latestVersion", new Object[]{}, LocaleContextHolder.getLocale()), null, null);

            }
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<DescriptionTemplateEntity> items = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._createdAt).ensure(DescriptionTemplate._belongsToCurrentTenant).ensure(DescriptionTemplate._versionStatus));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<DescriptionTemplateEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, this.messageSource.getMessage("kpi.totalCount", new Object[]{}, LocaleContextHolder.getLocale()), null, null);

            List<DescriptionTemplateEntity> currentVersionDefaultTenantItems = defaultTenantItems.stream().filter(x -> x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
            var groupCurrentVersionItems = currentVersionDefaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupCurrentVersionItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, this.messageSource.getMessage("kpi.latestVersion", new Object[]{}, LocaleContextHolder.getLocale()), null, null);

            for (TenantEntity tenant: tenants) {
                List<DescriptionTemplateEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, this.messageSource.getMessage("kpi.totalCount", new Object[]{}, LocaleContextHolder.getLocale()), null, null);

                List<DescriptionTemplateEntity> currentVersionTenantItems = tenantItems.stream().filter(x -> x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
                var groupTenantCurrentVersionItems = currentVersionTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantCurrentVersionItems, tenant.getCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, this.messageSource.getMessage("kpi.latestVersion", new Object[]{}, LocaleContextHolder.getLocale()), null, null);
            }
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointTenantCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<TenantEntity> items = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._createdAt).ensure(Tenant._code));
            var groupItems = items.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.Tenant, MetricType.Overtime, null, null, null, null);

        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    private void sendIndicatorPointEvents(Map<LocalDate, Long> groupItems, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId) {

        var dates = groupItems.keySet().stream().collect(Collectors.toList());

        if (!dates.isEmpty()) {

            IndicatorPointEvent event = new IndicatorPointEvent();
            if (kpiProperties.getIndicator() != null) event.setIndicatorId(this.kpiProperties.getIndicator().getId());

            dates.sort(Comparator.comparing(x -> x));

            List<Map<String, Object>> properties = new ArrayList<>();
            for (var date: dates) {

                Map<String, Object> map = this.buildMap(date, groupItems.get(date), tenantCode, entityType, metricType, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId);
                properties.add(map);
                event.setProperties(properties);

                if (properties.size() >= this.kpiProperties.getMaxIndicatorPointsPerRequest()) {
                    // send indicator points and initialize for the next request
                    this.validatorFactory.validator(IndicatorPointEvent.IndicatorPointEventValidator.class).validateForce(event);
                    this.indicatorPointEventHandler.handle(event);
                    properties = new ArrayList<>();
                }

            }

            if (!properties.isEmpty()) {
                this.validatorFactory.validator(IndicatorPointEvent.IndicatorPointEventValidator.class).validateForce(event);
                this.indicatorPointEventHandler.handle(event);
            }
        }


    }

    //
    // single indicator point
    //
    @Override
    public void sendIndicatorPointPlanEntry(KpiDirectionType type) throws InvalidApplicationException {
        if (type == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        this.sendSingleIndicatorPointEvent(type, KpiEntityType.Plan, tenantCode, null, null, null, null);

    }

    @Override
    public void sendIndicatorPointPlanPerBlueprintEntry(KpiDirectionType type, UUID planId) throws InvalidApplicationException {
        if (type == null || planId == null) return;

        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).isActive(IsActive.Active).firstAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._status).ensure(Plan._blueprint));
            if (planEntity != null ) {
                PlanBlueprintEntity planBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(planEntity.getBlueprintId()).firstAs(new BaseFieldSet().ensure(PlanBlueprint._groupId));
                if(planBlueprintEntity != null) {
                    String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode();
                    this.sendSingleIndicatorPointEvent(type, KpiEntityType.PlanPerBlueprint, tenantCode, null, null, planBlueprintEntity.getGroupId(), planEntity.getStatusId());
                }
            }
        }
        finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointDescriptionEntry(KpiDirectionType type) throws InvalidApplicationException {

        if (type == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        this.sendSingleIndicatorPointEvent(type, KpiEntityType.Description, tenantCode, null, null, null, null);

    }

    @Override
    public void sendIndicatorPointDescriptionPerTemplateEntry(KpiDirectionType type, UUID descriptionId) throws InvalidApplicationException {
        if (type == null || descriptionId == null) return;

        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            DescriptionEntity descriptionEntity = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionId).isActive(IsActive.Active).firstAs(new BaseFieldSet().ensure(Description._id).ensure(Description._status).ensure(Description._descriptionTemplate));
            if (descriptionEntity != null ) {
                DescriptionTemplateEntity descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(descriptionEntity.getDescriptionTemplateId()).firstAs(new BaseFieldSet().ensure(DescriptionTemplate._groupId));
                if(descriptionTemplateEntity != null) {
                    String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode();
                    this.sendSingleIndicatorPointEvent(type, KpiEntityType.DescriptionPerTemplate, tenantCode, null, null, descriptionTemplateEntity.getGroupId(), descriptionEntity.getStatusId());
                }
            }
        }
        finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointReferenceEntry(KpiDirectionType type, UUID referenceId) throws InvalidApplicationException {
        if (type == null || referenceId == null) return;

        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            ReferenceEntity reference = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(referenceId).firstAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._type));
            if (reference != null ) {
                ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(reference.getTypeId()).firstAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceType._code));
                if (referenceTypeEntity != null) {
                    String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode();
                    this.sendSingleIndicatorPointEvent(type, KpiEntityType.Reference, tenantCode, referenceTypeEntity.getId(), null, null, null);
                }
            }
        }
        finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }


    @Override
    public void sendIndicatorPointPlanBlueprintEntry(KpiDirectionType type, KpiVersionType versionType) throws InvalidApplicationException {
        if (type == null || versionType == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        KpiEntityType entityType = KpiEntityType.PlanBlueprint;

        if (versionType.equals(KpiVersionType.LatestVersion)) {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, this.messageSource.getMessage("kpi.latestVersion", new Object[]{}, LocaleContextHolder.getLocale()), null, null);
        } else {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, this.messageSource.getMessage("kpi.totalCount", new Object[]{}, LocaleContextHolder.getLocale()), null, null);
        }
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType type, KpiVersionType versionType) throws InvalidApplicationException {
        if (type == null || versionType == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        KpiEntityType entityType = KpiEntityType.DescriptionTemplate;

        if (versionType.equals(KpiVersionType.LatestVersion)) {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, this.messageSource.getMessage("kpi.latestVersion", new Object[]{}, LocaleContextHolder.getLocale()), null, null);
        } else {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, this.messageSource.getMessage("kpi.totalCount", new Object[]{}, LocaleContextHolder.getLocale()), null, null);
        }
    }

    @Override
    public void sendIndicatorPointUserEntry(KpiDirectionType type) throws InvalidApplicationException {
        if (type == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();

        this.sendSingleIndicatorPointEvent(type, KpiEntityType.User, tenantCode, null, null, null, null);
    }

    @Override
    public void sendIndicatorPointUserEntry(KpiDirectionType type, Boolean useDefaultTenantCode) throws InvalidApplicationException {
        if (type == null) return;
        if (useDefaultTenantCode) {
            this.sendSingleIndicatorPointEvent(type, KpiEntityType.User, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), null, null, null, null);
            return;
        }
        this.sendIndicatorPointUserEntry(type);
    }

    @Override
    public void sendIndicatorPointTenantEntry(KpiDirectionType type) throws InvalidApplicationException {
        if (type == null) return;

        this.sendSingleIndicatorPointEvent(type, KpiEntityType.Tenant, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), null, null, null, null);
    }

    private void sendSingleIndicatorPointEvent(KpiDirectionType type, KpiEntityType entityType, String tenantCode, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId) throws InvalidApplicationException {

        int sum = type.equals(KpiDirectionType.Increase)? 1: -1;

        IndicatorPointEvent event = new IndicatorPointEvent();
        if (kpiProperties.getIndicator() != null) event.setIndicatorId(this.kpiProperties.getIndicator().getId());

        List<Map<String, Object>> properties = new ArrayList<>();
        Map<String, Object> map = this.buildMap(LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")), sum, tenantCode, entityType, MetricType.Overtime, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId);
        properties.add(map);
        event.setProperties(properties);

        this.validatorFactory.validator(IndicatorPointEvent.IndicatorPointEventValidator.class).validateForce(event);
        this.indicatorPointEventHandler.handle(event);

    }

    private Map<String, Object> buildMap(LocalDate date, long sum, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId) {
        Map<String, Object> map = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        map.put(KpiSchemaFieldCodes.CreatedAt, date.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter));
        map.put(KpiSchemaFieldCodes.Value, sum);
        map.put(KpiSchemaFieldCodes.TenantCode, tenantCode);
        map.put(KpiSchemaFieldCodes.EntityType, entityType);
        map.put(KpiSchemaFieldCodes.MetricType, metricType);
        if (entityType.equals(KpiEntityType.Reference)) map.put(KpiSchemaFieldCodes.ReferenceTypeId, referenceTypeId);
        if (entityType.equals(KpiEntityType.PlanPerBlueprint) && blueprintOrTemplateGroupId != null) map.put(KpiSchemaFieldCodes.BlueprintGroupId, blueprintOrTemplateGroupId);
        if (entityType.equals(KpiEntityType.PlanPerBlueprint) && statusId != null) map.put(KpiSchemaFieldCodes.PlanStatus, statusId);
        if (entityType.equals(KpiEntityType.DescriptionPerTemplate) && blueprintOrTemplateGroupId != null) map.put(KpiSchemaFieldCodes.TemplateGroupId, blueprintOrTemplateGroupId);
        if (entityType.equals(KpiEntityType.DescriptionPerTemplate) && statusId != null) map.put(KpiSchemaFieldCodes.DescriptionStatus, statusId);
        if (entityType.equals(KpiEntityType.DescriptionTemplate)) map.put(KpiSchemaFieldCodes.LatestVersion, versionCode);
        if (entityType.equals(KpiEntityType.PlanBlueprint)) map.put(KpiSchemaFieldCodes.LatestVersion, versionCode);

        return map;
    }
}

