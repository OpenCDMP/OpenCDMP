package org.opencdmp.integrationevent.outbox.tenanttouched;

import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("outboxtenanttouchedintegrationeventhandler")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantTouchedIntegrationEventHandlerImpl implements TenantTouchedIntegrationEventHandler {

    private final OutboxService outboxService;

    public TenantTouchedIntegrationEventHandlerImpl(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Override
    public void handle(TenantTouchedIntegrationEvent event) {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.TENANT_TOUCH);
//        message.setTenantId(event.getId()); //Hack Can not Queue inbox before tenant created
        message.setEvent(event);

        this.outboxService.publish(message);
    }

}
