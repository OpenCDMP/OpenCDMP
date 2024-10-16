package org.opencdmp.model.builder.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.description.PropertyDefinitionFieldSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("description.PropertyDefinitionFieldSet")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionFieldSetBuilder extends BaseBuilder<PropertyDefinitionFieldSet, PropertyDefinitionFieldSetEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private FieldSetEntity fieldSetEntity;

    @Autowired
    public PropertyDefinitionFieldSetBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PropertyDefinitionFieldSetBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PropertyDefinitionFieldSetBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PropertyDefinitionFieldSetBuilder withFieldSetEntity(FieldSetEntity fieldSetEntity) {
        this.fieldSetEntity = fieldSetEntity;
        return this;
    }
    
    @Override
    public List<PropertyDefinitionFieldSet> build(FieldSet fields, List<PropertyDefinitionFieldSetEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet itemsFields = fields.extractPrefixed(this.asPrefix(PropertyDefinitionFieldSet._items));
        
        List<PropertyDefinitionFieldSet> models = new ArrayList<>();
        for (PropertyDefinitionFieldSetEntity d : data) {
            PropertyDefinitionFieldSet m = new PropertyDefinitionFieldSet();
            if (!itemsFields.isEmpty() && d.getItems() != null) m.setItems(this.builderFactory.builder(PropertyDefinitionFieldSetItemBuilder.class).withFieldSetEntity(this.fieldSetEntity).authorize(this.authorize).build(itemsFields, d.getItems()));
            if (fields.hasField(this.asIndexer(PropertyDefinitionFieldSet._comment))) m.setComment(d.getComment());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
