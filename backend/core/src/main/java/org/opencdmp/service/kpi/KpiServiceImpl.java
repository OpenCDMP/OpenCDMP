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
import org.opencdmp.integrationevent.outbox.indicator.IndicatorElasticEvent;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorField;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorMetadata;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorSchema;
import org.opencdmp.integrationevent.outbox.indicatoraccess.FilterColumnConfig;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessConfig;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEvent;
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
                    this.indicatorAccessEventHandler.handle(this.createIndicatorAccessEvent(user.getId(), this.tenantScope.getDefaultTenantCode()), null);
                }
                List<TenantUserEntity> tenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().userIds(users.stream().map(UserEntity::getId).distinct().toList()).isActive(IsActive.Active).collect();
                if (tenantUserEntities == null || tenantUserEntities.isEmpty()) return;
                List<TenantEntity> tenantEntities = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).ids(tenantUserEntities.stream().map(TenantUserEntity::getTenantId).distinct().toList()).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
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
        event.setIndicatorId(this.kpiProperties.getIndicator().getId());
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
            List<ReferenceEntity> items = this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._type).ensure(Reference._createdAt).ensure(Reference._belongsToCurrentTenant));
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
            List<ReferenceTypeEntity> referenceTypes = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceType._code));

            for (ReferenceTypeEntity referenceTypeEntity: referenceTypes) {
                List<ReferenceEntity> defaultTenantItems = items.stream().filter(x -> x.getTenantId() == null && x.getTypeId().equals(referenceTypeEntity.getId())).toList();
                if (!this.conventionService.isListNullOrEmpty(defaultTenantItems)) {
                    var groupItems = defaultTenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    this.sendIndicatorPointEvents(groupItems, this.tenantScope.getDefaultTenantCode(), KpiEntityType.Reference, MetricType.Overtime, referenceTypeEntity.getCode());
                }

            }

            for (TenantEntity tenant: tenants) {
                for (ReferenceTypeEntity referenceTypeEntity: referenceTypes) {
                    List<ReferenceEntity> tenantItems = items.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId()) && x.getTypeId().equals(referenceTypeEntity.getId())).toList();
                    var groupTenantItems = tenantItems.stream().collect(Collectors.groupingBy(x -> LocalDate.ofInstant(x.getCreatedAt(), ZoneId.of("UTC")), Collectors.counting()));
                    this.sendIndicatorPointEvents(groupTenantItems, tenant.getCode(), KpiEntityType.Reference, MetricType.Overtime, referenceTypeEntity.getCode());
                }

            }
        } finally {
            this.tenantEntityManager.reloadTenantFilters();
        }
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

    private void sendIndicatorPointEvents(Map<LocalDate, Long> groupItems, String tenantCode, KpiEntityType entityType, MetricType metricType, String referenceTypeCode) {

        var dates = groupItems.keySet().stream().collect(Collectors.toList());
        dates.sort(Comparator.comparing(x -> x));
        int sum = 0;
        for (var date: dates) {
            sum += groupItems.get(date);

            IndicatorPointEvent event = new IndicatorPointEvent();
            if (kpiProperties.getIndicator() != null) event.setIndicatorId(this.kpiProperties.getIndicator().getId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

            event.add(KpiSchemaFieldCodes.CreatedAt, date.atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter));
            event.add(KpiSchemaFieldCodes.Value, sum);
            event.add(KpiSchemaFieldCodes.TenantCode, tenantCode);
            event.add(KpiSchemaFieldCodes.EntityType, entityType);
            event.add(KpiSchemaFieldCodes.MetricType, metricType);
            if (entityType.equals(KpiEntityType.Reference)) event.add(KpiSchemaFieldCodes.ReferenceTypeCode, referenceTypeCode);

            this.validatorFactory.validator(IndicatorPointEvent.IndicatorPointEventValidator.class).validateForce(event);
            this.indicatorPointEventHandler.handle(event);
        }

    }
}

