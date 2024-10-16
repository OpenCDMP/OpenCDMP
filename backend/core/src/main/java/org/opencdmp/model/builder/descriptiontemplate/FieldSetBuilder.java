package org.opencdmp.model.builder.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldSetBuilder extends BaseBuilder<org.opencdmp.model.descriptiontemplate.FieldSet, FieldSetEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public FieldSetBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldSetBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public FieldSetBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<org.opencdmp.model.descriptiontemplate.FieldSet> build(FieldSet fields, List<FieldSetEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(org.opencdmp.model.descriptiontemplate.FieldSet._fields));
        FieldSet multiplicityFields = fields.extractPrefixed(this.asPrefix(org.opencdmp.model.descriptiontemplate.FieldSet._multiplicity));

        List<org.opencdmp.model.descriptiontemplate.FieldSet> models = new ArrayList<>();
        for (FieldSetEntity d : data) {
            org.opencdmp.model.descriptiontemplate.FieldSet m = new org.opencdmp.model.descriptiontemplate.FieldSet();
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._title))) m.setTitle(d.getTitle());
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._extendedDescription))) m.setExtendedDescription(d.getExtendedDescription());
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._additionalInformation))) m.setAdditionalInformation(d.getAdditionalInformation());
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._hasCommentField))) m.setHasCommentField(d.getHasCommentField());
            if (fields.hasField(this.asIndexer(org.opencdmp.model.descriptiontemplate.FieldSet._hasMultiplicity))) m.setHasMultiplicity(d.getHasMultiplicity());
            if (!multiplicityFields.isEmpty() && d.getMultiplicity() != null) m.setMultiplicity(this.builderFactory.builder(MultiplicityBuilder.class).authorize(this.authorize).build(multiplicityFields, d.getMultiplicity()));
            if (!fieldsFields.isEmpty() && d.getFields() != null) m.setFields(this.builderFactory.builder(FieldBuilder.class).authorize(this.authorize).build(fieldsFields, d.getFields()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
