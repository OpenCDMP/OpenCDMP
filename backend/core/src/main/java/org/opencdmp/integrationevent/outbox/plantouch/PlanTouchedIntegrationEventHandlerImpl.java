package org.opencdmp.integrationevent.outbox.plantouch;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commons.configuration.integration.IntegrationProperties;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.opencdmp.model.builder.commonmodels.plan.PlanCommonModelBuilder;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.plan.PlanServiceProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanTouchedIntegrationEventHandlerImpl implements PlanTouchedIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanTouchedIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private final FileTransformerService fileTransformerService;

    private final PlanServiceProperties planServiceProperties;

    private final ConventionService conventionService;

    private final IntegrationProperties integrationProperties;

    public PlanTouchedIntegrationEventHandlerImpl(OutboxService outboxService, QueryFactory queryFactory, BuilderFactory builderFactory, FileTransformerService fileTransformerService, PlanServiceProperties planServiceProperties, ConventionService conventionService, IntegrationProperties integrationProperties) {
        this.outboxService = outboxService;
	    this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.fileTransformerService = fileTransformerService;
        this.planServiceProperties = planServiceProperties;
        this.conventionService = conventionService;
        this.integrationProperties = integrationProperties;
    }

    private void handle(PlanTouchedIntegrationEvent event) {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.PLAN_TOUCH);
        message.setEvent(event);
        this.outboxService.publish(message);
    }

    @Override
    public void handlePlan(List<UUID> planIds) {
        if (this.integrationProperties.getEnabled() == null || !this.integrationProperties.getEnabled()) return;
        if (planIds == null || planIds.isEmpty()) return;

        List<PlanEntity> planEntities = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planIds).collect();

        List<PlanTouchedIntegrationEvent.PlanTouched> planModels = new ArrayList<>();
        for(PlanEntity entity : planEntities){
            try {
                FileEnvelopeModel jsonEnvelope = new FileEnvelopeModel();

                org.opencdmp.model.file.FileEnvelope rda = this.fileTransformerService.exportPlanInternal(entity.getId(), planServiceProperties.getRdaTransformerId(), "json");
                jsonEnvelope.setFilename(rda.getFilename());
                jsonEnvelope.setFile(rda.getFile());
                jsonEnvelope.setMimeType("application/json");

                PlanModel plan = this.builderFactory.builder(PlanCommonModelBuilder.class).setRdaJsonFile(jsonEnvelope).build(entity);

                PlanTouchedIntegrationEvent.PlanTouched touched = new PlanTouchedIntegrationEvent.PlanTouched();
                touched.setPlan(plan);
                touched.setTenantId(entity.getTenantId());

                planModels.add(touched);
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
        if (this.conventionService.isListNullOrEmpty(planModels)) return;

        PlanTouchedIntegrationEvent event = new PlanTouchedIntegrationEvent();
        event.setPlans(planModels);

        this.handle(event);
    }
}
