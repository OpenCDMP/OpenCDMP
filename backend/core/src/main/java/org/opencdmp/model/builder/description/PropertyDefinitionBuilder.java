package org.opencdmp.model.builder.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.description.PropertyDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("descriptionpropertiesdefinitionbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PropertyDefinitionBuilder extends BaseBuilder<PropertyDefinition, PropertyDefinitionEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private DefinitionEntity definition;

    @Autowired
    public PropertyDefinitionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PropertyDefinitionBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PropertyDefinitionBuilder withDefinition(DefinitionEntity definition) {
        this.definition = definition;
        return this;
    }

    public PropertyDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PropertyDefinition> build(FieldSet fields, List<PropertyDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(PropertyDefinition._fieldSets));
        
        List<PropertyDefinition> models = new ArrayList<>();
        for (PropertyDefinitionEntity d : data) {
            PropertyDefinition m = new PropertyDefinition();
            if (!fieldsFields.isEmpty() && d.getFieldSets() != null && !d.getFieldSets().isEmpty()) {
                m.setFieldSets(new HashMap<>());
                for (String key : d.getFieldSets().keySet()){
                    FieldSetEntity fieldSetEntity = this.definition != null ? this.definition.getFieldSetById(key).stream().findFirst().orElse(null) : null;
                    m.getFieldSets().put(key, this.builderFactory.builder(PropertyDefinitionFieldSetBuilder.class).authorize(this.authorize).withFieldSetEntity(fieldSetEntity).build(fieldsFields, d.getFieldSets().get(key)));
                }
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
