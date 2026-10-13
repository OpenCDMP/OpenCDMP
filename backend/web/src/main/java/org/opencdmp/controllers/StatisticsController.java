package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.commons.types.StatisticsNameResult;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.PlanBlueprintType;
import org.opencdmp.model.builder.statistics.StatisticsBuilder;
import org.opencdmp.model.censorship.StatisticsNameQueryResponseCensor;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.*;
import org.opencdmp.query.lookup.StatisticsNameQueryLookup;
import org.opencdmp.model.StatisticsNameQueryResponse;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InvalidApplicationException;
import java.util.List;


@RestController
@RequestMapping("api/statistics")
public class StatisticsController {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StatisticsBuilder.class));
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final AuditService auditService;
    private final CensorFactory censorFactory;
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;

    public StatisticsController(TenantEntityManagerFactory tenantEntityManagerFactory, AuditService auditService, CensorFactory censorFactory, QueryFactory queryFactory, BuilderFactory builderFactory) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.auditService = auditService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    @PostMapping("plan-status/names")
    public List<StatisticsNameQueryResponse> queryPlanStatusNames(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(PlanStatus._id).ensure(PlanStatus._name);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            PlanStatusQuery query = queryFactory.query(PlanStatusQuery.class).disableTracking().ids(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getId(), x.getName())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }

        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("description-status/names")
    public List<StatisticsNameQueryResponse> queryDescriptionStatusNames(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._name);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            DescriptionStatusQuery query = queryFactory.query(DescriptionStatusQuery.class).disableTracking().ids(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getId(), x.getName())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }

        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("reference-type/names")
    public List<StatisticsNameQueryResponse> queryReferenceTypeNames(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceType._name);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            ReferenceTypeQuery query = queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getId(), x.getName())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }

        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("plan-blueprint-type/names")
    public List<StatisticsNameQueryResponse> queryPlanBlueprintTypeNames(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(PlanBlueprintType._id).ensure(PlanBlueprintType._name);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            PlanBlueprintTypeQuery query = queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().ids(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getId(), x.getName())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("description-template-type/names")
    public List<StatisticsNameQueryResponse> queryDescriptionTemplateTypeNames(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(DescriptionTemplateType._id).ensure(DescriptionTemplateType._name);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            DescriptionTemplateTypeQuery query = queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().ids(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getId(), x.getName())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("blueprint-group/names")
    public List<StatisticsNameQueryResponse> queryBlueprintGroupLabels(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanStatus._name).ensure(PlanBlueprint._groupId).ensure(PlanBlueprint._label);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            PlanBlueprintQuery query = queryFactory.query(PlanBlueprintQuery.class).disableTracking().groupIds(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getGroupId(), x.getLabel())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("template-group/names")
    public List<StatisticsNameQueryResponse> queryTemplateGroupLabels(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(DescriptionTemplate._id)
                    .ensure(DescriptionStatus._name)
                    //.ensure(PlanStatus._name)
                    .ensure(DescriptionTemplate._groupId).ensure(DescriptionTemplate._label);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            DescriptionTemplateQuery query = queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getGroupId(), x.getLabel())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("plan/names")
    public List<StatisticsNameQueryResponse> queryPlanLabels(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(Plan._id).ensure(Plan._label);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            PlanQuery query = queryFactory.query(PlanQuery.class).disableTracking().ids(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet)
                    .stream().map(x -> new StatisticsNameResult(x.getId(), x.getLabel())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }

    @PostMapping("reference/names")
    public List<StatisticsNameQueryResponse> queryReferenceLabels(@RequestBody StatisticsNameQueryLookup statisticsNameQueryLookup) throws InvalidApplicationException {
        logger.debug("querying {}", StatisticsController.class.getSimpleName());

        this.censorFactory.censor(StatisticsNameQueryResponseCensor.class).censor(statisticsNameQueryLookup.getProject(), null);

        List<StatisticsNameResult> queryResults = List.of();
        try {
            FieldSet fieldSet = new BaseFieldSet().ensure(Reference._id).ensure(Reference._label);

            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            ReferenceQuery query = queryFactory.query(ReferenceQuery.class).disableTracking().ids(statisticsNameQueryLookup.getIds());
            queryResults = query.collectAs(fieldSet).stream().map(x -> new StatisticsNameResult(x.getId(), x.getLabel())).toList();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }
        this.auditService.track(AuditableAction.Statistics_Query, "lookup", statisticsNameQueryLookup);
        return this.builderFactory
                .builder(StatisticsBuilder.class)
                .build(statisticsNameQueryLookup.getProject(), queryResults);
    }


}

