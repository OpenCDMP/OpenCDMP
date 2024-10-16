package org.opencdmp.model.builder.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.Section;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("descriptiontemplatedefinitionsectionbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SectionBuilder extends BaseBuilder<Section, SectionEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public SectionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SectionBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public SectionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Section> build(FieldSet fields, List<SectionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet fieldSetsFields = fields.extractPrefixed(this.asPrefix(Section._fieldSets));

        List<Section> models = new ArrayList<>();
        for (SectionEntity d : data) {
            Section m = new Section();
            if (fields.hasField(this.asIndexer(Section._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Section._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(Section._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(Section._title))) m.setTitle(d.getTitle());
            if (fields.hasField(this.asIndexer(Section._extendedDescription))) m.setExtendedDescription(d.getExtendedDescription());
            if (d.getSections() != null) m.setSections(this.builderFactory.builder(SectionBuilder.class).authorize(this.authorize).build(fields, d.getSections()));
            if (!fieldSetsFields.isEmpty() && d.getFieldSets() != null) m.setFieldSets(this.builderFactory.builder(FieldSetBuilder.class).authorize(this.authorize).build(fieldSetsFields, d.getFieldSets()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
