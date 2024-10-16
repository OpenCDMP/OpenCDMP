package org.opencdmp.model.builder.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.Field;
import org.opencdmp.service.fielddatahelper.FieldDataHelperService;
import org.opencdmp.service.fielddatahelper.FieldDataHelperServiceProvider;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("descriptiontemplatedefinitionfieldbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldBuilder extends BaseBuilder<Field, FieldEntity> {

    private final BuilderFactory builderFactory;
    private final FieldDataHelperServiceProvider fieldDataHelperServiceProvider;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public FieldBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, FieldDataHelperServiceProvider fieldDataHelperServiceProvider) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldBuilder.class)));
        this.builderFactory = builderFactory;
        this.fieldDataHelperServiceProvider = fieldDataHelperServiceProvider;
    }

    public FieldBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Field> build(FieldSet fields, List<FieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet visibilityRulesFields = fields.extractPrefixed(this.asPrefix(Field._visibilityRules));
        FieldSet defaultValueFields = fields.extractPrefixed(this.asPrefix(Field._defaultValue));
        FieldSet dataFields = fields.extractPrefixed(this.asPrefix(Field._data));

        List<Field> models = new ArrayList<>();
        for (FieldEntity d : data) {
            Field m = new Field();
            if (fields.hasField(this.asIndexer(Field._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Field._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(Field._semantics))) m.setSemantics(d.getSemantics());
            if (fields.hasField(this.asIndexer(Field._includeInExport))) m.setIncludeInExport(d.getIncludeInExport());
            if (fields.hasField(this.asIndexer(Field._validations))) m.setValidations(d.getValidations());
            if (!defaultValueFields.isEmpty() && d.getDefaultValue() != null) m.setDefaultValue(this.builderFactory.builder(DefaultValueBuilder.class).withFieldEntity(d).authorize(this.authorize).build(defaultValueFields, d.getDefaultValue()));
            if (!visibilityRulesFields.isEmpty() && d.getVisibilityRules() != null) m.setVisibilityRules(this.builderFactory.builder(RuleBuilder.class).withFieldEntity(d).authorize(this.authorize).build(visibilityRulesFields, d.getVisibilityRules()));
            if (!dataFields.isEmpty() && d.getData() != null){
                FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(d.getData().getFieldType());
                m.setData(fieldDataHelperService.buildOne(dataFields, d.getData(), this.authorize));
            }
            
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
