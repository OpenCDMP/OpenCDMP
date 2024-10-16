package org.opencdmp.model.builder.externalfetcher;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.externalfetcher.ResultFieldsMappingConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.externalfetcher.ResultFieldsMappingConfiguration;
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
public class ResultFieldsMappingConfigurationBuilder extends BaseBuilder<ResultFieldsMappingConfiguration, ResultFieldsMappingConfigurationEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ResultFieldsMappingConfigurationBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ResultFieldsMappingConfigurationBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public ResultFieldsMappingConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<ResultFieldsMappingConfiguration> build(FieldSet fields, List<ResultFieldsMappingConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<ResultFieldsMappingConfiguration> models = new ArrayList<>();
        for (ResultFieldsMappingConfigurationEntity d : data) {
            ResultFieldsMappingConfiguration m = new ResultFieldsMappingConfiguration();
            if (fields.hasField(this.asIndexer(ResultFieldsMappingConfiguration._code))) m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(ResultFieldsMappingConfiguration._responsePath))) m.setResponsePath(d.getResponsePath());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
