package org.opencdmp.service.kpi;


import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.enums.kpi.KpiVersionType;
import org.opencdmp.commons.enums.kpi.KpiEntityType;
import org.opencdmp.commons.enums.kpi.MetricType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.outbox.dashboardtouched.DashboardTouchedEvent;
import org.opencdmp.integrationevent.outbox.dashboardtouched.DashboardTouchedEventHandler;
import org.opencdmp.integrationevent.outbox.indicator.*;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEvent;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEvent;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.PlanBlueprintType;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    private final DashboardTouchedEventHandler dashboardTouchedEventHandler;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final QueryFactory queryFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final MessageSource messageSource;
    private final ResourceLoader resourceLoader;

    @Autowired
    public KpiServiceImpl(
            KpiProperties kpiProperties, ValidatorFactory validatorFactory, ConventionService conventionService, IndicatorPointEventHandler indicatorPointEventHandler, IndicatorAccessEventHandler indicatorAccessEventHandler, DashboardTouchedEventHandler dashboardTouchedEventHandler, TenantEntityManagerFactory tenantEntityManagerFactory, QueryFactory queryFactory, TenantScopeFactory tenantScopeFactory, MessageSource messageSource, ResourceLoader resourceLoader) {
        this.kpiProperties = kpiProperties;
        this.validatorFactory = validatorFactory;
        this.conventionService = conventionService;
        this.indicatorPointEventHandler = indicatorPointEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.dashboardTouchedEventHandler = dashboardTouchedEventHandler;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.queryFactory = queryFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.messageSource = messageSource;
        this.resourceLoader = resourceLoader;
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
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.User, MetricType.Overtime, null, null, null, null, null);

            // specific tenants
            List<TenantUserEntity> allTenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().isActive(IsActive.Active).collect();
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            for (TenantEntity tenant: tenants) {
                List<TenantUserEntity> tenantUserEntities = allTenantUserEntities.stream().filter(x -> x.getTenantId().equals(tenant.getId())).toList();
                var groupTenantItems = tenantUserEntities.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.User, MetricType.Overtime, null, null, null, null, null, null);
            }

        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointPlanCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<PlanEntity> items = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._createdAt).ensure(Plan._belongsToCurrentTenant).ensure(Plan._status).ensure(Plan._blueprint).ensure(Plan._accessType));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<PlanBlueprintEntity> blueprints = this.queryFactory.query(PlanBlueprintQuery.class).isActive(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._groupId).ensure(PlanBlueprint._belongsToCurrentTenant));
            List<PlanStatusEntity> planStatusEntities = this.queryFactory.query(PlanStatusQuery.class).isActives(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(PlanStatus._id).ensure(PlanStatus._internalStatus).ensure(PlanStatus._name).ensure(PlanStatus._belongsToCurrentTenant));

            // Bucket plans once. plansByTenant feeds Plan/Overtime; plansByTenantStatusBlueprint feeds PlanPerBlueprint and replaces the per-(status × blueprint) re-filter scans.
            Map<UUID, List<PlanEntity>> plansByTenant = new HashMap<>();
            for (PlanEntity p : items) {
                plansByTenant.computeIfAbsent(p.getTenantId(), k -> new ArrayList<>()).add(p);
            }
            Map<UUID, Map<UUID, Map<UUID, List<PlanEntity>>>> plansByTenantStatusBlueprint = new HashMap<>();
            for (PlanEntity p : items) {
                if (p.getStatusId() == null || p.getBlueprintId() == null) continue;
                plansByTenantStatusBlueprint
                        .computeIfAbsent(p.getTenantId(), k -> new HashMap<>())
                        .computeIfAbsent(p.getStatusId(), k -> new HashMap<>())
                        .computeIfAbsent(p.getBlueprintId(), k -> new ArrayList<>())
                        .add(p);
            }

            List<Map<String, Object>> buffer = new ArrayList<>();

            // Default tenant
            this.accumulatePlanOvertime(buffer, plansByTenant.getOrDefault(null, Collections.emptyList()), this.tenantScopeFactory.getInstance().getDefaultTenantCode());

            List<PlanStatusEntity> defaultTenantPlanStatus = planStatusEntities.stream().filter(x -> x.getTenantId() == null).toList();
            List<PlanBlueprintEntity> defaultTenantBlueprints = blueprints.stream().filter(x -> x.getTenantId() == null).toList();
            Map<UUID, Map<UUID, List<PlanEntity>>> defaultPlansByStatusByBlueprint = plansByTenantStatusBlueprint.getOrDefault(null, Collections.emptyMap());
            for (PlanStatusEntity status : defaultTenantPlanStatus) {
                Map<UUID, List<PlanEntity>> byBlueprint = defaultPlansByStatusByBlueprint.getOrDefault(status.getId(), Collections.emptyMap());
                for (PlanBlueprintEntity blueprint : defaultTenantBlueprints) {
                    this.accumulatePlanPerBlueprintGroupedByVisibility(buffer, byBlueprint.getOrDefault(blueprint.getId(), Collections.emptyList()), this.tenantScopeFactory.getInstance().getDefaultTenantCode(), blueprint.getGroupId(), status.getId());
                }
            }

            for (TenantEntity tenant : tenants) {
                this.accumulatePlanOvertime(buffer, plansByTenant.getOrDefault(tenant.getId(), Collections.emptyList()), tenant.getCode());

                List<PlanStatusEntity> tenantPlanStatus = planStatusEntities.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId()))).toList();
                List<PlanBlueprintEntity> tenantBlueprints = blueprints.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId()))).toList();
                Map<UUID, Map<UUID, List<PlanEntity>>> tenantPlansByStatusByBlueprint = plansByTenantStatusBlueprint.getOrDefault(tenant.getId(), Collections.emptyMap());
                for (PlanStatusEntity status : tenantPlanStatus) {
                    Map<UUID, List<PlanEntity>> byBlueprint = tenantPlansByStatusByBlueprint.getOrDefault(status.getId(), Collections.emptyMap());
                    for (PlanBlueprintEntity blueprint : tenantBlueprints) {
                        this.accumulatePlanPerBlueprintGroupedByVisibility(buffer, byBlueprint.getOrDefault(blueprint.getId(), Collections.emptyList()), tenant.getCode(), blueprint.getGroupId(), status.getId());
                    }
                }
            }

            this.flushIndicatorPointBuffer(buffer);
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    private void accumulatePlanOvertime(List<Map<String, Object>> buffer, List<PlanEntity> plans, String tenantCode) {
        if (plans == null || plans.isEmpty()) return;
        int max = this.kpiProperties.getMaxIndicatorPointsPerRequest();
        Map<LocalDate, Long> dateGroup = plans.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
        for (var dateEntry : dateGroup.entrySet()) {
            buffer.add(this.buildMap(dateEntry.getKey(), dateEntry.getValue(), tenantCode, KpiEntityType.Plan, MetricType.Overtime, null, null, null, null));
            if (buffer.size() >= max) this.flushIndicatorPointBuffer(buffer);
        }
    }

    private void accumulatePlanPerBlueprintGroupedByVisibility(List<Map<String, Object>> buffer, List<PlanEntity> plans, String tenantCode, UUID blueprintGroupId, UUID statusId) {
        if (plans == null || plans.isEmpty()) return;
        int max = this.kpiProperties.getMaxIndicatorPointsPerRequest();
        Map<String, List<PlanEntity>> byVisibility = plans.stream().collect(Collectors.groupingBy(p -> {
            String code = planVisibilityCode(p.getAccessType());
            return code != null ? code : "";
        }));
        for (var entry : byVisibility.entrySet()) {
            String visibilityCode = entry.getKey().isEmpty() ? null : entry.getKey();
            Map<LocalDate, Long> dateGroup = entry.getValue().stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            for (var dateEntry : dateGroup.entrySet()) {
                buffer.add(this.buildMap(dateEntry.getKey(), dateEntry.getValue(), tenantCode, KpiEntityType.PlanPerBlueprint, MetricType.Overtime, null, null, blueprintGroupId, statusId, visibilityCode, null, null));
                if (buffer.size() >= max) this.flushIndicatorPointBuffer(buffer);
            }
        }
    }

    @Override
    public void sendIndicatorPointDescriptionCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<DescriptionEntity> items = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._createdAt).ensure(Description._belongsToCurrentTenant).ensure(Description._status).ensure(Description._descriptionTemplate).ensure(Description._plan));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<DescriptionTemplateEntity> descriptionTemplates = this.queryFactory.query(DescriptionTemplateQuery.class).isActive(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._groupId).ensure(DescriptionTemplate._belongsToCurrentTenant).ensure(DescriptionTemplate._status).ensure(DescriptionTemplate._versionStatus));
            List<DescriptionStatusEntity> descriptionStatusEntities = this.queryFactory.query(DescriptionStatusQuery.class).isActive(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._internalStatus).ensure(DescriptionStatus._name).ensure(DescriptionStatus._belongsToCurrentTenant));
            List<PlanEntity> plans = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._blueprint));
            List<PlanBlueprintEntity> planBlueprints = this.queryFactory.query(PlanBlueprintQuery.class).isActive(IsActive.Active).disableTracking().collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._groupId));

            Map<UUID, UUID> blueprintIdToGroupId = planBlueprints.stream().filter(b -> b.getGroupId() != null).collect(Collectors.toMap(PlanBlueprintEntity::getId, PlanBlueprintEntity::getGroupId, (a, b) -> a));
            Map<UUID, UUID> planIdToBlueprintGroupId = new HashMap<>();
            for (PlanEntity p : plans) {
                if (p.getBlueprintId() == null) continue;
                UUID bgId = blueprintIdToGroupId.get(p.getBlueprintId());
                if (bgId != null) planIdToBlueprintGroupId.put(p.getId(), bgId);
            }

            // descriptionTemplateId -> groupId. Lets us bucket descriptions by template-group up front instead of re-streaming descriptionTemplates inside the (status × template) loop.
            Map<UUID, UUID> templateIdToGroupId = descriptionTemplates.stream().filter(t -> t.getGroupId() != null).collect(Collectors.toMap(DescriptionTemplateEntity::getId, DescriptionTemplateEntity::getGroupId, (a, b) -> a));

            // Bucket descriptions once. descsByTenant feeds Description/Overtime; descsByTenantStatusGroup feeds DescriptionPerTemplate.
            Map<UUID, List<DescriptionEntity>> descsByTenant = new HashMap<>();
            for (DescriptionEntity d : items) {
                descsByTenant.computeIfAbsent(d.getTenantId(), k -> new ArrayList<>()).add(d);
            }
            Map<UUID, Map<UUID, Map<UUID, List<DescriptionEntity>>>> descsByTenantStatusGroup = new HashMap<>();
            for (DescriptionEntity d : items) {
                if (d.getStatusId() == null || d.getDescriptionTemplateId() == null) continue;
                UUID groupId = templateIdToGroupId.get(d.getDescriptionTemplateId());
                if (groupId == null) continue;
                descsByTenantStatusGroup
                        .computeIfAbsent(d.getTenantId(), k -> new HashMap<>())
                        .computeIfAbsent(d.getStatusId(), k -> new HashMap<>())
                        .computeIfAbsent(groupId, k -> new ArrayList<>())
                        .add(d);
            }

            List<Map<String, Object>> buffer = new ArrayList<>();

            // Default tenant
            this.accumulateDescriptionOvertime(buffer, descsByTenant.getOrDefault(null, Collections.emptyList()), this.tenantScopeFactory.getInstance().getDefaultTenantCode());

            List<DescriptionStatusEntity> defaultTenantDescriptionStatus = descriptionStatusEntities.stream().filter(x -> x.getTenantId() == null).toList();
            List<DescriptionTemplateEntity> defaultTenantTemplates = descriptionTemplates.stream().filter(x -> x.getTenantId() == null && x.getStatus().equals(DescriptionTemplateStatus.Finalized) && x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
            Map<UUID, Map<UUID, List<DescriptionEntity>>> defaultByStatusByGroup = descsByTenantStatusGroup.getOrDefault(null, Collections.emptyMap());
            for (DescriptionStatusEntity status : defaultTenantDescriptionStatus) {
                Map<UUID, List<DescriptionEntity>> byGroup = defaultByStatusByGroup.getOrDefault(status.getId(), Collections.emptyMap());
                for (DescriptionTemplateEntity template : defaultTenantTemplates) {
                    this.accumulateDescriptionPerTemplateGroupedByBlueprint(buffer, byGroup.getOrDefault(template.getGroupId(), Collections.emptyList()), this.tenantScopeFactory.getInstance().getDefaultTenantCode(), template.getGroupId(), status.getId(), planIdToBlueprintGroupId);
                }
            }

            for (TenantEntity tenant : tenants) {
                this.accumulateDescriptionOvertime(buffer, descsByTenant.getOrDefault(tenant.getId(), Collections.emptyList()), tenant.getCode());

                List<DescriptionStatusEntity> tenantDescriptionStatus = descriptionStatusEntities.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId()))).toList();
                List<DescriptionTemplateEntity> tenantTemplates = descriptionTemplates.stream().filter(x -> x.getTenantId() == null || (x.getTenantId()!= null && x.getTenantId().equals(tenant.getId())) && x.getStatus().equals(DescriptionTemplateStatus.Finalized) && x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
                Map<UUID, Map<UUID, List<DescriptionEntity>>> tenantByStatusByGroup = descsByTenantStatusGroup.getOrDefault(tenant.getId(), Collections.emptyMap());
                for (DescriptionStatusEntity status : tenantDescriptionStatus) {
                    Map<UUID, List<DescriptionEntity>> byGroup = tenantByStatusByGroup.getOrDefault(status.getId(), Collections.emptyMap());
                    for (DescriptionTemplateEntity template : tenantTemplates) {
                        this.accumulateDescriptionPerTemplateGroupedByBlueprint(buffer, byGroup.getOrDefault(template.getGroupId(), Collections.emptyList()), tenant.getCode(), template.getGroupId(), status.getId(), planIdToBlueprintGroupId);
                    }
                }
            }

            this.flushIndicatorPointBuffer(buffer);
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    private static final UUID DESCRIPTION_NO_BLUEPRINT_KEY = new UUID(0L, 0L);

    private void accumulateDescriptionOvertime(List<Map<String, Object>> buffer, List<DescriptionEntity> descriptions, String tenantCode) {
        if (descriptions == null || descriptions.isEmpty()) return;
        int max = this.kpiProperties.getMaxIndicatorPointsPerRequest();
        Map<LocalDate, Long> dateGroup = descriptions.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
        for (var dateEntry : dateGroup.entrySet()) {
            buffer.add(this.buildMap(dateEntry.getKey(), dateEntry.getValue(), tenantCode, KpiEntityType.Description, MetricType.Overtime, null, null, null, null));
            if (buffer.size() >= max) this.flushIndicatorPointBuffer(buffer);
        }
    }

    private void accumulateDescriptionPerTemplateGroupedByBlueprint(List<Map<String, Object>> buffer, List<DescriptionEntity> descriptions, String tenantCode, UUID templateGroupId, UUID statusId, Map<UUID, UUID> planIdToBlueprintGroupId) {
        if (descriptions == null || descriptions.isEmpty()) return;
        int max = this.kpiProperties.getMaxIndicatorPointsPerRequest();
        Map<UUID, List<DescriptionEntity>> byBlueprint = descriptions.stream().collect(Collectors.groupingBy(d -> {
            UUID bgId = planIdToBlueprintGroupId.get(d.getPlanId());
            return bgId != null ? bgId : DESCRIPTION_NO_BLUEPRINT_KEY;
        }));
        for (var entry : byBlueprint.entrySet()) {
            UUID planBlueprintGroupId = entry.getKey().equals(DESCRIPTION_NO_BLUEPRINT_KEY) ? null : entry.getKey();
            Map<UUID, List<DescriptionEntity>> byPlan = entry.getValue().stream().collect(Collectors.groupingBy(d -> d.getPlanId() != null ? d.getPlanId() : DESCRIPTION_NO_BLUEPRINT_KEY));
            for (var planEntry : byPlan.entrySet()) {
                UUID planId = planEntry.getKey().equals(DESCRIPTION_NO_BLUEPRINT_KEY) ? null : planEntry.getKey();
                Map<LocalDate, Long> dateGroup = planEntry.getValue().stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                for (var dateEntry : dateGroup.entrySet()) {
                    buffer.add(this.buildMap(dateEntry.getKey(), dateEntry.getValue(), tenantCode, KpiEntityType.DescriptionPerTemplate, MetricType.Overtime, null, null, templateGroupId, statusId, null, planBlueprintGroupId, planId, null, null, null));
                    if (buffer.size() >= max) this.flushIndicatorPointBuffer(buffer);
                }
            }
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
            List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._plan));
            Map<UUID, UUID> descriptionIdToPlanId = descriptions.stream().filter(d -> d.getPlanId() != null).collect(Collectors.toMap(DescriptionEntity::getId, DescriptionEntity::getPlanId, (a, b) -> a));

            // Index references by id once. The default-tenant pass joins only against null-tenant references (matches the prior filter); the per-tenant pass joins against any reference of the requested type.
            Map<UUID, UUID> refIdToTypeIdAll = references.stream()
                    .filter(r -> r.getTypeId() != null)
                    .collect(Collectors.toMap(ReferenceEntity::getId, ReferenceEntity::getTypeId, (a, b) -> a));
            Map<UUID, UUID> refIdToTypeIdDefault = references.stream()
                    .filter(r -> r.getTenantId() == null && r.getTypeId() != null)
                    .collect(Collectors.toMap(ReferenceEntity::getId, ReferenceEntity::getTypeId, (a, b) -> a));

            // Bucket plan/description references by (tenantId, typeId) in a single pass each — replaces the per-(tenant,type) filter+List.contains scans. Default-tenant rows use null as the tenant key (HashMap permits it).
            Map<UUID, Map<UUID, List<PlanReferenceEntity>>> planRefBuckets = new HashMap<>();
            for (PlanReferenceEntity pr : planReferences) {
                if (pr.getReferenceId() == null) continue;
                UUID typeId = pr.getTenantId() == null ? refIdToTypeIdDefault.get(pr.getReferenceId()) : refIdToTypeIdAll.get(pr.getReferenceId());
                if (typeId == null) continue;
                planRefBuckets.computeIfAbsent(pr.getTenantId(), k -> new HashMap<>()).computeIfAbsent(typeId, k -> new ArrayList<>()).add(pr);
            }
            Map<UUID, Map<UUID, List<DescriptionReferenceEntity>>> descRefBuckets = new HashMap<>();
            for (DescriptionReferenceEntity dr : descriptionReferences) {
                if (dr.getReferenceId() == null) continue;
                UUID typeId = dr.getTenantId() == null ? refIdToTypeIdDefault.get(dr.getReferenceId()) : refIdToTypeIdAll.get(dr.getReferenceId());
                if (typeId == null) continue;
                descRefBuckets.computeIfAbsent(dr.getTenantId(), k -> new HashMap<>()).computeIfAbsent(typeId, k -> new ArrayList<>()).add(dr);
            }

            // Pack many points into a single shared buffer; the buffer flushes only when full or at the end, so a sync produces a few large outbox events instead of one event per (refId, planId) group.
            List<Map<String, Object>> buffer = new ArrayList<>();

            Map<UUID, List<PlanReferenceEntity>> defaultPlanByType = planRefBuckets.getOrDefault(null, Collections.emptyMap());
            Map<UUID, List<DescriptionReferenceEntity>> defaultDescByType = descRefBuckets.getOrDefault(null, Collections.emptyMap());
            for (ReferenceTypeEntity referenceTypeEntity : referenceTypes) {
                this.accumulateReferenceIndicatorPoints(buffer,
                        defaultPlanByType.getOrDefault(referenceTypeEntity.getId(), Collections.emptyList()),
                        defaultDescByType.getOrDefault(referenceTypeEntity.getId(), Collections.emptyList()),
                        this.tenantScopeFactory.getInstance().getDefaultTenantCode(),
                        referenceTypeEntity.getId(),
                        descriptionIdToPlanId);
            }

            for (TenantEntity tenant : tenants) {
                Map<UUID, List<PlanReferenceEntity>> tenantPlanByType = planRefBuckets.getOrDefault(tenant.getId(), Collections.emptyMap());
                Map<UUID, List<DescriptionReferenceEntity>> tenantDescByType = descRefBuckets.getOrDefault(tenant.getId(), Collections.emptyMap());
                for (ReferenceTypeEntity referenceTypeEntity : referenceTypes) {
                    this.accumulateReferenceIndicatorPoints(buffer,
                            tenantPlanByType.getOrDefault(referenceTypeEntity.getId(), Collections.emptyList()),
                            tenantDescByType.getOrDefault(referenceTypeEntity.getId(), Collections.emptyList()),
                            tenant.getCode(),
                            referenceTypeEntity.getId(),
                            descriptionIdToPlanId);
                }
            }

            this.flushIndicatorPointBuffer(buffer);
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    private void accumulateReferenceIndicatorPoints(List<Map<String, Object>> buffer, List<PlanReferenceEntity> planReferences, List<DescriptionReferenceEntity> descriptionReferences, String tenantCode, UUID referenceTypeId, Map<UUID, UUID> descriptionIdToPlanId) {
        int max = this.kpiProperties.getMaxIndicatorPointsPerRequest();

        if (!this.conventionService.isListNullOrEmpty(planReferences)) {
            // partition plan-references by (referenceId, planId) so charts can split by reference_id (e.g. top funders) or plan_id (references per plan)
            Map<UUID, Map<UUID, List<PlanReferenceEntity>>> byRefByPlan = planReferences.stream()
                    .filter(x -> x.getReferenceId() != null && x.getPlanId() != null)
                    .collect(Collectors.groupingBy(PlanReferenceEntity::getReferenceId, Collectors.groupingBy(PlanReferenceEntity::getPlanId)));
            for (var refEntry : byRefByPlan.entrySet()) {
                UUID refId = refEntry.getKey();
                for (var planEntry : refEntry.getValue().entrySet()) {
                    UUID planId = planEntry.getKey();
                    Map<LocalDate, Long> dateGroup = planEntry.getValue().stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    for (var dateEntry : dateGroup.entrySet()) {
                        buffer.add(this.buildMap(dateEntry.getKey(), dateEntry.getValue(), tenantCode, KpiEntityType.Reference, MetricType.Overtime, referenceTypeId, null, null, null, null, null, planId, refId, null,null));
                        if (buffer.size() >= max) this.flushIndicatorPointBuffer(buffer);
                    }
                }
            }
        }

        if (!this.conventionService.isListNullOrEmpty(descriptionReferences)) {
            // description-references: resolve description -> plan via the prefetched map so plan_id matches plan-references' shape.
            Map<UUID, Map<UUID, List<DescriptionReferenceEntity>>> byRefByPlan = descriptionReferences.stream()
                    .filter(x -> x.getReferenceId() != null && x.getDescriptionId() != null)
                    .collect(Collectors.groupingBy(DescriptionReferenceEntity::getReferenceId, Collectors.groupingBy(x -> {
                        UUID planId = descriptionIdToPlanId.get(x.getDescriptionId());
                        return planId != null ? planId : DESCRIPTION_NO_BLUEPRINT_KEY;
                    })));
            for (var refEntry : byRefByPlan.entrySet()) {
                UUID refId = refEntry.getKey();
                for (var planEntry : refEntry.getValue().entrySet()) {
                    UUID planId = planEntry.getKey().equals(DESCRIPTION_NO_BLUEPRINT_KEY) ? null : planEntry.getKey();
                    Map<LocalDate, Long> dateGroup = planEntry.getValue().stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    for (var dateEntry : dateGroup.entrySet()) {
                        buffer.add(this.buildMap(dateEntry.getKey(), dateEntry.getValue(), tenantCode, KpiEntityType.Reference, MetricType.Overtime, referenceTypeId, null, null, null, null, null, planId, refId, null,null));
                        if (buffer.size() >= max) this.flushIndicatorPointBuffer(buffer);
                    }
                }
            }
        }
    }

    private void flushIndicatorPointBuffer(List<Map<String, Object>> buffer) {
        if (buffer == null || buffer.isEmpty()) return;
        IndicatorPointEvent event = new IndicatorPointEvent();
        if (kpiProperties.getIndicator() != null) event.setIndicatorId(this.kpiProperties.getIndicator().getId());
        event.setProperties(new ArrayList<>(buffer));
        this.validatorFactory.validator(IndicatorPointEvent.IndicatorPointEventValidator.class).validateForce(event);
        this.indicatorPointEventHandler.handle(event);
        buffer.clear();
    }

    @Override
    public void sendIndicatorPointPlanBlueprintCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._createdAt).ensure(PlanBlueprint._belongsToCurrentTenant).ensure(PlanBlueprint._versionStatus).ensure(PlanBlueprint._type));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<PlanBlueprintTypeEntity> planBlueprintTypes = this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanBlueprintType._id).ensure(PlanBlueprintType._belongsToCurrentTenant));

            List<PlanBlueprintEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null, null);

            List<PlanBlueprintEntity> currentVersionDefaultTenantItems = defaultTenantItems.stream().filter(x -> x.getVersionStatus().equals(PlanBlueprintVersionStatus.Current)).toList();
            var groupCurrentVersionItems = currentVersionDefaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupCurrentVersionItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, KpiVersionType.LatestVersion.asString(), null, null, null);


            for (TenantEntity tenant: tenants) {
                List<PlanBlueprintEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null, null);

                List<PlanBlueprintEntity> currentVersionTenantItems = tenantItems.stream().filter(x -> x.getVersionStatus().equals(PlanBlueprintVersionStatus.Current)).toList();
                var groupTenantCurrentVersionItems = currentVersionTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantCurrentVersionItems, tenant.getCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null, KpiVersionType.LatestVersion.asString(), null, null, null);
            }

            for (PlanBlueprintTypeEntity type: planBlueprintTypes){
                List<PlanBlueprintEntity> defaultBlueprintItems = items.stream().filter(x-> x.getTypeId() != null && x.getTypeId().equals(type.getId()) && x.getTenantId() == null ).toList();
                var _groupItems = defaultBlueprintItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(_groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.PlanBlueprintPerType, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null,  type.getId());

                for (TenantEntity tenant: tenants) {
                    List<PlanBlueprintEntity> tenantItems = items.stream().filter(x ->  x.getTypeId() != null && x.getTypeId().equals(type.getId()) && tenant.getId().equals(x.getTenantId())).toList();
                    var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.PlanBlueprintPerType, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null, type.getId());

                }

            }

        }
        finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<DescriptionTemplateEntity> items = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._createdAt).ensure(DescriptionTemplate._belongsToCurrentTenant).ensure(DescriptionTemplate._versionStatus).ensure(DescriptionTemplate._type));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<DescriptionTemplateTypeEntity> descriptionTemplateTypes = this.queryFactory.query(DescriptionTemplateTypeQuery.class).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplateType._id).ensure(DescriptionTemplateType._belongsToCurrentTenant));

            List<DescriptionTemplateEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null, null);

            List<DescriptionTemplateEntity> currentVersionDefaultTenantItems = defaultTenantItems.stream().filter(x -> x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
            var groupCurrentVersionItems = currentVersionDefaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupCurrentVersionItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, KpiVersionType.LatestVersion.asString(), null, null, null);

            for (TenantEntity tenant: tenants) {
                List<DescriptionTemplateEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null, null);

                List<DescriptionTemplateEntity> currentVersionTenantItems = tenantItems.stream().filter(x -> x.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)).toList();
                var groupTenantCurrentVersionItems = currentVersionTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantCurrentVersionItems, tenant.getCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null, KpiVersionType.LatestVersion.asString(), null, null, null);
            }

            for(DescriptionTemplateTypeEntity type: descriptionTemplateTypes)
            {
                List<DescriptionTemplateEntity> defaultTemplateItems = items.stream().filter(x-> x.getTypeId() != null && x.getTypeId().equals(type.getId()) && x.getTenantId() == null ).toList();
                var _groupItems = defaultTemplateItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(_groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.DescriptionTemplatePerType, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null,null,null,null,null,null, type.getId());

                for (TenantEntity tenant: tenants) {
                    List<DescriptionTemplateEntity> tenantItems = items.stream().filter(x ->  x.getTypeId() != null && x.getTypeId().equals(type.getId()) && tenant.getId().equals(x.getTenantId())).toList();
                    var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.DescriptionTemplatePerType, MetricType.Overtime, null, KpiVersionType.TotalCount.asString(), null, null,null,null,null,null,null, type.getId());
                }
            }
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointPlanBlueprintTypeCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            List<PlanBlueprintTypeEntity> items = this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanBlueprintType._id).ensure(PlanBlueprintType._createdAt).ensure(PlanBlueprintType._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<PlanBlueprintTypeEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.PlanBlueprintType, MetricType.Overtime, null, null, null, null, null);
            for (TenantEntity tenant: tenants) {
                List<PlanBlueprintTypeEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.PlanBlueprintType, MetricType.Overtime, null, null, null, null, null);
            }
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateTypeCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();

            List<DescriptionTemplateTypeEntity> items = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplateType._id).ensure(DescriptionTemplateType._createdAt).ensure(DescriptionTemplateType._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<DescriptionTemplateTypeEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.DescriptionTemplateType, MetricType.Overtime, null, null, null, null, null);
            for (TenantEntity tenant: tenants) {
                List<DescriptionTemplateTypeEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.DescriptionTemplateType, MetricType.Overtime, null, null, null, null, null);
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
            this.sendIndicatorPointEvents(groupItems, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), KpiEntityType.Tenant, MetricType.Overtime, null, null, null, null, null);

        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendDashboardTouchedEvents() throws InvalidApplicationException {
        DashboardDefaultsProperties defaults = this.kpiProperties.getDashboardDefaults();
        String indicatorId = (this.kpiProperties.getIndicator() != null && this.kpiProperties.getIndicator().getId() != null)
                ? this.kpiProperties.getIndicator().getId().toString() : "";
        String referenceTypeDefault = defaults != null ? nullToEmpty(defaults.getReferenceTypeId()) : "";
        String planStatusDefault = defaults != null ? nullToEmpty(defaults.getPlanStatusId()) : "";
        String descriptionStatusDefault = defaults != null ? nullToEmpty(defaults.getDescriptionStatusId()) : "";

        this.publishDashboard(defaults.getAdminDashboardInfo().getPath(), defaults.getAdminDashboardInfo().getKey(), defaults.getAdminDashboardInfo().getName(),
                indicatorId, referenceTypeDefault, planStatusDefault, descriptionStatusDefault);
        this.publishDashboard(defaults.getTenantDashboardInfo().getPath(), defaults.getTenantDashboardInfo().getKey(), defaults.getTenantDashboardInfo().getName(),
                indicatorId, referenceTypeDefault, planStatusDefault, descriptionStatusDefault);
    }

    private void publishDashboard(String resourcePath, String key, String name, String indicatorId, String referenceTypeDefault, String planStatusDefault, String descriptionStatusDefault) {
        String template;

        try {
            Resource resource = this.resourceLoader.getResource(resourcePath);
            if (!resource.isReadable()) return;
            try(InputStream in = resource.getInputStream()) {
                template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        catch (Exception ex) {
            logger.error("Failed reading dashboard template " + resourcePath + ": " + ex.getMessage());
            return;
        }
        String value = template
                .replace("{{indicator_id}}", indicatorId)
                .replace("{{reference_type_default}}", referenceTypeDefault)
                .replace("{{plan_status_default}}", planStatusDefault)
                .replace("{{description_status_default}}", descriptionStatusDefault);

        DashboardTouchedEvent event = new DashboardTouchedEvent();
        event.setKey(key);
        event.setName(name);
        event.setValue(value);
        event.setEntityType(DashboardTouchedEvent.ENTITY_TYPE_APPLICATION);
        event.setEntityId(null);

        this.validatorFactory.validator(DashboardTouchedEvent.DashboardTouchedEventValidator.class).validateForce(event);
        this.dashboardTouchedEventHandler.handle(event);
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private void sendIndicatorPointEvents(Map<LocalDate, Long> groupItems, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId, UUID planBlueprintTypeId) {
        this.sendIndicatorPointEvents(groupItems, tenantCode, entityType, metricType, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, null, null, null, null, planBlueprintTypeId, null);
    }

    private void sendIndicatorPointEvents(Map<LocalDate, Long> groupItems, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId, String planVisibility, UUID planBlueprintGroupId) {
        this.sendIndicatorPointEvents(groupItems, tenantCode, entityType, metricType, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, planVisibility, planBlueprintGroupId,null,null, null, null);
    }

    private void sendIndicatorPointEvents(Map<LocalDate, Long> groupItems, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId, String planVisibility, UUID planBlueprintGroupId, UUID planId, UUID referenceIdField, UUID planBlueprintTypeId, UUID descriptionTemplateTypeId ) {

        var dates = groupItems.keySet().stream().collect(Collectors.toList());

        if (!dates.isEmpty()) {

            IndicatorPointEvent event = new IndicatorPointEvent();
            if (kpiProperties.getIndicator() != null) event.setIndicatorId(this.kpiProperties.getIndicator().getId());

            dates.sort(Comparator.comparing(x -> x));

            List<Map<String, Object>> properties = new ArrayList<>();
            for (var date: dates) {

                Map<String, Object> map = this.buildMap(date, groupItems.get(date), tenantCode, entityType, metricType, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, planVisibility, planBlueprintGroupId, planId, referenceIdField,planBlueprintTypeId,descriptionTemplateTypeId);
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
            PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).isActive(IsActive.Active).firstAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._status).ensure(Plan._blueprint).ensure(Plan._accessType));
            if (planEntity != null ) {
                PlanBlueprintEntity planBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(planEntity.getBlueprintId()).firstAs(new BaseFieldSet().ensure(PlanBlueprint._groupId));
                if(planBlueprintEntity != null) {
                    String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode();
                    this.sendSingleIndicatorPointEvent(type, KpiEntityType.PlanPerBlueprint, tenantCode, null, null, planBlueprintEntity.getGroupId(), planEntity.getStatusId(), planVisibilityCode(planEntity.getAccessType()), null,null);
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
            DescriptionEntity descriptionEntity = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionId).isActive(IsActive.Active).firstAs(new BaseFieldSet().ensure(Description._id).ensure(Description._status).ensure(Description._descriptionTemplate).ensure(Description._plan));
            if (descriptionEntity != null ) {
                DescriptionTemplateEntity descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(descriptionEntity.getDescriptionTemplateId()).firstAs(new BaseFieldSet().ensure(DescriptionTemplate._groupId));
                if(descriptionTemplateEntity != null) {
                    UUID planBlueprintGroupId = null;
                    if (descriptionEntity.getPlanId() != null) {
                        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(descriptionEntity.getPlanId()).firstAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._blueprint));
                        if (planEntity != null && planEntity.getBlueprintId() != null) {
                            PlanBlueprintEntity planBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(planEntity.getBlueprintId()).firstAs(new BaseFieldSet().ensure(PlanBlueprint._groupId));
                            if (planBlueprintEntity != null) planBlueprintGroupId = planBlueprintEntity.getGroupId();
                        }
                    }
                    String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode();
                    this.sendSingleIndicatorPointEvent(type, KpiEntityType.DescriptionPerTemplate, tenantCode, null, null, descriptionTemplateEntity.getGroupId(), descriptionEntity.getStatusId(), null, planBlueprintGroupId, descriptionEntity.getPlanId(), null,null,null);
                }
            }
        }
        finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointReferenceEntry(KpiDirectionType type, UUID referenceId, UUID planId) throws InvalidApplicationException {
        if (type == null || referenceId == null) return;

        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            ReferenceEntity reference = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(referenceId).firstAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._type));
            if (reference != null ) {
                ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(reference.getTypeId()).firstAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceType._code));
                if (referenceTypeEntity != null) {
                    String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode();
                    this.sendSingleIndicatorPointEvent(type, KpiEntityType.Reference, tenantCode, referenceTypeEntity.getId(), null, null, null, null, null, planId, referenceId,null,null);
                }
            }
        }
        finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointReferenceBatchEntry(KpiDirectionType type, List<ReferenceIndicatorEntry> entries) throws InvalidApplicationException {
        if (type == null || entries == null || entries.isEmpty()) return;

        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();

            Set<UUID> uniqueRefIds = entries.stream().map(ReferenceIndicatorEntry::referenceId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (uniqueRefIds.isEmpty()) return;

            // Bulk-fetch references and types once instead of per-entry round-trips. Mirrors the single-event method's existence checks.
            List<ReferenceEntity> references = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(uniqueRefIds).collectAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._type));
            Map<UUID, UUID> refIdToTypeId = references.stream().filter(r -> r.getTypeId() != null).collect(Collectors.toMap(ReferenceEntity::getId, ReferenceEntity::getTypeId, (a, b) -> a));
            if (refIdToTypeId.isEmpty()) return;

            Set<UUID> uniqueTypeIds = new HashSet<>(refIdToTypeId.values());
            List<ReferenceTypeEntity> referenceTypes = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(uniqueTypeIds).collectAs(new BaseFieldSet().ensure(ReferenceType._id));
            Set<UUID> validTypeIds = referenceTypes.stream().map(ReferenceTypeEntity::getId).collect(Collectors.toSet());
            if (validTypeIds.isEmpty()) return;

            String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode();
            int sum = type.equals(KpiDirectionType.Increase) ? 1 : -1;
            LocalDate today = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC"));
            int max = this.kpiProperties.getMaxIndicatorPointsPerRequest();

            List<Map<String, Object>> buffer = new ArrayList<>();
            for (ReferenceIndicatorEntry entry : entries) {
                if (entry == null || entry.referenceId() == null) continue;
                UUID typeId = refIdToTypeId.get(entry.referenceId());
                if (typeId == null || !validTypeIds.contains(typeId)) continue;
                buffer.add(this.buildMap(today, sum, tenantCode, KpiEntityType.Reference, MetricType.Overtime, typeId, null, null, null, null, null, entry.planId(), entry.referenceId(),null, null));
                if (buffer.size() >= max) this.flushIndicatorPointBuffer(buffer);
            }
            this.flushIndicatorPointBuffer(buffer);
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
    }


    @Override
    public void sendIndicatorPointPlanBlueprintEntry(KpiDirectionType type, KpiVersionType versionType) throws InvalidApplicationException {
        if (type == null || versionType == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        KpiEntityType entityType = KpiEntityType.PlanBlueprint;

        if (versionType.equals(KpiVersionType.LatestVersion)) {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, KpiVersionType.LatestVersion.asString(), null, null);
        } else {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, KpiVersionType.TotalCount.asString(), null, null);
        }
    }

    @Override
    public void sendIndicatorPointPlanBlueprintPerTypeEntry(KpiDirectionType type, UUID planBlueprintTypeId) throws InvalidApplicationException {
        if (type == null || planBlueprintTypeId == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();

        this.sendSingleIndicatorPointEvent(type, KpiEntityType.PlanBlueprintPerType, tenantCode, null, null, null, null, null, null, planBlueprintTypeId);

    }

    @Override
    public void sendIndicatorPointDescriptionTemplatePerTypeEntry(KpiDirectionType type, UUID descriptionTemplateTypeId) throws InvalidApplicationException {
        if (type == null || descriptionTemplateTypeId == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();

        this.sendSingleIndicatorPointEvent(type, KpiEntityType.DescriptionTemplatePerType, tenantCode, null, null, null, null, null, null,null,null,null,descriptionTemplateTypeId);

    }

    @Override
    public void sendIndicatorPointPlanBlueprintTypeEntry(KpiDirectionType type) throws InvalidApplicationException {
        if(type == null) return;
        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        KpiEntityType entityType = KpiEntityType.PlanBlueprintType;
        this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, null, null, null);
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateTypeEntry(KpiDirectionType type) throws InvalidApplicationException {
        if(type == null) return;
        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        KpiEntityType entityType = KpiEntityType.DescriptionTemplateType;
        this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, null, null, null);
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType type, KpiVersionType versionType) throws InvalidApplicationException {
        if (type == null || versionType == null) return;

        String tenantCode = this.tenantScopeFactory.getInstance().isDefaultTenant() ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() :this.tenantScopeFactory.getInstance().getTenantCode();
        KpiEntityType entityType = KpiEntityType.DescriptionTemplate;

        if (versionType.equals(KpiVersionType.LatestVersion)) {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, KpiVersionType.LatestVersion.asString(), null, null);
        } else {
            this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, null, KpiVersionType.TotalCount.asString(), null, null);
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
        this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, null, null, null,null,null,null);
    }

    private void sendSingleIndicatorPointEvent(KpiDirectionType type, KpiEntityType entityType, String tenantCode, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId, String planVisibility, UUID planBlueprintGroupId , UUID planBlueprintTypeId) throws InvalidApplicationException {
        this.sendSingleIndicatorPointEvent(type, entityType, tenantCode, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, planVisibility, planBlueprintGroupId, null, null, planBlueprintTypeId,null);
    }

    private void sendSingleIndicatorPointEvent(KpiDirectionType type, KpiEntityType entityType, String tenantCode, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId, String planVisibility, UUID planBlueprintGroupId, UUID planId, UUID referenceIdField , UUID planBlueprintTypeId , UUID descriptionTemplateTypeId) throws InvalidApplicationException {

        int sum = type.equals(KpiDirectionType.Increase)? 1: -1;

        IndicatorPointEvent event = new IndicatorPointEvent();
        if (kpiProperties.getIndicator() != null) event.setIndicatorId(this.kpiProperties.getIndicator().getId());

        List<Map<String, Object>> properties = new ArrayList<>();
        Map<String, Object> map = this.buildMap(LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")), sum, tenantCode, entityType, MetricType.Overtime, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, planVisibility, planBlueprintGroupId, planId, referenceIdField,planBlueprintTypeId,descriptionTemplateTypeId);
        properties.add(map);
        event.setProperties(properties);

        this.validatorFactory.validator(IndicatorPointEvent.IndicatorPointEventValidator.class).validateForce(event);
        this.indicatorPointEventHandler.handle(event);

    }

    private Map<String, Object> buildMap(LocalDate date, long sum, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId) {
        return this.buildMap(date, sum, tenantCode, entityType, metricType, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, null, null, null, null,null, null);
    }

    private Map<String, Object> buildMap(LocalDate date, long sum, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId, String planVisibility, UUID planBlueprintGroupId, UUID planBlueprintTypeId) {
        return this.buildMap(date, sum, tenantCode, entityType, metricType, referenceTypeId, versionCode, blueprintOrTemplateGroupId, statusId, planVisibility, planBlueprintGroupId, null, null, planBlueprintTypeId, null);
    }

    private Map<String, Object> buildMap(LocalDate date, long sum, String tenantCode, KpiEntityType entityType, MetricType metricType, UUID referenceTypeId, String versionCode, UUID blueprintOrTemplateGroupId, UUID statusId, String planVisibility, UUID planBlueprintGroupId, UUID planId, UUID referenceIdField , UUID planBlueprintTypeId, UUID descriptionTemplateTypeId) {
        Map<String, Object> map = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        map.put(KpiSchemaFieldCodes.CreatedAt, date.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter));
        map.put(KpiSchemaFieldCodes.Value, sum);
        map.put(KpiSchemaFieldCodes.TenantCode, tenantCode);
        map.put(KpiSchemaFieldCodes.EntityType, entityType);
        map.put(KpiSchemaFieldCodes.MetricType, metricType);
        if (entityType.equals(KpiEntityType.Reference)) map.put(KpiSchemaFieldCodes.ReferenceTypeId, referenceTypeId);
        if (entityType.equals(KpiEntityType.Reference) && referenceIdField != null) map.put(KpiSchemaFieldCodes.ReferenceId, referenceIdField);
        if (entityType.equals(KpiEntityType.Reference) && planId != null) map.put(KpiSchemaFieldCodes.PlanId, planId);
        if (entityType.equals(KpiEntityType.PlanPerBlueprint) && blueprintOrTemplateGroupId != null) map.put(KpiSchemaFieldCodes.BlueprintGroupId, blueprintOrTemplateGroupId);
        if (entityType.equals(KpiEntityType.PlanPerBlueprint) && statusId != null) map.put(KpiSchemaFieldCodes.PlanStatus, statusId);
        if (entityType.equals(KpiEntityType.PlanPerBlueprint) && planVisibility != null) map.put(KpiSchemaFieldCodes.PlanVisibility, planVisibility);
        if (entityType.equals(KpiEntityType.DescriptionPerTemplate) && blueprintOrTemplateGroupId != null) map.put(KpiSchemaFieldCodes.TemplateGroupId, blueprintOrTemplateGroupId);
        if (entityType.equals(KpiEntityType.DescriptionPerTemplate) && statusId != null) map.put(KpiSchemaFieldCodes.DescriptionStatus, statusId);
        if (entityType.equals(KpiEntityType.DescriptionPerTemplate) && planBlueprintGroupId != null) map.put(KpiSchemaFieldCodes.PlanBlueprintId, planBlueprintGroupId);
        if (entityType.equals(KpiEntityType.DescriptionPerTemplate) && planId != null) map.put(KpiSchemaFieldCodes.PlanId, planId);
        if (entityType.equals(KpiEntityType.DescriptionTemplate)) map.put(KpiSchemaFieldCodes.LatestVersion, versionCode);
        if (entityType.equals(KpiEntityType.PlanBlueprint)) map.put(KpiSchemaFieldCodes.LatestVersion, versionCode);
        if (entityType.equals(KpiEntityType.PlanBlueprintPerType)) map.put(KpiSchemaFieldCodes.PlanBlueprintTypeId, planBlueprintTypeId);
        if (entityType.equals(KpiEntityType.DescriptionTemplatePerType)) map.put(KpiSchemaFieldCodes.DescriptionTemplateTypeId, descriptionTemplateTypeId);


        return map;
    }

    private static String planVisibilityCode(PlanAccessType accessType) {
        if (accessType == null) return null;
        return accessType.name().toLowerCase();
    }
}

