package org.opencdmp.model.builder.externalfetcher;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherApiHeaderConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.externalfetcher.ExternalFetcherApiHeaderConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExternalFetcherApiHeaderConfigurationBuilder extends BaseBuilder<ExternalFetcherApiHeaderConfiguration, ExternalFetcherApiHeaderConfigurationEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    public ExternalFetcherApiHeaderConfigurationBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ExternalFetcherApiHeaderConfigurationBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public ExternalFetcherApiHeaderConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    @Override
    public List<ExternalFetcherApiHeaderConfiguration> build(FieldSet fields, List<ExternalFetcherApiHeaderConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<ExternalFetcherApiHeaderConfiguration> models = new ArrayList<>();
        for (ExternalFetcherApiHeaderConfigurationEntity d : data){
            ExternalFetcherApiHeaderConfiguration m = new ExternalFetcherApiHeaderConfiguration();
            if (fields.hasField(this.asIndexer(ExternalFetcherApiHeaderConfiguration._key))) m.setKey(d.getKey());
            if (fields.hasField(this.asIndexer(ExternalFetcherApiHeaderConfiguration._value))) m.setValue(d.getValue());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
