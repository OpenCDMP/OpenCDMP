package org.opencdmp.model.builder.referencetype;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherApiSourceConfigurationEntity;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherStaticOptionSourceConfigurationEntity;
import org.opencdmp.commons.types.referencetype.ReferenceTypeDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.externalfetcher.ExternalFetcherApiSourceConfigurationBuilder;
import org.opencdmp.model.builder.externalfetcher.ExternalFetcherStaticOptionSourceConfigurationBuilder;
import org.opencdmp.model.referencetype.ReferenceTypeDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeDefinitionBuilder extends BaseBuilder<ReferenceTypeDefinition, ReferenceTypeDefinitionEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceTypeDefinitionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeDefinitionBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public ReferenceTypeDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<ReferenceTypeDefinition> build(FieldSet fields, List<ReferenceTypeDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(ReferenceTypeDefinition._fields));
        FieldSet sourcesFields = fields.extractPrefixed(this.asPrefix(ReferenceTypeDefinition._sources));

        List<ReferenceTypeDefinition> models = new ArrayList<>();
        for (ReferenceTypeDefinitionEntity d : data) {
            ReferenceTypeDefinition m = new ReferenceTypeDefinition();
            if (!fieldsFields.isEmpty() && d.getFields() != null) m.setFields(this.builderFactory.builder(ReferenceTypeFieldBuilder.class).authorize(this.authorize).build(fieldsFields, d.getFields()));
            if (!sourcesFields.isEmpty() && d.getSources() != null) {
                List<ExternalFetcherApiSourceConfigurationEntity> externalApiConfigEntities = d.getSources().stream().filter(x-> ExternalFetcherSourceType.API.equals(x.getType())).map(x-> (ExternalFetcherApiSourceConfigurationEntity)x).toList();
                List<ExternalFetcherStaticOptionSourceConfigurationEntity> staticOptionConfigEntities = d.getSources().stream().filter(x-> ExternalFetcherSourceType.STATIC.equals(x.getType())).map(x-> (ExternalFetcherStaticOptionSourceConfigurationEntity)x).toList();
                m.setSources(new ArrayList<>());
                m.getSources().addAll(this.builderFactory.builder(ExternalFetcherApiSourceConfigurationBuilder.class).authorize(this.authorize).build(sourcesFields, externalApiConfigEntities));
                m.getSources().addAll(this.builderFactory.builder(ExternalFetcherStaticOptionSourceConfigurationBuilder.class).authorize(this.authorize).build(sourcesFields, staticOptionConfigEntities));
                m.getSources().sort(Comparator.comparing(x -> x.getOrdinal() == null ? 0 : x.getOrdinal()));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
