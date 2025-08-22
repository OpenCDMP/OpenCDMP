package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.evaluation.RankResultEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.models.misc.RankResultModel;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RankResultModelBuilder extends BaseBuilder<RankResultModel, RankResultEntity> {

    public static class RankModelKnownFields {
        public static final String _rank = "rank";
        public static final String _results = "results";
        public static final String _details = "details";
    }

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public RankResultModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RankResultModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public RankResultModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<RankResultModel> build(FieldSet fields, List<RankResultEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet evaluationResultFields = fields.extractPrefixed(this.asPrefix(RankModelKnownFields._results));

        List<RankResultModel> models = new ArrayList<>();
        for (RankResultEntity d : data) {
            RankResultModel m = new RankResultModel();
            if (fields.hasField(this.asIndexer(RankModelKnownFields._rank))) m.setRank(d.getRank());
            if (fields.hasField(this.asIndexer(RankModelKnownFields._details))) m.setDetails(d.getDetails());
            if (!evaluationResultFields.isEmpty() && d.getResults() != null) m.setResults(this.builderFactory.builder(EvaluationResultBuilder.class).authorize(this.authorize).build(evaluationResultFields, d.getResults()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
