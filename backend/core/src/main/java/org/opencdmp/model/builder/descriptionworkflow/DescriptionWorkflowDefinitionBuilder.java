package org.opencdmp.model.builder.descriptionworkflow;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionworkflow.DescriptionWorkflowDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.descriptionstatus.DescriptionStatusBuilder;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.descriptionworkflow.DescriptionWorkflowDefinition;
import org.opencdmp.query.DescriptionStatusQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionWorkflowDefinitionBuilder extends BaseBuilder<DescriptionWorkflowDefinition, DescriptionWorkflowDefinitionEntity> {


    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    public DescriptionWorkflowDefinitionBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionWorkflowDefinitionBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public DescriptionWorkflowDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionWorkflowDefinition> build(FieldSet fields, List<DescriptionWorkflowDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || fields.isEmpty() || data == null)
            return new ArrayList<>();

        List<DescriptionWorkflowDefinition> models = new ArrayList<>();

        FieldSet transitionsFields = fields.extractPrefixed(this.asPrefix(DescriptionWorkflowDefinition._statusTransitions));

        FieldSet startingStatusFields = fields.extractPrefixed(this.asPrefix(DescriptionWorkflowDefinition._startingStatus));
        Map<UUID, DescriptionStatus> startingStatusMap = this.collectDescriptionStatuses(startingStatusFields, data);

        for (DescriptionWorkflowDefinitionEntity d : data) {
            DescriptionWorkflowDefinition m = new DescriptionWorkflowDefinition();
            if (!startingStatusFields.isEmpty() && startingStatusMap != null && startingStatusMap.containsKey(d.getStartingStatusId()))
                m.setStartingStatus(startingStatusMap.get(d.getStartingStatusId()));

            if (!transitionsFields.isEmpty() && d.getStatusTransitions() != null && !d.getStatusTransitions().isEmpty())
                m.setStatusTransitions(this.builderFactory.builder(DescriptionWorkflowDefinitionTransitionBuilder.class).build(transitionsFields, d.getStatusTransitions()));

            models.add(m);
        }

        return models;
    }

    private Map<UUID, DescriptionStatus> collectDescriptionStatuses(FieldSet fields, List<DescriptionWorkflowDefinitionEntity> data) {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionStatus.class.getSimpleName());

        Map<UUID, DescriptionStatus> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(DescriptionStatus._id));
        DescriptionStatusQuery q = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionWorkflowDefinitionEntity::getStartingStatusId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionStatusBuilder.class).authorize(this.authorize).asForeignKey(q, clone, DescriptionStatus::getId);

        if (!fields.hasField(this.asIndexer(DescriptionStatus._id)))
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> { x.setId(null); });

        return itemMap;

    }
}
