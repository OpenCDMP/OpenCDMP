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
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.inbox.EventProcessingStatus;
import org.opencdmp.integrationevent.inbox.InboxPrincipal;
import org.opencdmp.integrationevent.inbox.IntegrationEventProperties;
import org.opencdmp.model.Tenant;
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
    private final AuditService auditService;
    private final PlanUpdateRequestService planUpdateRequestService;

    public DmpTouchedIntegrationEventHandlerImpl(QueryFactory queryFactory, JsonHandlingService jsonHandlingService, TenantScopeFactory tenantScopeFactory, CurrentPrincipalResolver currentPrincipalResolver, ClaimExtractorProperties claimExtractorProperties, TenantEntityManagerFactory tenantEntityManagerFactory, AuditService auditService, PlanUpdateRequestService planUpdateRequestService) {
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.tenantScopeFactory = tenantScopeFactory;
        this.currentPrincipalResolver = currentPrincipalResolver;
        this.claimExtractorProperties = claimExtractorProperties;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.auditService = auditService;
        this.planUpdateRequestService = planUpdateRequestService;
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

            this.planUpdateRequestService.persistFromEvent(event);

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

}
