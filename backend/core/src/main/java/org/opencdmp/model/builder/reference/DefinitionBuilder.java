package org.opencdmp.model.builder.reference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.reference.Definition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("referencedefinitionbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefinitionBuilder extends BaseBuilder<Definition, DefinitionEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DefinitionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefinitionBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public DefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Definition> build(FieldSet fields, List<DefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(Definition._fields));
        
        List<Definition> models = new ArrayList<>();
        for (DefinitionEntity d : data) {
            Definition m = new Definition();
            if (!definitionFields.isEmpty() && d.getFields() != null) m.setFields(this.builderFactory.builder(FieldBuilder.class).authorize(this.authorize).build(definitionFields, d.getFields()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
