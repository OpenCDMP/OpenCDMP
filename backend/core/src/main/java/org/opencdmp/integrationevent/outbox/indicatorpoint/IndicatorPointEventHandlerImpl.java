package org.opencdmp.integrationevent.outbox.indicatorpoint;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndicatorPointEventHandlerImpl implements IndicatorPointEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(IndicatorPointEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final ApplicationContext applicationContext;

    @Autowired
    public IndicatorPointEventHandlerImpl(OutboxService outboxService, ApplicationContext applicationContext) {
        this.outboxService = outboxService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(IndicatorPointEvent event) {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.INDICATOR_POINT_ENTRY);
        message.setEvent(event);
        message.setTenantId(null);

        this.outboxService.publish(message);
    }
}
