package org.opencdmp.model.builder.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.types.descriptiontemplate.RuleEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.Rule;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RuleBuilder extends BaseBuilder<Rule, RuleEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity;

    @Autowired
    public RuleBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RuleBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public RuleBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public RuleBuilder withFieldEntity(org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity) {
        this.fieldEntity = fieldEntity;
        return this;
    }

    @Override
    public List<Rule> build(FieldSet fields, List<RuleEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        FieldType fieldType = this.fieldEntity != null && this.fieldEntity.getData() != null ? this.fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;


        List<Rule> models = new ArrayList<>();
        for (RuleEntity d : data) {
            Rule m = new Rule();
            if (fields.hasField(this.asIndexer(Rule._target))) m.setTarget(d.getTarget());
            if (fields.hasField(this.asIndexer(Rule._dateValue))) m.setDateValue(d.getDateValue());
            if (fields.hasField(this.asIndexer(Rule._booleanValue))) m.setBooleanValue(d.getBooleanValue());
            if (fields.hasField(this.asIndexer(Rule._textValue))) m.setTextValue(d.getTextValue());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
