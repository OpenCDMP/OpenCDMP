package org.opencdmp.model.builder.prefillingsource;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.prefillingsource.PrefillingSourceDefinitionFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.prefillingsource.PrefillingSourceDefinitionField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrefillingSourceDefinitionFieldBuilder extends BaseBuilder<PrefillingSourceDefinitionField, PrefillingSourceDefinitionFieldEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PrefillingSourceDefinitionFieldBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PrefillingSourceDefinitionFieldBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PrefillingSourceDefinitionFieldBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PrefillingSourceDefinitionField> build(FieldSet fields, List<PrefillingSourceDefinitionFieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PrefillingSourceDefinitionField> models = new ArrayList<>();
        for (PrefillingSourceDefinitionFieldEntity d : data) {
            PrefillingSourceDefinitionField m = new PrefillingSourceDefinitionField();
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionField._code))) m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionField._systemFieldTarget))) m.setSystemFieldTarget(d.getSystemFieldTarget());
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionField._semanticTarget))) m.setSemanticTarget(d.getSemanticTarget());
            if (fields.hasField(this.asIndexer(PrefillingSourceDefinitionField._trimRegex))) m.setTrimRegex(d.getTrimRegex());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
