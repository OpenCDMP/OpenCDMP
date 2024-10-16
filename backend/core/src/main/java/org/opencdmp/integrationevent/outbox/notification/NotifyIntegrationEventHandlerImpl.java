package org.opencdmp.integrationevent.outbox.notification;

import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import java.util.UUID;

@Component
@RequestScope
public class NotifyIntegrationEventHandlerImpl implements NotifyIntegrationEventHandler {

    private final OutboxService outboxService;
    private final TenantScope tenantScope;

    @Autowired
    public NotifyIntegrationEventHandlerImpl(
		    OutboxService outboxService, TenantScope tenantScope) {
        this.outboxService = outboxService;
	    this.tenantScope = tenantScope;
    }

    @Override
    public void handle(NotifyIntegrationEvent event) throws InvalidApplicationException {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.NOTIFY);
        message.setEvent(event);
        if (this.tenantScope.isSet()) message.setTenantId(this.tenantScope.getTenant());
        this.outboxService.publish(message);
    }
}
