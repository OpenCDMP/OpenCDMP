package org.opencdmp.model.builder.prefillingsource;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.prefillingsource.PrefillingSourceDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.externalfetcher.ExternalFetcherApiSourceConfigurationBuilder;
import org.opencdmp.model.externalfetcher.ExternalFetcherApiSourceConfiguration;
import org.opencdmp.model.prefillingsource.PrefillingSourceDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrefillingSourceDefinitionBuilder extends BaseBuilder<PrefillingSourceDefinition, PrefillingSourceDefinitionEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PrefillingSourceDefinitionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PrefillingSourceDefinitionBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PrefillingSourceDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PrefillingSourceDefinition> build(FieldSet fields, List<PrefillingSourceDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet searchConfigurationFields = fields.extractPrefixed(this.asPrefix(PrefillingSourceDefinition._searchConfiguration));
        FieldSet getConfigurationFields = fields.extractPrefixed(this.asPrefix(PrefillingSourceDefinition._getConfiguration));
        FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(PrefillingSourceDefinition._fields));
        FieldSet fixedValueFieldsFields = fields.extractPrefixed(this.asPrefix(PrefillingSourceDefinition._fixedValueFields));

        List<PrefillingSourceDefinition> models = new ArrayList<>();
        for (PrefillingSourceDefinitionEntity d : data) {
            PrefillingSourceDefinition m = new PrefillingSourceDefinition();
            if (!fieldsFields.isEmpty() && d.getFields() != null) m.setFields(this.builderFactory.builder(PrefillingSourceDefinitionFieldBuilder.class).authorize(this.authorize).build(fieldsFields, d.getFields()));
            if (!fixedValueFieldsFields.isEmpty() && d.getFixedValueFields() != null) m.setFixedValueFields(this.builderFactory.builder(PrefillingSourceDefinitionFixedValueFieldBuilder.class).authorize(this.authorize).build(fixedValueFieldsFields, d.getFixedValueFields()));
            if (!searchConfigurationFields.isEmpty() && d.getSearchConfiguration() != null) {
                m.setSearchConfiguration((ExternalFetcherApiSourceConfiguration) this.builderFactory.builder(ExternalFetcherApiSourceConfigurationBuilder.class).authorize(this.authorize).build(searchConfigurationFields, d.getSearchConfiguration()));
            }
            if (!getConfigurationFields.isEmpty() && d.getGetConfiguration() != null) {
                m.setGetConfiguration((ExternalFetcherApiSourceConfiguration) this.builderFactory.builder(ExternalFetcherApiSourceConfigurationBuilder.class).authorize(this.authorize).build(searchConfigurationFields, d.getGetConfiguration()));
            }

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
