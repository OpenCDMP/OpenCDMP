package org.opencdmp.integrationevent.outbox.notification;

import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NotifyIntegrationEventHandlerImpl implements NotifyIntegrationEventHandler {

    private final OutboxService outboxService;
    private final TenantScopeFactory tenantScopeFactory;

    @Autowired
    public NotifyIntegrationEventHandlerImpl(
		    OutboxService outboxService, TenantScopeFactory tenantScopeFactory) {
        this.outboxService = outboxService;
	    this.tenantScopeFactory = tenantScopeFactory;
    }

    @Override
    public void handle(NotifyIntegrationEvent event) throws InvalidApplicationException {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.NOTIFY);
        message.setEvent(event);
        if (this.tenantScopeFactory.getInstance().isSet()) message.setTenantId(this.tenantScopeFactory.getInstance().getTenant());
        this.outboxService.publish(message);
    }
}
