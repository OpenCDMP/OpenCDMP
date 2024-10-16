package org.opencdmp.model.builder.prefillingsource;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.prefillingsource.PrefillingSourceDefinitionFixedValueFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.prefillingsource.PrefillingSourceDefinitionFixedValueField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrefillingSourceDefinitionFixedValueFieldBuilder extends BaseBuilder<PrefillingSourceDefinitionFixedValueField, PrefillingSourceDefinitionFixedValueFieldEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PrefillingSourceDefinitionFixedValueFieldBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PrefillingSourceDefinitionFixedValueFieldBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PrefillingSourceDefinitionFixedValueFieldBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PrefillingSourceDefinitionFixedValueField> build(FieldSet fields, List<PrefillingSourceDefinitionFixedValueFieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PrefillingSourceDefinitionFixedValueField> models = new ArrayList<>();
        for (PrefillingSourceDefinitionFixedValueFieldEntity d : data) {
            PrefillingSourceDefinitionFixedValueField m = new PrefillingSourceDefinitionFixedValueField();
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionFixedValueField._systemFieldTarget))) m.setSystemFieldTarget(d.getSystemFieldTarget());
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionFixedValueField._semanticTarget))) m.setSemanticTarget(d.getSemanticTarget());
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionFixedValueField._trimRegex))) m.setTrimRegex(d.getTrimRegex());
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionFixedValueField._fixedValue))) m.setFixedValue(d.getFixedValue());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
