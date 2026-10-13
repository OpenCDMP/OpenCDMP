package org.opencdmp.controllers;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import jakarta.transaction.Transactional;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.Permission;
import org.opencdmp.model.maintenance.DatabaseMigrationInfo;
import org.opencdmp.model.maintenance.InstallationInfo;
import org.opencdmp.model.maintenance.MaintenanceRun;
import org.opencdmp.model.maintenance.MaintenanceTaskRun;
import org.opencdmp.model.persist.InstallationPersist;
import org.opencdmp.model.persist.MaintenanceRunPersist;
import org.opencdmp.service.elastic.ElasticService;
import org.opencdmp.service.maintenance.DatabaseMigrationService;
import org.opencdmp.service.maintenance.InstallationService;
import org.opencdmp.service.maintenance.MaintenanceRunService;
import org.opencdmp.service.maintenance.MaintenanceService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InvalidApplicationException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/maintenance")
public class MaintenanceController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(MaintenanceController.class));

    private final AuthorizationService authorizationService;

    private final ElasticService elasticService;

    private final AuditService auditService;

    private final MaintenanceService maintenanceService;

    private final MaintenanceRunService maintenanceRunService;

    private final DatabaseMigrationService databaseMigrationService;

    private final InstallationService installationService;
    @Autowired
    public MaintenanceController(
		    AuthorizationService authorizationService,
		    ElasticService elasticService,
		    AuditService auditService,
            MaintenanceService maintenanceService,
            MaintenanceRunService maintenanceRunService,
            DatabaseMigrationService databaseMigrationService,
            InstallationService installationService) {
        this.authorizationService = authorizationService;
        this.elasticService = elasticService;
        this.auditService = auditService;
	    this.maintenanceService = maintenanceService;
        this.maintenanceRunService = maintenanceRunService;
        this.databaseMigrationService = databaseMigrationService;
        this.installationService = installationService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/runs")
    @ValidationFilterAnnotation(validator = MaintenanceRunPersist.MaintenanceRunPersistValidator.ValidatorName, argumentName = "model")
    public MaintenanceRun startRun(@RequestBody MaintenanceRunPersist model) throws InvalidApplicationException {
        logger.debug("start maintenance run");

        MaintenanceRun run = this.maintenanceRunService.start(model);

        this.auditService.track(AuditableAction.Maintenance_StartRun, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));
        return run;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/runs/latest")
    public MaintenanceRun latestRun() {
        logger.debug("latest maintenance run");

        MaintenanceRun run = this.maintenanceRunService.latest();

        this.auditService.track(AuditableAction.Maintenance_LatestRun);
        return run;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/runs/{id}/stop")
    public MaintenanceRun stopRun(@PathVariable("id") UUID id) {
        logger.debug("stop maintenance run {}", id);

        MaintenanceRun run = this.maintenanceRunService.stop(id);

        this.auditService.track(AuditableAction.Maintenance_StopRun, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));
        return run;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tasks/last-runs")
    public List<MaintenanceTaskRun> lastTaskRuns() {
        logger.debug("last run of each maintenance task");

        List<MaintenanceTaskRun> runs = this.maintenanceRunService.lastTaskRuns();

        this.auditService.track(AuditableAction.Maintenance_LastTaskRuns);
        return runs;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/database-migrations")
    public DatabaseMigrationInfo databaseMigrations() {
        logger.debug("database migrations");

        DatabaseMigrationInfo info = this.databaseMigrationService.info();

        this.auditService.track(AuditableAction.Maintenance_DatabaseMigrations);
        return info;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/installation")
    public InstallationInfo installation() {
        logger.debug("installation info");

        InstallationInfo info = this.installationService.info();

        this.auditService.track(AuditableAction.Maintenance_Installation);
        return info;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/installation")
    @Transactional
    @ValidationFilterAnnotation(validator = InstallationPersist.InstallationPersistValidator.ValidatorName, argumentName = "model")
    public InstallationInfo persistInstallation(@RequestBody InstallationPersist model) throws InvalidApplicationException {
        logger.debug("persist installation");

        InstallationInfo info = this.installationService.persist(model);

        this.auditService.track(AuditableAction.Maintenance_PersistInstallation, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));
        return info;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/index/elastic")
    public void generateIndex() throws Exception {
        logger.debug("generate elastic ");
        this.authorizationService.authorizeForce(Permission.ManageElastic);

	    this.elasticService.resetPlanIndex();
	    this.elasticService.resetDescriptionIndex();

        this.auditService.track(AuditableAction.Maintenance_GenerateElastic);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/index/elastic")
    public void clearIndex() throws Exception {
        logger.debug("clear elastic");
        this.authorizationService.authorizeForce(Permission.ManageElastic);

	    this.elasticService.deleteDescriptionIndex();
	    this.elasticService.deletePlanIndex();

        this.auditService.track(AuditableAction.Maintenance_ClearElastic);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/users/touch")
    @Transactional
    public void sendUserTouchEvents() throws InvalidApplicationException {
        logger.debug("send user touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendUserTouchEvents();
        
        this.auditService.track(AuditableAction.Maintenance_SendUserTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/tenants/touch")
    @Transactional
    public void sendTenantTouchEvents() throws InvalidApplicationException {
        logger.debug("send tenant touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendTenantTouchEvents();

        this.auditService.track(AuditableAction.Maintenance_SendTenantTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plans/touch")
    @Transactional
    public void sendPlanTouchEvents() throws InvalidApplicationException {
        logger.debug("send plan touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendPlanTouchEvents();

        this.auditService.track(AuditableAction.Maintenance_SendPlanTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/descriptions/touch")
    @Transactional
    public void sendDescriptionTouchEvents() throws InvalidApplicationException {
        logger.debug("send description touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionTouchEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plans/accounting-entry")
    @Transactional
    public void sendPlanAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendPlanAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendPlanAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/descriptions/accounting-entry")
    @Transactional
    public void sendDescriptionAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plan-blueprints/accounting-entry")
    @Transactional
    public void sendBlueprintAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send blueprints accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendBlueprintAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendBlueprintAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plan-blueprints-types/accounting-entry")
    @Transactional
    public void sendBlueprintTypeAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send blueprint types accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendBlueprintTypeAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendBlueprintTypeAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/description-templates/accounting-entry")
    @Transactional
    public void sendDescriptionTemplateAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description templates accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionTemplateAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionTemplateAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/description-template-types/accounting-entry")
    @Transactional
    public void sendDescriptionTemplateTypeAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description template types accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionTemplateTypeAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionTemplateTypeAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/prefilling-sources/accounting-entry")
    @Transactional
    public void sendPrefillingSourceAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send prefilling source accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendPrefillingSourceAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendPrefillingSourceAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/reference-types/accounting-entry")
    @Transactional
    public void sendReferenceTypeAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send reference type accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendReferenceTypeAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendReferenceTypeAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plan-statuses/accounting-entry")
    @Transactional
    public void sendPlanStatusAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan status accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendPlanStatusAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendPlanStatusAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/description-statuses/accounting-entry")
    @Transactional
    public void sendDescriptionStatusAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description status accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionStatusAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionStatusAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/users/accounting-entry")
    @Transactional
    public void sendUserAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send user accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendUserAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendUserAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/evaluation-plan/accounting-entry")
    @Transactional
    public void sendEvaluationPlanAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send evaluation plan accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendEvaluationPlanAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendEvaluationPlanAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/references/accounting-entry")
    @Transactional
    public void sendReferenceAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send reference entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendReferenceAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendReferenceEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/languages/accounting-entry")
    @Transactional
    public void sendLanguageAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send reference entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendLanguageAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendLanguageEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/evaluation-description/accounting-entry")
    @Transactional
    public void sendEvaluationDescriptionAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send evaluation description accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendEvaluationDescriptionAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendEvaluationDescriptionAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-entry")
    @Transactional
    public void sendIndicatorCreateEvents() throws InvalidApplicationException {
        logger.debug("send indicator create entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorCreateEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorCreateEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-reset-entry")
    @Transactional
    public void sendIndicatorResetEvents() throws InvalidApplicationException {
        logger.debug("send indicator reset entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorResetEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorResetEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-access-entry")
    @Transactional
    public void sendIndicatorAccessEvents() throws InvalidApplicationException {
        logger.debug("send indicator create entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorAccessEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorAccessEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-plan-entry")
    @Transactional
    public void sendIndicatorPointPlanEvents() throws InvalidApplicationException {
        logger.debug("send indicator point plan entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointPlanEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointPlanEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-description-entry")
    @Transactional
    public void sendIndicatorPointDescriptionEvents() throws InvalidApplicationException {
        logger.debug("send indicator point description entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointDescriptionEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointDescriptionEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-reference-entry")
    @Transactional
    public void sendIndicatorPointReferenceEvents() throws InvalidApplicationException {
        logger.debug("send indicator point reference entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointReferenceEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointReferenceEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-user-entry")
    @Transactional
    public void sendIndicatorPointUserEvents() throws InvalidApplicationException {
        logger.debug("send indicator point user entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointUserEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointUserEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-plan-blueprint-entry")
    @Transactional
    public void sendIndicatorPointPlanBlueprintEvents() throws InvalidApplicationException {
        logger.debug("send indicator point plan blueprint entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointPlanBlueprintEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointPlanBlueprintEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-plan-blueprint-type-entry")
    @Transactional
    public void sendIndicatorPointPlanBlueprintTypeEvents() throws InvalidApplicationException {
        logger.debug("send indicator point plan blueprint type entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointPlanBlueprintTypeEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointPlanBlueprintTypeEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-description-template-type-entry")
    @Transactional
    public void sendIndicatorPointDescriptionTemplateTypeEvents() throws InvalidApplicationException {
        logger.debug("send indicator point plan blueprint type entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointDescriptionTemplateTypeEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointDescriptionTemplateTypeEntryEvents);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-description-template-entry")
    @Transactional
    public void sendIndicatorPointDescriptionTemplateEvents() throws InvalidApplicationException {
        logger.debug("send indicator point description template entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointDescriptionTemplateEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointDescriptionTemplateEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-tenant-entry")
    @Transactional
    public void sendIndicatorPointTenantEvents() throws InvalidApplicationException {
        logger.debug("send indicator point tenant entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointTenantEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointTenantEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/dashboard-touched-entry")
    @Transactional
    public void sendDashboardTouchedEvents() throws InvalidApplicationException {
        logger.debug("send dashboard touched entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDashboardTouchedEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDashboardTouchedEntryEvents);
    }
}
