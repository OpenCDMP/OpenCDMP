package org.opencdmp.integrationevent.inbox.dmptouch;

import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorProperties;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.PlanUpdateRequestActionType;
import org.opencdmp.commons.enums.PlanUpdateRequestStatus;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.inbox.EventProcessingStatus;
import org.opencdmp.integrationevent.inbox.InboxPrincipal;
import org.opencdmp.integrationevent.inbox.IntegrationEventProperties;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.persist.PlanUpdateRequestPersist;
import org.opencdmp.query.*;
import org.opencdmp.service.planupdaterequest.PlanUpdateRequestService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.*;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DmpTouchedIntegrationEventHandlerImpl implements DmpTouchedIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DmpTouchedIntegrationEventHandlerImpl.class));

    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final TenantScopeFactory tenantScopeFactory;
    private final CurrentPrincipalResolver currentPrincipalResolver;
    private final ClaimExtractorProperties claimExtractorProperties;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final ValidatorFactory validatorFactory;
    private final AuditService auditService;
    private final PlanUpdateRequestService planUpdateRequestService;
    private final BuilderFactory builderFactory;

    public DmpTouchedIntegrationEventHandlerImpl(QueryFactory queryFactory, JsonHandlingService jsonHandlingService, TenantScopeFactory tenantScopeFactory, CurrentPrincipalResolver currentPrincipalResolver, ClaimExtractorProperties claimExtractorProperties, TenantEntityManagerFactory tenantEntityManagerFactory, ValidatorFactory validatorFactory, AuditService auditService, PlanUpdateRequestService planUpdateRequestService, BuilderFactory builderFactory) {
	    this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.tenantScopeFactory = tenantScopeFactory;
        this.currentPrincipalResolver = currentPrincipalResolver;
        this.claimExtractorProperties = claimExtractorProperties;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.validatorFactory = validatorFactory;
        this.auditService = auditService;
        this.planUpdateRequestService = planUpdateRequestService;
        this.builderFactory = builderFactory;
    }

    @Override
    public EventProcessingStatus handle(IntegrationEventProperties properties, String message) throws InvalidApplicationException {
        DmpTouchedIntegrationEvent event = this.jsonHandlingService.fromJsonSafe(DmpTouchedIntegrationEvent.class, message);
        if (event == null)
            return EventProcessingStatus.Error;

        logger.debug("Handling {}", DmpTouchedIntegrationEvent.class.getSimpleName());

        EventProcessingStatus status = EventProcessingStatus.Success;
        try {

            if (this.tenantScopeFactory.getInstance().isMultitenant()) {
                if (properties.getTenantId() != null) {
                    TenantEntity tenant = this.queryFactory.query(TenantQuery.class).disableTracking().ids(properties.getTenantId()).firstAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
                    if (tenant == null) {
                        logger.error("missing tenant from event message");
                        return EventProcessingStatus.Error;
                    }
                    this.tenantScopeFactory.getInstance().setTempTenant(this.tenantEntityManagerFactory.getInstance(), properties.getTenantId(), tenant.getCode());
                } else if (this.tenantScopeFactory.getInstance().supportExpansionTenant()) {
                    this.tenantScopeFactory.getInstance().setTempTenant(this.tenantEntityManagerFactory.getInstance(), null, this.tenantScopeFactory.getInstance().getDefaultTenantCode());
                } else {
                    logger.error("missing tenant from event message");
                    return EventProcessingStatus.Error;
                }
            }

	        this.currentPrincipalResolver.push(InboxPrincipal.build(properties, this.claimExtractorProperties));

            PlanUpdateRequestPersist persist = this.buildPlanUpdateRequestPersist(event);
            this.validatorFactory.validator(PlanUpdateRequestPersist.PlanUpdateRequestPersistValidator.class).validateForce(persist);
            this.planUpdateRequestService.persist(persist, null);

            this.auditService.track(AuditableAction.MaDMP_Touched, Map.ofEntries(
                    new AbstractMap.SimpleEntry<String, Object>("model", event)
            ));

        } catch (Exception ex) {
            status = EventProcessingStatus.Error;
            logger.error("Problem getting list of queue outbox. Skipping: {}", ex.getMessage(), ex);
        } finally {
	        this.currentPrincipalResolver.pop();
            try {
	            this.tenantScopeFactory.getInstance().removeTempTenant(this.tenantEntityManagerFactory.getInstance());
                this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
            } catch (InvalidApplicationException e) {
            }
        }

        return status;
    }

    private PlanUpdateRequestPersist buildPlanUpdateRequestPersist(DmpTouchedIntegrationEvent event) {
        PlanUpdateRequestPersist persist = new PlanUpdateRequestPersist();
        if (event == null) return persist;

        if (event.getActionType() != null && event.getActionType().equals(PlanUpdateRequestActionType.UpdatePlan)) persist.setPlanId(event.getDmpId());

        persist.setActionType(event.getActionType());
        persist.setStatus(PlanUpdateRequestStatus.Pending);
        persist.setSubmitterId(event.getSubmitterId());
        persist.setData(event.getData());
        persist.setSourceCode(event.getSourceCode());

        return persist;
    }
}
