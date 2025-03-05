package org.opencdmp.service.metrics;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.Gauge;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.metrics.MetricLabels;
import org.opencdmp.commons.metrics.MetricNames;
import org.opencdmp.data.PlanDescriptionTemplateEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.query.*;
import org.opencdmp.service.keycloak.MyKeycloakAdminRestApi;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final PrometheusMeterRegistry registry;

    private final QueryFactory queryFactory;

    private final TenantEntityManager entityManager;

    private final MyKeycloakAdminRestApi keycloakAdminRestApi;

    private final UpdateMetricsTaskProperties _config;


    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(MetricsServiceImpl.class));

    public MetricsServiceImpl(
            PrometheusMeterRegistry registry,
            QueryFactory queryFactory,
            TenantEntityManager entityManager,
            MyKeycloakAdminRestApi keycloakAdminRestApi,
            UpdateMetricsTaskProperties config) {
        this.registry = registry;
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
        this.keycloakAdminRestApi = keycloakAdminRestApi;
        this._config = config;
    }

    @Override
    public void calculate(Map<String, Gauge> gauges) throws InvalidApplicationException {
        try {
            this.entityManager.disableTenantFilters();

            try {
                this.setGaugeValue(gauges, MetricNames.DMP, this.calculateDraftDmps(false), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DMP, this.calculateFinalizedDmps(false), MetricLabels.FINALIZED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DMP, this.calculatePublishedDmps(false), MetricLabels.PUBLISHED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DMP, this.calculateDoiedDmps(false), MetricLabels.DOIED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP, this.calculateDraftDmps(true), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP, this.calculateFinalizedDmps(true), MetricLabels.FINALIZED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP, this.calculatePublishedDmps(true), MetricLabels.PUBLISHED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP, this.calculateDoiedDmps(true), MetricLabels.DOIED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.DMP_WITH_GRANT, this.calculateDraftDmpsWithGrant(false), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DMP_WITH_GRANT, this.calculateFinalizedDmpsWithGrant(false), MetricLabels.FINALIZED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DMP_WITH_GRANT, this.calculatePublishedDmpsWithGrant(false), MetricLabels.PUBLISHED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DMP_WITH_GRANT, this.calculateDoiedDmpsWithGrant(false), MetricLabels.DOIED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP_WITH_GRANT, this.calculateDraftDmpsWithGrant(true), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP_WITH_GRANT, this.calculateFinalizedDmpsWithGrant(true), MetricLabels.FINALIZED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP_WITH_GRANT, this.calculatePublishedDmpsWithGrant(true), MetricLabels.PUBLISHED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DMP_WITH_GRANT, this.calculateDoiedDmpsWithGrant(true), MetricLabels.DOIED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.FUNDERS, this.calculateAllFunders(false), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.GRANTS, this.calculateAllGrants(false), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.PROJECTS, this.calculateAllProjects(false), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.RESEARCHERS, this.calculateAllResearchers(false), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.FUNDERS, this.calculateAllFunders(true), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.GRANTS, this.calculateAllGrants(true), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.PROJECTS, this.calculateAllProjects(true), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.RESEARCHERS, this.calculateAllResearchers(true), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.DATASET, this.calculateDraftDatasets(false), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DATASET, this.calculateFinalizedDatasets(false), MetricLabels.FINALIZED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DATASET, this.calculatePublishedDatasets(false), MetricLabels.PUBLISHED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DATASET, this.calculateDoiedDatasets(false), MetricLabels.DOIED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DATASET, this.calculateDraftDatasets(true), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DATASET, this.calculateFinalizedDatasets(true), MetricLabels.FINALIZED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DATASET, this.calculatePublishedDatasets(true), MetricLabels.PUBLISHED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DATASET, this.calculateDoiedDatasets(true), MetricLabels.DOIED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.DATASET_TEMPLATE, this.calculateDraftTemplates(false), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DATASET_TEMPLATE, this.calculateFinalizedTemplates(false), MetricLabels.ACTIVE);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.DATASET_TEMPLATE, this.calculateUsedTemplates(false), MetricLabels.USED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DATASET_TEMPLATE, this.calculateDraftTemplates(true), MetricLabels.DRAFT);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DATASET_TEMPLATE, this.calculateFinalizedTemplates(true), MetricLabels.ACTIVE);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.DATASET_TEMPLATE, this.calculateUsedTemplates(true), MetricLabels.USED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.LANGUAGES, this.calculateLanguages(), null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.INSTALLATIONS, 1d, null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.NEXUS_PREFIX + MetricNames.INSTALLATIONS, 1d, null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                this.setGaugeValue(gauges, MetricNames.USERS, this.calculateActiveUsers(), MetricLabels.LOGGEDIN);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                this.setGaugeValue(gauges, MetricNames.USERS, this.calculateAllUsers(), MetricLabels.TOTAL);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            this.entityManager.reloadTenantFilters();
        }
    }

    @Override
    public Map<String, Gauge> gaugesBuild() {
	    this.registry.clear();

        return Stream.of(new Object[][]{
                {MetricNames.DMP, Gauge.build().name(MetricNames.DMP).help("Number of managed DMPs").labelNames("status").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.DMP, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.DMP).help("Number of managed DMPs during Nexus").labelNames("status").register(this.registry.getPrometheusRegistry())},

                {MetricNames.FUNDERS, Gauge.build().name(MetricNames.FUNDERS).help("Number of registered Funders").register(this.registry.getPrometheusRegistry())},
                {MetricNames.GRANTS, Gauge.build().name(MetricNames.GRANTS).help("Number of registered Grants").register(this.registry.getPrometheusRegistry())},
                {MetricNames.PROJECTS, Gauge.build().name(MetricNames.PROJECTS).help("Number of registered Projects").register(this.registry.getPrometheusRegistry())},
                {MetricNames.RESEARCHERS, Gauge.build().name(MetricNames.RESEARCHERS).help("Number of Collaborators/Researchers").register(this.registry.getPrometheusRegistry())},

                {MetricNames.NEXUS_PREFIX + MetricNames.FUNDERS, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.FUNDERS).help("Number of registered Funders during Nexus").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.GRANTS, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.GRANTS).help("Number of registered Grants during Nexus").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.PROJECTS, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.PROJECTS).help("Number of registered Projects during Nexus").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.RESEARCHERS, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.RESEARCHERS).help("Number of Collaborators/Researchers during Nexus").register(this.registry.getPrometheusRegistry())},

                {MetricNames.DATASET, Gauge.build().name(MetricNames.DATASET).help("Number of managed Dataset Descriptions").labelNames("status").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.DATASET, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.DATASET).help("Number of managed Dataset Descriptions during Nexus").labelNames("status").register(this.registry.getPrometheusRegistry())},

                {MetricNames.DATASET_TEMPLATE, Gauge.build().name(MetricNames.DATASET_TEMPLATE).help("Number of dataset Templates").labelNames("status").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.DATASET_TEMPLATE, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.DATASET_TEMPLATE).help("Number of dataset Templates during Nexus").labelNames("status").register(this.registry.getPrometheusRegistry())},

                {MetricNames.USERS, Gauge.build().name(MetricNames.USERS).help("Number of users").labelNames("type").register(this.registry.getPrometheusRegistry())},

                {MetricNames.LANGUAGES, Gauge.build().name(MetricNames.LANGUAGES).help("Number of Languages").register(this.registry.getPrometheusRegistry())},

                {MetricNames.DMP_WITH_GRANT, Gauge.build().name(MetricNames.DMP_WITH_GRANT).help("Number of Grants based on the status of the DMP that is using them").labelNames("status").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.DMP_WITH_GRANT, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.DMP_WITH_GRANT).help("Number of Grants based on the status of the DMP that is using them during Nexus").labelNames("status").register(this.registry.getPrometheusRegistry())},

                {MetricNames.INSTALLATIONS, Gauge.build().name(MetricNames.INSTALLATIONS).help("Number of Installations").register(this.registry.getPrometheusRegistry())},
                {MetricNames.NEXUS_PREFIX + MetricNames.INSTALLATIONS, Gauge.build().name(MetricNames.NEXUS_PREFIX + MetricNames.INSTALLATIONS).help("Number of Installations").register(this.registry.getPrometheusRegistry())},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Gauge) data[1]));
    }

    private double calculateDraftDmps(boolean forNexus) {
        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Draft).isActives(IsActive.Active);
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().planStatusSubQuery(statusQuery).isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        return planQuery.count();
    }

    private double calculateFinalizedDmps(boolean forNexus) {
        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().planStatusSubQuery(statusQuery).isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        return planQuery.count();
    }

    private double calculatePublishedDmps(boolean forNexus) {
        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public).isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        return planQuery.count();
    }

    private double calculateDoiedDmps(boolean forNexus) {
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().types(EntityType.Plan).isActive(IsActive.Active);
        planQuery.entityDoiSubQuery(entityDoiQuery);
        planQuery.setDistinct(true);
        return planQuery.count();
    }

    private double calculateDraftDmpsWithGrant(boolean forNexus)
    {
        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Draft).isActives(IsActive.Active);
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).planStatusSubQuery(statusQuery).disableTracking().isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getGrantIds()).isActive(IsActive.Active);
        PlanReferenceQuery planReferenceQuery = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().referenceSubQuery(referenceQuery).isActives(IsActive.Active);
        planQuery.planReferenceSubQuery(planReferenceQuery);
        return planQuery.count();
    }

    private double calculateFinalizedDmpsWithGrant(boolean forNexus) {
        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().planStatusSubQuery(statusQuery).isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getGrantIds()).isActive(IsActive.Active);
        PlanReferenceQuery planReferenceQuery = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().referenceSubQuery(referenceQuery).isActives(IsActive.Active);
        planQuery.planReferenceSubQuery(planReferenceQuery);
        return planQuery.count();
    }

    private double calculatePublishedDmpsWithGrant(boolean forNexus) {
        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).planStatusSubQuery(statusQuery).disableTracking().accessTypes(PlanAccessType.Public).isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getGrantIds()).isActive(IsActive.Active);
        PlanReferenceQuery planReferenceQuery = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().referenceSubQuery(referenceQuery).isActives(IsActive.Active);
        planQuery.planReferenceSubQuery(planReferenceQuery);
        return planQuery.count();
    }

    private double calculateDoiedDmpsWithGrant(boolean forNexus) {
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active);
        if (forNexus)
            planQuery.after(this._config.getNexusDate());
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getGrantIds()).isActive(IsActive.Active);
        PlanReferenceQuery planReferenceQuery = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().referenceSubQuery(referenceQuery).isActives(IsActive.Active);
        planQuery.planReferenceSubQuery(planReferenceQuery);
        EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().isActive(IsActive.Active);
        planQuery.entityDoiSubQuery(entityDoiQuery);
        return planQuery.count();
    }

    private double calculateAllFunders(boolean forNexus) {
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getFunderIds()).isActive(IsActive.Active);
        if (forNexus)
            referenceQuery.after(this._config.getNexusDate());
        return referenceQuery.count();
    }

    private double calculateAllGrants(boolean forNexus) {
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getGrantIds()).isActive(IsActive.Active);
        if (forNexus)
            referenceQuery.after(this._config.getNexusDate());
        return referenceQuery.count();
    }

    private double calculateAllProjects(boolean forNexus) {
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getProjectIds()).isActive(IsActive.Active);
        if (forNexus)
            referenceQuery.after(this._config.getNexusDate());
        return referenceQuery.count();
    }

    private double calculateAllResearchers(boolean forNexus) {
        ReferenceQuery referenceQuery = this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(this._config.getReferenceTypes().getResearcherIds()).isActive(IsActive.Active);
        if (forNexus)
            referenceQuery.after(this._config.getNexusDate());
        return referenceQuery.count();
    }

    private double calculateDraftDatasets(boolean forNexus) {
        DescriptionStatusQuery descriptionStatusQuery = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().internalStatuses(DescriptionStatus.Draft).isActive(IsActive.Active);
        DescriptionQuery descriptionQuery = this.queryFactory.query(DescriptionQuery.class).disableTracking().descriptionStatusSubQuery(descriptionStatusQuery).isActive(IsActive.Active);
        if (forNexus)
            descriptionQuery.createdAfter(this._config.getNexusDate());
        return descriptionQuery.count();
    }

    private double calculateFinalizedDatasets(boolean forNexus) {
        DescriptionStatusQuery descriptionStatusQuery = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().internalStatuses(DescriptionStatus.Finalized).isActive(IsActive.Active);
        DescriptionQuery descriptionQuery = this.queryFactory.query(DescriptionQuery.class).disableTracking().descriptionStatusSubQuery(descriptionStatusQuery).isActive(IsActive.Active);
        if (forNexus)
            descriptionQuery.createdAfter(this._config.getNexusDate());
        return descriptionQuery.count();
    }

    private double calculatePublishedDatasets(boolean forNexus) {
        DescriptionStatusQuery descriptionStatusQuery = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().internalStatuses(DescriptionStatus.Finalized).isActive(IsActive.Active);
        DescriptionQuery descriptionQuery = this.queryFactory.query(DescriptionQuery.class).disableTracking().descriptionStatusSubQuery(descriptionStatusQuery).isActive(IsActive.Active);
        if (forNexus)
            descriptionQuery.createdAfter(this._config.getNexusDate());
        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).disableTracking().planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public);
        descriptionQuery.planSubQuery(planQuery);
        return descriptionQuery.count();
    }

    private double calculateDoiedDatasets(boolean forNexus) {
        DescriptionStatusQuery descriptionStatusQuery = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().internalStatuses(DescriptionStatus.Finalized).isActive(IsActive.Active);
        DescriptionQuery descriptionQuery = this.queryFactory.query(DescriptionQuery.class).disableTracking().descriptionStatusSubQuery(descriptionStatusQuery).isActive(IsActive.Active);
        if (forNexus)
            descriptionQuery.createdAfter(this._config.getNexusDate());
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active);
        EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).types(EntityType.Plan).isActive(IsActive.Active);
        planQuery.entityDoiSubQuery(entityDoiQuery);
        descriptionQuery.planSubQuery(planQuery);
        descriptionQuery.setDistinct(true);
        return descriptionQuery.count();
    }

    private double calculateDraftTemplates(boolean forNexus) {
        DescriptionTemplateQuery descriptionTemplateQuery = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().statuses(DescriptionTemplateStatus.Draft).isActive(IsActive.Active);
        if (forNexus)
            descriptionTemplateQuery.after(this._config.getNexusDate());
        return descriptionTemplateQuery.count();
    }

    private double calculateFinalizedTemplates(boolean forNexus) {
        DescriptionTemplateQuery descriptionTemplateQuery = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().statuses(DescriptionTemplateStatus.Finalized).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active);
        if (forNexus)
            descriptionTemplateQuery.after(this._config.getNexusDate());
        return descriptionTemplateQuery.count();
    }

    private double calculateUsedTemplates(boolean forNexus) {
        PlanDescriptionTemplateQuery planDescriptionTemplateQuery = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active);
        planDescriptionTemplateQuery.setDistinct(true);
        if (forNexus) {
            DescriptionTemplateQuery descriptionTemplateQuery = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).after(this._config.getNexusDate());
            planDescriptionTemplateQuery.descriptionTemplateSubQuery(descriptionTemplateQuery);
        }
        return planDescriptionTemplateQuery.collectAs(new BaseFieldSet().ensure(PlanDescriptionTemplateEntity._descriptionTemplateGroupId)).size();
    }

    private double calculateActiveUsers() {
        return this.keycloakAdminRestApi.users().getUserSessionsCountByClientId(this._config.getUsersLoginClient());
    }

    private double calculateAllUsers() {
        UserQuery userQuery = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active);
        return userQuery.count();
    }

    private double calculateLanguages() {
        LanguageQuery languageQuery = this.queryFactory.query(LanguageQuery.class).disableTracking().isActive(IsActive.Active);
        return languageQuery.count();
    }

    private void setGaugeValue(Map<String, Gauge> gauges, String name, Double amount, String label) {
        if (label != null) {
            gauges.get(name).labels(label).set(amount);
        } else {
            gauges.get(name).set(amount);
        }
    }
}
