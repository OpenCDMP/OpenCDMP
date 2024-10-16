package org.opencdmp.model.builder.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.description.PropertyDefinitionFieldSetItem;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("description.PropertyDefinitionFieldSetItemBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionFieldSetItemBuilder extends BaseBuilder<PropertyDefinitionFieldSetItem, PropertyDefinitionFieldSetItemEntity> {

    private final BuilderFactory builderFactory;
    private FieldSetEntity fieldSetEntity;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PropertyDefinitionFieldSetItemBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PropertyDefinitionFieldSetItemBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PropertyDefinitionFieldSetItemBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PropertyDefinitionFieldSetItemBuilder withFieldSetEntity(FieldSetEntity fieldSetEntity) {
        this.fieldSetEntity = fieldSetEntity;
        return this;
    }

    @Override
    public List<PropertyDefinitionFieldSetItem> build(FieldSet fields, List<PropertyDefinitionFieldSetItemEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(PropertyDefinitionFieldSetItem._fields));
        
        List<PropertyDefinitionFieldSetItem> models = new ArrayList<>();
        for (PropertyDefinitionFieldSetItemEntity d : data) {
            PropertyDefinitionFieldSetItem m = new PropertyDefinitionFieldSetItem();
            if (fields.hasField(this.asIndexer(PropertyDefinitionFieldSetItem._ordinal)))  m.setOrdinal(d.getOrdinal());
            if (!fieldsFields.isEmpty() && d.getFields() != null && !d.getFields().isEmpty()) {
                m.setFields(new HashMap<>());
                for (String key : d.getFields().keySet()){
                    FieldEntity fieldEntity = this.fieldSetEntity != null ? this.fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                    m.getFields().put(key, this.builderFactory.builder(FieldBuilder.class).authorize(this.authorize).withFieldEntity(fieldEntity).build(fieldsFields, d.getFields().get(key)));
                }
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
