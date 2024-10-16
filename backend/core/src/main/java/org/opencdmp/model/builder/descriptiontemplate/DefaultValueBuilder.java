package org.opencdmp.model.builder.descriptiontemplate;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.types.descriptiontemplate.DefaultValueEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.DefaultValue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefaultValueBuilder extends BaseBuilder<DefaultValue, DefaultValueEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity;

    @Autowired
    public DefaultValueBuilder(
		    ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefaultValueBuilder.class)));
    }

    public DefaultValueBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DefaultValueBuilder withFieldEntity(org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity) {
        this.fieldEntity = fieldEntity;
        return this;
    }

    @Override
    public List<DefaultValue> build(FieldSet fields, List<DefaultValueEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        FieldType fieldType = this.fieldEntity != null && this.fieldEntity.getData() != null ? this.fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;

        List<DefaultValue> models = new ArrayList<>();
        for (DefaultValueEntity d : data) {
            DefaultValue m = new DefaultValue();
            if (fields.hasField(this.asIndexer(DefaultValue._dateValue))) m.setDateValue(d.getDateValue());
            if (fields.hasField(this.asIndexer(DefaultValue._booleanValue))) m.setBooleanValue(d.getBooleanValue());
            if (fields.hasField(this.asIndexer(DefaultValue._textValue))) m.setTextValue(d.getTextValue());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
