package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.evaluation.EvaluationResultMetricEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.models.misc.EvaluationResultMetricModel;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationResultMetricBuilder extends BaseBuilder<EvaluationResultMetricModel, EvaluationResultMetricEntity> {

    public static class EvaluationResultFields {
        public static final String _rank = "rank";
        public static final String _metricTitle = "metricTitle";
        public static final String _metricDetails = "metricDetails";
        public static final String _messages = "messages";

    }

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public EvaluationResultMetricBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EvaluationResultMetricBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public EvaluationResultMetricBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<EvaluationResultMetricModel> build(FieldSet fields, List<EvaluationResultMetricEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        FieldSet messageFields = fields.extractPrefixed(this.asPrefix(EvaluationResultFields._messages));


        List<EvaluationResultMetricModel> models = new ArrayList<>();
        for (EvaluationResultMetricEntity d : data) {
            EvaluationResultMetricModel m = new EvaluationResultMetricModel();
            if (fields.hasField(this.asIndexer(EvaluationResultFields._rank))) m.setRank(d.getRank());
            if (fields.hasField(this.asIndexer(EvaluationResultFields._metricDetails))) m.setMetricDetails(d.getMetricDetails());
            if (fields.hasField(this.asIndexer(EvaluationResultFields._metricTitle))) m.setMetricTitle(d.getMetricTitle());
            if (!messageFields.isEmpty() && d.getMessages() != null) m.setMessages(this.builderFactory.builder(EvaluationResultMessageBuilder.class).authorize(this.authorize).build(messageFields, d.getMessages()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
