package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.PluginEntityType;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.types.evaluation.EvaluationTargetEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.models.misc.EvaluationTargetModel;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationTargetBuilder extends BaseBuilder<EvaluationTargetModel, EvaluationTargetEntity> {

    public static class EvaluationTargetFields {
        public static final String _entityType = "entityType";
        public static final String _entityId = "entityId";
        public static final String _label = "label";
    }

    @Autowired
    public EvaluationTargetBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EvaluationTargetBuilder.class)));
    }

    public EvaluationTargetBuilder authorize(EnumSet<AuthorizationFlags> values) {
        return this;
    }

    @Override
    public List<EvaluationTargetModel> build(FieldSet fields, List<EvaluationTargetEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<EvaluationTargetModel> models = new ArrayList<>();
        for (EvaluationTargetEntity d : data) {
            EvaluationTargetModel m = new EvaluationTargetModel();
            if (fields.hasField(this.asIndexer(EvaluationTargetFields._entityType))) m.setEntityType(this.toPluginEntityType(d.getEntityType()));
            if (fields.hasField(this.asIndexer(EvaluationTargetFields._entityId))) m.setEntityId(d.getEntityId());
            if (fields.hasField(this.asIndexer(EvaluationTargetFields._label))) m.setLabel(d.getLabel());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private PluginEntityType toPluginEntityType(EntityType entityType) {
        if (entityType == null) return null;
        return switch (entityType) {
            case Plan -> PluginEntityType.Plan;
            case Description -> PluginEntityType.Description;
        };
    }
}
