package org.opencdmp.integrationevent.outbox.annotationentityremoval;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnnotationEntityRemovalIntegrationEventHandlerImpl implements AnnotationEntityRemovalIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(AnnotationEntityRemovalIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final QueryFactory queryFactory;

    public AnnotationEntityRemovalIntegrationEventHandlerImpl(OutboxService outboxService, QueryFactory queryFactory, TenantScope tenantScope) {
        this.outboxService = outboxService;
	    this.queryFactory = queryFactory;
    }

    private void handle(AnnotationEntitiesRemovalIntegrationEvent event, UUID tenantId)  {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.ANNOTATION_ENTITY_REMOVE);
        message.setEvent(event);
        this.outboxService.publish(message);
    }

    @Override
    public void handleDescription(UUID descriptionId) {
        DescriptionEntity description = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionId).firstAs(new BaseFieldSet().ensure(Description._id).ensure(DescriptionEntity._tenantId));
        if (description == null) return;
        
	    AnnotationEntitiesRemovalIntegrationEvent event = new AnnotationEntitiesRemovalIntegrationEvent();
        event.setEntityIds(List.of(descriptionId));
        
        this.handle(event, description.getTenantId());
    }

    @Override
    public void handlePlan(UUID planId) {
        PlanEntity plan = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).firstAs(new BaseFieldSet().ensure(Plan._id).ensure(PlanEntity._tenantId));
        if(plan == null) return;
        
        List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(planId).collectAs(new BaseFieldSet().ensure(Description._id));
        
        AnnotationEntitiesRemovalIntegrationEvent event = new AnnotationEntitiesRemovalIntegrationEvent();
        event.setEntityIds(new ArrayList<>());
        event.getEntityIds().add(planId);

        for (DescriptionEntity description : descriptionEntities) event.getEntityIds().add(description.getId());
        
        this.handle(event, plan.getTenantId());
    }
}
