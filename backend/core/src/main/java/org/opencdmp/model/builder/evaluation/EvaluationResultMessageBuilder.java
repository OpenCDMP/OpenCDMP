package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.evaluation.EvaluationResultMessageEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.models.misc.EvaluationResultMessageModel;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationResultMessageBuilder extends BaseBuilder<EvaluationResultMessageModel, EvaluationResultMessageEntity> {

    public static class EvaluationResultFields {
        public static final String _title = "title";
        public static final String _message = "message";

    }


    @Autowired
    public EvaluationResultMessageBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EvaluationResultMessageBuilder.class)));
    }

    public EvaluationResultMessageBuilder authorize(EnumSet<AuthorizationFlags> values) {
        return this;
    }

    @Override
    public List<EvaluationResultMessageModel> build(FieldSet fields, List<EvaluationResultMessageEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<EvaluationResultMessageModel> models = new ArrayList<>();
        for (EvaluationResultMessageEntity d : data) {
            EvaluationResultMessageModel m = new EvaluationResultMessageModel();
            if (fields.hasField(this.asIndexer(EvaluationResultFields._title))) m.setTitle(d.getTitle());
            if (fields.hasField(this.asIndexer(EvaluationResultFields._message))) m.setMessage(d.getMessage());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
