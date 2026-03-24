package org.opencdmp.integrationevent.outbox.planremoval;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanRemovalIntegrationEventHandlerImpl implements PlanRemovalIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanRemovalIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    public PlanRemovalIntegrationEventHandlerImpl(OutboxService outboxService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        this.outboxService = outboxService;
	    this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    private void handle(PlanRemovalIntegrationEvent event)  {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.PLAN_REMOVE);
        message.setEvent(event);
        this.outboxService.publish(message);
    }

    @Override
    public void handlePlan(List<UUID> planIds) {
        if (planIds == null || planIds.isEmpty()) return;

        PlanRemovalIntegrationEvent event = new PlanRemovalIntegrationEvent();
        event.setPlanIds(planIds);

        this.handle(event);
    }
}
