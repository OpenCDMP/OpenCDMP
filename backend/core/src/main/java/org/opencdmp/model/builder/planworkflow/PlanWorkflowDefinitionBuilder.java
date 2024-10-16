package org.opencdmp.model.builder.planworkflow;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.planstatus.PlanStatusBuilder;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.planworkflow.PlanWorkflow;
import org.opencdmp.model.planworkflow.PlanWorkflowDefinition;
import org.opencdmp.query.PlanStatusQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanWorkflowDefinitionBuilder extends BaseBuilder<PlanWorkflowDefinition, PlanWorkflowDefinitionEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;

    public PlanWorkflowDefinitionBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanWorkflowDefinitionBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public PlanWorkflowDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        authorize = values;
        return this;
    }

    @Override
    public List<PlanWorkflowDefinition> build(FieldSet fields, List<PlanWorkflowDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || fields.isEmpty() || data == null)
            return new ArrayList<>();

        List<PlanWorkflowDefinition> models = new ArrayList<>();

        FieldSet startingStatusFields = fields.extractPrefixed(this.asPrefix(PlanWorkflowDefinition._startingStatus));
        Map<UUID, PlanStatus> startingStatusItemsMap = this.collectPlanStatuses(startingStatusFields, data);

        FieldSet transitionsFields = fields.extractPrefixed(this.asPrefix(PlanWorkflowDefinition._statusTransitions));

        for (PlanWorkflowDefinitionEntity d : data) {
            PlanWorkflowDefinition m = new PlanWorkflowDefinition();
            if (startingStatusItemsMap != null && !startingStatusFields.isEmpty() && startingStatusItemsMap.containsKey(d.getStartingStatusId())) m.setStartingStatus(startingStatusItemsMap.get(d.getStartingStatusId()));
            if (transitionsFields != null && !d.getStatusTransitions().isEmpty()) m.setStatusTransitions(this.builderFactory.builder(PlanWorkflowDefinitionTransitionBuilder.class).build(transitionsFields, d.getStatusTransitions()));

            models.add(m);
        }

        return models;
    }

    private Map<UUID, PlanStatus> collectPlanStatuses(FieldSet fields, List<PlanWorkflowDefinitionEntity> data) {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanStatus.class.getSimpleName());

        Map<UUID, PlanStatus> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PlanStatus._id));
        PlanStatusQuery q = this.queryFactory.query(PlanStatusQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanWorkflowDefinitionEntity::getStartingStatusId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanStatusBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PlanStatus::getId);

        if (!fields.hasField(this.asIndexer(PlanStatus._id)))
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> { x.setId(null); });

        return itemMap;

    }
}
