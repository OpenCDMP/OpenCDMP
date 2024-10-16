package org.opencdmp.integrationevent.outbox.tenantremoval;

import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.opencdmp.integrationevent.outbox.userremoval.UserRemovalConsistencyHandler;
import org.opencdmp.integrationevent.outbox.userremoval.UserRemovalConsistencyPredicates;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantRemovalIntegrationEventHandlerImpl implements TenantRemovalIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantRemovalIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final ApplicationContext applicationContext;

    @Autowired
    public TenantRemovalIntegrationEventHandlerImpl(OutboxService outboxService, ApplicationContext applicationContext) {
        this.outboxService = outboxService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(TenantRemovalIntegrationEvent event) {
        TenantRemovalConsistencyHandler tenantRemovalConsistencyHandler = this.applicationContext.getBean(TenantRemovalConsistencyHandler.class);
        if (!tenantRemovalConsistencyHandler.isConsistent(new TenantRemovalConsistencyPredicates(event.getId())))
            return;

        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.TENANT_REMOVE);
        message.setEvent(event);
        message.setTenantId(event.getId());
        this.outboxService.publish(message);
    }

    @Override
    public void handle(UUID tenantId) {
        TenantRemovalConsistencyHandler tenantRemovalConsistencyHandler = this.applicationContext.getBean(TenantRemovalConsistencyHandler.class);
        if (!tenantRemovalConsistencyHandler.isConsistent(new TenantRemovalConsistencyPredicates(tenantId)))
            return;

        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.TENANT_REMOVE);
        TenantRemovalIntegrationEvent event = new TenantRemovalIntegrationEvent();
        event.setId(tenantId);
        message.setEvent(event);
        message.setTenantId(event.getId());
        this.outboxService.publish(message);
    }

}
