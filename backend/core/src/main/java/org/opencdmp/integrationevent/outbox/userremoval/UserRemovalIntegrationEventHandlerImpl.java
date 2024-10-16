package org.opencdmp.integrationevent.outbox.userremoval;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.UUID;

@Component("outboxuserremovalintegrationeventhandler")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserRemovalIntegrationEventHandlerImpl implements UserRemovalIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserRemovalIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final ApplicationContext applicationContext;

    public UserRemovalIntegrationEventHandlerImpl(OutboxService outboxService, ApplicationContext applicationContext) {
        this.outboxService = outboxService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(UUID userId) throws InvalidApplicationException {
        UserRemovalConsistencyHandler userRemovalConsistencyHandler = this.applicationContext.getBean(UserRemovalConsistencyHandler.class);
        if (!userRemovalConsistencyHandler.isConsistent(new UserRemovalConsistencyPredicates(userId)))
            return;

        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.USER_REMOVE);
        UserRemovalIntegrationEvent event = new UserRemovalIntegrationEvent();
        event.setUserId(userId);
        message.setEvent(event);
        
        this.outboxService.publish(message);
    }
}
