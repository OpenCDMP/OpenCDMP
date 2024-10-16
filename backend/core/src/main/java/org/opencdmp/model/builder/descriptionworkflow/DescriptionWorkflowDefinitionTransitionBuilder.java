package org.opencdmp.model.builder.descriptionworkflow;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionworkflow.DescriptionWorkflowDefinitionTransitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.descriptionstatus.DescriptionStatusBuilder;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.descriptionworkflow.DescriptionWorkflowDefinitionTransition;
import org.opencdmp.query.DescriptionStatusQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionWorkflowDefinitionTransitionBuilder extends BaseBuilder<DescriptionWorkflowDefinitionTransition, DescriptionWorkflowDefinitionTransitionEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;

    public DescriptionWorkflowDefinitionTransitionBuilder(ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionWorkflowDefinitionTransitionBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public DescriptionWorkflowDefinitionTransitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionWorkflowDefinitionTransition> build(FieldSet fields, List<DescriptionWorkflowDefinitionTransitionEntity> data) throws MyApplicationException {
        if (fields == null || fields.isEmpty() || data == null)
            return new ArrayList<>();

        List<DescriptionWorkflowDefinitionTransition> models = new ArrayList<>();

        FieldSet fromStatusFields = fields.extractPrefixed(this.asPrefix(DescriptionWorkflowDefinitionTransition._fromStatus));
        Map<UUID, DescriptionStatus> fromStatusMap = this.collectDescriptionStatuses(fromStatusFields, data, data.stream().map(DescriptionWorkflowDefinitionTransitionEntity::getFromStatusId).distinct().collect(Collectors.toList()));

        FieldSet toStatusFields = fields.extractPrefixed(this.asPrefix(DescriptionWorkflowDefinitionTransition._toStatus));
        Map<UUID, DescriptionStatus> toStatusMap = this.collectDescriptionStatuses(fromStatusFields, data, data.stream().map(DescriptionWorkflowDefinitionTransitionEntity::getToStatusId).distinct().collect(Collectors.toList()));

        for (DescriptionWorkflowDefinitionTransitionEntity d : data) {
            DescriptionWorkflowDefinitionTransition m = new DescriptionWorkflowDefinitionTransition();

            if (!fromStatusFields.isEmpty() && fromStatusMap != null && fromStatusMap.containsKey(d.getFromStatusId())) m.setFromStatus(fromStatusMap.get(d.getFromStatusId()));
            if (!toStatusFields.isEmpty() && toStatusMap != null && toStatusMap.containsKey(d.getToStatusId())) m.setToStatus(toStatusMap.get(d.getToStatusId()));

            models.add(m);
        }

        return models;
    }


    private Map<UUID, DescriptionStatus> collectDescriptionStatuses(FieldSet fields, List<DescriptionWorkflowDefinitionTransitionEntity> data, Collection<UUID> descriptionStatusIds) {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || fields.isEmpty() || data == null)
            return null;
        this.logger.debug("checking related - {}", DescriptionStatus.class.getSimpleName());

        Map<UUID, DescriptionStatus> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(DescriptionStatus._id));
        DescriptionStatusQuery q = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().authorize(this.authorize).ids(descriptionStatusIds);
        itemMap = this.builderFactory.builder(DescriptionStatusBuilder.class).authorize(this.authorize).asForeignKey(q, clone, DescriptionStatus::getId);

        if (!fields.hasField(this.asIndexer(DescriptionStatus._id)))
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> { x.setId(null); });

        return itemMap;

    }
}
