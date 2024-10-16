package org.opencdmp.integrationevent.outbox.annotationentitytouch;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.PlanUserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnnotationEntityTouchedIntegrationEventHandlerImpl implements AnnotationEntityTouchedIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(AnnotationEntityTouchedIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final QueryFactory queryFactory;

    public AnnotationEntityTouchedIntegrationEventHandlerImpl(OutboxService outboxService, QueryFactory queryFactory) {
        this.outboxService = outboxService;
	    this.queryFactory = queryFactory;
    }

    private void handle(AnnotationEntitiesTouchedIntegrationEvent event, UUID tenantId) {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.ANNOTATION_ENTITY_TOUCH);
        message.setEvent(event);
        message.setTenantId(tenantId);
        this.outboxService.publish(message);
    }

    @Override
    public void handleDescription(UUID descriptionId) {
        DescriptionEntity entity = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionId).firstAs(new BaseFieldSet().ensure(Description._plan).ensure(DescriptionEntity._tenantId));
        if (entity == null) return;
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(entity.getPlanId()).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanUser._user));
        
        AnnotationEntitiesTouchedIntegrationEvent event = new AnnotationEntitiesTouchedIntegrationEvent();
        event.setEvents(List.of(this.buildEventItem(descriptionId, planUsers)));
        
        this.handle(event, entity.getTenantId());
    }

    @Override
    public void handlePlan(UUID planId) {
        List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(planId).collectAs(new BaseFieldSet().ensure(Description._id));
        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).firstAs(new BaseFieldSet().ensure(Plan._id, Plan._tenantId));
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(planId).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanUser._user));

        AnnotationEntitiesTouchedIntegrationEvent event = new AnnotationEntitiesTouchedIntegrationEvent();
        event.setEvents(new ArrayList<>());
        event.getEvents().add(this.buildEventItem(planId, planUsers));

        if (descriptionEntities != null) {
            for (DescriptionEntity description : descriptionEntities) event.getEvents().add(this.buildEventItem(description.getId(), planUsers));
        }

        this.handle(event, planEntity.getTenantId());
    }
    
    private AnnotationEntitiesTouchedIntegrationEvent.AnnotationEntityTouchedIntegrationEvent buildEventItem(UUID entityId, List<PlanUserEntity> planUsers){
        AnnotationEntitiesTouchedIntegrationEvent.AnnotationEntityTouchedIntegrationEvent eventItem = new AnnotationEntitiesTouchedIntegrationEvent.AnnotationEntityTouchedIntegrationEvent();
        eventItem.setEntityId(entityId);
        List<UUID> users = new ArrayList<>();
        for (PlanUserEntity planUser : planUsers){
            users.add(planUser.getUserId());
        }
        eventItem.setUserIds(users);
        return eventItem;
    }
}
