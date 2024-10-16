package org.opencdmp.model.builder.planworkflow;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionTransitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.planstatus.PlanStatusBuilder;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.planworkflow.PlanWorkflowDefinitionTransition;
import org.opencdmp.query.PlanStatusQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanWorkflowDefinitionTransitionBuilder extends BaseBuilder<PlanWorkflowDefinitionTransition, PlanWorkflowDefinitionTransitionEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    public PlanWorkflowDefinitionTransitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanWorkflowDefinitionTransitionBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanWorkflowDefinitionTransitionBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    @Override
    public List<PlanWorkflowDefinitionTransition> build(FieldSet fields, List<PlanWorkflowDefinitionTransitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanWorkflowDefinitionTransition> models = new ArrayList<>();

        FieldSet fromStatusFields = fields.extractPrefixed(this.asPrefix(PlanWorkflowDefinitionTransition._fromStatus));
        Map<UUID, PlanStatus> fromStatusItemsMap = this.collectPlanStatuses(fromStatusFields, data, data.stream().map(PlanWorkflowDefinitionTransitionEntity::getFromStatusId).distinct().collect(Collectors.toList()));

        FieldSet toStatusFields = fields.extractPrefixed(this.asPrefix(PlanWorkflowDefinitionTransition._fromStatus));
        Map<UUID, PlanStatus> toStatusItemsMap = this.collectPlanStatuses(toStatusFields, data, data.stream().map(PlanWorkflowDefinitionTransitionEntity::getToStatusId).distinct().collect(Collectors.toList()));

        for (PlanWorkflowDefinitionTransitionEntity d : data) {
            PlanWorkflowDefinitionTransition m = new PlanWorkflowDefinitionTransition();
            if (fromStatusItemsMap != null && !fromStatusFields.isEmpty() && fromStatusItemsMap.containsKey(d.getFromStatusId())) m.setFromStatus(fromStatusItemsMap.get(d.getFromStatusId()));
            if (toStatusItemsMap != null && !toStatusFields.isEmpty() && toStatusItemsMap.containsKey(d.getToStatusId())) m.setToStatus(toStatusItemsMap.get(d.getToStatusId()));

            models.add(m);
        }

        return models;
    }

    private Map<UUID, PlanStatus> collectPlanStatuses(FieldSet fields, List<PlanWorkflowDefinitionTransitionEntity> data, Collection<UUID> planStatusIds) {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanStatus.class.getSimpleName());

        Map<UUID, PlanStatus> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PlanStatus._id));
        PlanStatusQuery q = this.queryFactory.query(PlanStatusQuery.class).disableTracking().authorize(this.authorize).ids(planStatusIds);
        itemMap = this.builderFactory.builder(PlanStatusBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PlanStatus::getId);

        if (!fields.hasField(this.asIndexer(PlanStatus._id)))
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> { x.setId(null); });

        return itemMap;

    }
}
