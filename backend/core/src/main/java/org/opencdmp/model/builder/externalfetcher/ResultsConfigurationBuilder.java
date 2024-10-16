package org.opencdmp.model.builder.externalfetcher;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.externalfetcher.ResultsConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.externalfetcher.ResultsConfiguration;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ResultsConfigurationBuilder extends BaseBuilder<ResultsConfiguration, ResultsConfigurationEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ResultsConfigurationBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ResultsConfigurationBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public ResultsConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<ResultsConfiguration> build(FieldSet fields, List<ResultsConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet fieldsMappingFields = fields.extractPrefixed(this.asPrefix(ResultsConfiguration._fieldsMapping));

        List<ResultsConfiguration> models = new ArrayList<>();
        for (ResultsConfigurationEntity d : data) {
            ResultsConfiguration m = new ResultsConfiguration();
            if (fields.hasField(this.asIndexer(ResultsConfiguration._resultsArrayPath))) m.setResultsArrayPath(d.getResultsArrayPath());
            if (!fieldsMappingFields.isEmpty() && d.getFieldsMapping() != null) {
                m.setFieldsMapping(this.builderFactory.builder(ResultFieldsMappingConfigurationBuilder.class).authorize(this.authorize).build(fieldsMappingFields, d.getFieldsMapping()));
            }

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
