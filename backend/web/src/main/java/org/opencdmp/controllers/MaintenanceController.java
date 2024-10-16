package org.opencdmp.controllers;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.Permission;
import org.opencdmp.service.elastic.ElasticService;
import org.opencdmp.service.maintenance.MaintenanceService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InvalidApplicationException;

@RestController
@RequestMapping(path = "api/maintenance")
public class MaintenanceController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(MaintenanceController.class));

    private final AuthorizationService authorizationService;

    private final ElasticService elasticService;

    private final AuditService auditService;

    private final MaintenanceService maintenanceService;
    @Autowired
    public MaintenanceController(
		    AuthorizationService authorizationService,
		    ElasticService elasticService,
		    AuditService auditService, 
            MaintenanceService maintenanceService) {
        this.authorizationService = authorizationService;
        this.elasticService = elasticService;
        this.auditService = auditService;
	    this.maintenanceService = maintenanceService;
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
    public void sendUserTouchEvents() throws InvalidApplicationException {
        logger.debug("send user touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendUserTouchEvents();
        
        this.auditService.track(AuditableAction.Maintenance_SendUserTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/tenants/touch")
    public void sendTenantTouchEvents() throws InvalidApplicationException {
        logger.debug("send tenant touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendTenantTouchEvents();

        this.auditService.track(AuditableAction.Maintenance_SendTenantTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plans/touch")
    public void sendPlanTouchEvents() throws InvalidApplicationException {
        logger.debug("send plan touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendPlanTouchEvents();

        this.auditService.track(AuditableAction.Maintenance_SendPlanTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/descriptions/touch")
    public void sendDescriptionTouchEvents() throws InvalidApplicationException {
        logger.debug("send description touch queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionTouchEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionTouchEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plans/accounting-entry")
    public void sendPlanAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendPlanAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendPlanAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/descriptions/accounting-entry")
    public void sendDescriptionAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send plan accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/plan-blueprints/accounting-entry")
    public void sendBlueprintAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send blueprints accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendBlueprintAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendBlueprintAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/description-templates/accounting-entry")
    public void sendDescriptionTemplateAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description templates accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionTemplateAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionTemplateAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/description-template-types/accounting-entry")
    public void sendDescriptionTemplateTypeAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send description template types accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendDescriptionTemplateTypeAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendDescriptionTemplateTypeAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/prefilling-sources/accounting-entry")
    public void sendPrefillingSourceAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send prefilling source accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendPrefillingSourceAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendPrefillingSourceAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/reference-types/accounting-entry")
    public void sendReferenceTypeAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send reference type accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendReferenceTypeAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendReferenceTypeAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/users/accounting-entry")
    public void sendUserAccountingEntriesEvents() throws InvalidApplicationException {
        logger.debug("send user accounting entries queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendUserAccountingEntriesEvents();

        this.auditService.track(AuditableAction.Maintenance_SendUserAccountingEntriesEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-entry")
    public void sendIndicatorCreateEvents() throws InvalidApplicationException {
        logger.debug("send indicator create entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorCreateEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorCreateEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-reset-entry")
    public void sendIndicatorResetEvents() throws InvalidApplicationException {
        logger.debug("send indicator reset entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorResetEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorResetEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-access-entry")
    public void sendIndicatorAccessEvents() throws InvalidApplicationException {
        logger.debug("send indicator create entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorAccessEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorAccessEntryEvents);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/indicator-point-entry")
    public void sendIndicatorPointEvents() throws InvalidApplicationException {
        logger.debug("send indicator point entry queue events");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        this.maintenanceService.sendIndicatorPointEntryEvents();

        this.auditService.track(AuditableAction.Maintenance_SendIndicatorPointEntryEvents);
    }
}
