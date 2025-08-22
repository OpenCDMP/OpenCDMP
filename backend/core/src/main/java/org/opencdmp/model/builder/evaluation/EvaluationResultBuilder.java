package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.evaluation.EvaluationResultEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.models.misc.EvaluationResultModel;

import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationResultBuilder extends BaseBuilder<EvaluationResultModel, EvaluationResultEntity> {

    public static class EvaluationResultFields {
        public static final String _rank = "rank";
        public static final String _benchmarkTitle = "benchmarkTitle";
        public static final String _benchmarkDetails = "benchmarkDetails";
        public static final String _metrics = "metrics";

    }

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public EvaluationResultBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EvaluationResultBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public EvaluationResultBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<EvaluationResultModel> build(FieldSet fields, List<EvaluationResultEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        FieldSet metricFields = fields.extractPrefixed(this.asPrefix(EvaluationResultFields._metrics));


        List<EvaluationResultModel> models = new ArrayList<>();
        for (EvaluationResultEntity d : data) {
            EvaluationResultModel m = new EvaluationResultModel();
            if (fields.hasField(this.asIndexer(EvaluationResultFields._rank))) m.setRank(d.getRank());
            if (fields.hasField(this.asIndexer(EvaluationResultFields._benchmarkDetails))) m.setBenchmarkDetails(d.getBenchmarkDetails());
            if (fields.hasField(this.asIndexer(EvaluationResultFields._benchmarkTitle))) m.setBenchmarkTitle(d.getBenchmarkTitle());
            if (!metricFields.isEmpty() && d.getMetrics() != null) m.setMetrics(this.builderFactory.builder(EvaluationResultMetricBuilder.class).authorize(this.authorize).build(metricFields, d.getMetrics()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
