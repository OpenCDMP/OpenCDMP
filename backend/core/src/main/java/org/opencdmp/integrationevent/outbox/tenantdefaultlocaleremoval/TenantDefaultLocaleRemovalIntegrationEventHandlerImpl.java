package org.opencdmp.integrationevent.outbox.tenantdefaultlocaleremoval;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import java.util.UUID;

@Component
@RequestScope
public class TenantDefaultLocaleRemovalIntegrationEventHandlerImpl implements TenantDefaultLocaleRemovalIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantDefaultLocaleRemovalIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    @Autowired
    public TenantDefaultLocaleRemovalIntegrationEventHandlerImpl(
		    OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Override
    public void handle(TenantDefaultLocaleRemovalIntegrationEvent event) throws InvalidApplicationException {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.TENANT_DEFAULT_LOCALE_REMOVAL);
        message.setEvent(event);
        message.setTenantId(event.getTenantId());
        this.outboxService.publish(message);
    }
}
