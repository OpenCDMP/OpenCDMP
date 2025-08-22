package org.opencdmp.service.kpi;


import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.kpi.KpiEntityType;
import org.opencdmp.commons.enums.kpi.MetricType;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.outbox.indicator.*;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEvent;
import org.opencdmp.integrationevent.outbox.indicatorpoint.IndicatorPointEventHandler;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEvent;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.query.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final TenantEntityManager tenantEntityManager;
    private final QueryFactory queryFactory;
    private final TenantScope tenantScope;


    @Autowired
    public KpiServiceImpl(
            KpiProperties kpiProperties, ValidatorFactory validatorFactory, ConventionService conventionService, IndicatorPointEventHandler indicatorPointEventHandler, IndicatorAccessEventHandler indicatorAccessEventHandler, TenantEntityManager tenantEntityManager, QueryFactory queryFactory, TenantScope tenantScope) {
        this.kpiProperties = kpiProperties;
        this.validatorFactory = validatorFactory;
        this.conventionService = conventionService;
        this.indicatorPointEventHandler = indicatorPointEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.tenantEntityManager = tenantEntityManager;
        this.queryFactory = queryFactory;
        this.tenantScope = tenantScope;
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
            this.tenantEntityManager.disableTenantFilters();
            List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).userRoleSubQuery(userRoleQuery).collectAs(new BaseFieldSet().ensure(User._id).ensure(User._name).ensure(User._createdAt));

            if (users != null && !users.isEmpty()) {
                for (UserEntity user: users) {
                    this.indicatorAccessEventHandler.handle(user.getId());
                }
            }

        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointUserCountEntryEvents() throws InvalidApplicationException {

        try {
            this.tenantEntityManager.disableTenantFilters();
            // default tenant
            List<UserEntity> items = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(User._id).ensure(User._createdAt));
            var groupItems = items.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScope.getDefaultTenantCode(), KpiEntityType.User, MetricType.Overtime, null);

            // specific tenants
            List<TenantUserEntity> allTenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().isActive(IsActive.Active).collect();
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            for (TenantEntity tenant: tenants) {
                List<TenantUserEntity> tenantUserEntities = allTenantUserEntities.stream().filter(x -> x.getTenantId().equals(tenant.getId())).toList();
                var groupTenantItems = tenantUserEntities.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.User, MetricType.Overtime, null);
            }

        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointPlanCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PlanEntity> items = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._createdAt).ensure(Plan._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<PlanEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScope.getDefaultTenantCode(), KpiEntityType.Plan, MetricType.Overtime, null);

            for (TenantEntity tenant: tenants) {
                List<PlanEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.Plan, MetricType.Overtime, null);
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointDescriptionCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManager.disableTenantFilters();
            List<DescriptionEntity> items = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._createdAt).ensure(Description._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<DescriptionEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScope.getDefaultTenantCode(), KpiEntityType.Description, MetricType.Overtime, null);

            for (TenantEntity tenant: tenants) {
                List<DescriptionEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.Description, MetricType.Overtime, null);
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointReferenceCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PlanReferenceEntity> planReferences = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().isActives(IsActive.Active).collect();
            List<DescriptionReferenceEntity> descriptionReferences = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().isActive(IsActive.Active).collect();
            List<ReferenceEntity> references = this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._type).ensure(Reference._createdAt).ensure(Reference._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<ReferenceTypeEntity> referenceTypes = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceType._code));

            for (ReferenceTypeEntity referenceTypeEntity: referenceTypes) {
                List<UUID> defaultTenantReferencesIds = references.stream().filter(x -> x.getTenantId() == null && x.getTypeId().equals(referenceTypeEntity.getId())).toList().stream().map(ReferenceEntity::getId).toList();
                List<PlanReferenceEntity> defaultPlanReferences = planReferences.stream().filter(x -> x.getTenantId() == null && defaultTenantReferencesIds.contains(x.getReferenceId())).toList();
                List<DescriptionReferenceEntity> defaultDescriptionReferences = descriptionReferences.stream().filter(x -> x.getTenantId() == null && defaultTenantReferencesIds.contains(x.getReferenceId())).toList();

                this.calculateAndSendReferenceIndicatorPoints(defaultPlanReferences, defaultDescriptionReferences, this.tenantScope.getDefaultTenantCode(), referenceTypeEntity.getCode());

            }

            for (TenantEntity tenant: tenants) {
                for (ReferenceTypeEntity referenceTypeEntity: referenceTypes) {

                    List<PlanReferenceEntity> tenantPlanReferences = planReferences.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).toList();
                    List<DescriptionReferenceEntity> tenantDescriptionReferences = descriptionReferences.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).toList();

                    this.calculateAndSendReferenceIndicatorPoints(tenantPlanReferences, tenantDescriptionReferences, tenant.getCode(), referenceTypeEntity.getCode());
                }

            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    private void calculateAndSendReferenceIndicatorPoints(List<PlanReferenceEntity> planReferences, List<DescriptionReferenceEntity> descriptionReferences, String tenantCode, String referenceTypeCode) {
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

        this.sendIndicatorPointEvents(finalGroupItems, tenantCode, KpiEntityType.Reference, MetricType.Overtime, referenceTypeCode);
    }

    @Override
    public void sendIndicatorPointPlanBlueprintCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManager.disableTenantFilters();
            List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._createdAt).ensure(PlanBlueprint._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<PlanBlueprintEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScope.getDefaultTenantCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null);

            for (TenantEntity tenant: tenants) {
                List<PlanBlueprintEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.PlanBlueprint, MetricType.Overtime, null);
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointDescriptionTemplateCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManager.disableTenantFilters();
            List<DescriptionTemplateEntity> items = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._createdAt).ensure(DescriptionTemplate._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));

            List<DescriptionTemplateEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null).toList();
            var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScope.getDefaultTenantCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null);

            for (TenantEntity tenant: tenants) {
                List<DescriptionTemplateEntity> tenantItems = items.stream().filter(x -> tenant.getId().equals(x.getTenantId())).toList();
                var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.DescriptionTemplate, MetricType.Overtime, null);
            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    @Override
    public void sendIndicatorPointTenantCountEntryEvents() throws InvalidApplicationException {
        try {
            this.tenantEntityManager.disableTenantFilters();
            List<TenantEntity> items = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._createdAt).ensure(Tenant._code));
            var groupItems = items.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
            this.sendIndicatorPointEvents(groupItems, this.tenantScope.getDefaultTenantCode(), KpiEntityType.Tenant, MetricType.Overtime, null);

        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
    }

    private void sendIndicatorPointEvents(Map<LocalDate, Long> groupItems, String tenantCode, KpiEntityType entityType, MetricType metricType, String referenceTypeCode) {

        var dates = groupItems.keySet().stream().collect(Collectors.toList());

        if (!dates.isEmpty()) {

            IndicatorPointEvent event = new IndicatorPointEvent();
            if (kpiProperties.getIndicator() != null) event.setIndicatorId(this.kpiProperties.getIndicator().getId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

            dates.sort(Comparator.comparing(x -> x));
            int sum = 0;

            List<Map<String, Object>> properties = new ArrayList<>();
            for (var date: dates) {
                sum += groupItems.get(date);

                Map<String, Object> map = new HashMap<>();
                map.put(KpiSchemaFieldCodes.CreatedAt, date.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter));
                map.put(KpiSchemaFieldCodes.Value, sum);
                map.put(KpiSchemaFieldCodes.TenantCode, tenantCode);
                map.put(KpiSchemaFieldCodes.EntityType, entityType);
                map.put(KpiSchemaFieldCodes.MetricType, metricType);
                if (entityType.equals(KpiEntityType.Reference)) map.put(KpiSchemaFieldCodes.ReferenceTypeCode, referenceTypeCode);
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
}

