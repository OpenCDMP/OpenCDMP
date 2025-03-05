package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.evaluation.EvaluationDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.evaluation.EvaluationData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationDataBuilder extends BaseBuilder<EvaluationData, EvaluationDataEntity> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public EvaluationDataBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EvaluationDataBuilder.class)));
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;

    }

    public EvaluationDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<EvaluationData> build(FieldSet fields, List<EvaluationDataEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet rankedConfigFields = fields.extractPrefixed(this.asPrefix(EvaluationData._rankConfig));
        FieldSet rankModelFields = fields.extractPrefixed(this.asPrefix(EvaluationData._rankModel));

        List<EvaluationData> models = new ArrayList<>();
        for (EvaluationDataEntity d : data) {
            EvaluationData m = new EvaluationData();
            if(fields.hasField(this.asIndexer(EvaluationData._evaluatorId))) m.setEvaluatorId(d.getEvaluatorId());
            if (!rankedConfigFields.isEmpty() && d.getRankConfig() != null) m.setRankConfig(this.builderFactory.builder(RankConfigBuilder.class).authorize(this.authorize).build(rankedConfigFields, d.getRankConfig()));
            if (!rankModelFields.isEmpty() && d.getRankModel() != null) m.setRankModel(this.builderFactory.builder(RankModelBuilder.class).authorize(this.authorize).build(rankModelFields, d.getRankModel()));
            models.add(m);
        }


        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
