package org.opencdmp.model.builder.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planblueprint.RootSectionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PrefillingSourceEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.descriptiontemplate.FieldSetBuilder;
import org.opencdmp.model.builder.descriptiontemplate.SectionBuilder;
import org.opencdmp.model.builder.prefillingsource.PrefillingSourceBuilder;
import org.opencdmp.model.planblueprint.RootSection;
import org.opencdmp.query.PrefillingSourceQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("planblueprintdefinitionsectionbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RootSectionBuilder extends BaseBuilder<RootSection, RootSectionEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private boolean isPublic;

    @Autowired
    public RootSectionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RootSectionBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public RootSectionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public RootSectionBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    @Override
    public List<RootSection> build(FieldSet fields, List<RootSectionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet descriptionTemplatesFields = fields.extractPrefixed(this.asPrefix(RootSection._descriptionTemplates));
        FieldSet fieldSetsFields = fields.extractPrefixed(this.asPrefix(RootSection._fieldSets));
        FieldSet prefillingSourcesFields = fields.extractPrefixed(this.asPrefix(RootSection._prefillingSources));

        List<RootSection> models = new ArrayList<>();
        for (RootSectionEntity d : data) {
            RootSection m = new RootSection();
            if (fields.hasField(this.asIndexer(RootSection._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(RootSection._title))) m.setTitle(d.getTitle());
            if (fields.hasField(this.asIndexer(RootSection._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(RootSection._extendedDescription))) m.setExtendedDescription(d.getExtendedDescription());
            if (fields.hasField(this.asIndexer(RootSection._semantics))) m.setSemantics(d.getSemantics());
            if (!this.isPublic && fields.hasField(this.asIndexer(RootSection._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(RootSection._hasTemplates))) m.setHasTemplates(d.getHasTemplates());
            if (!this.isPublic && fields.hasField(this.asIndexer(RootSection._prefillingSourcesEnabled))) m.setPrefillingSourcesEnabled(d.getPrefillingSourcesEnabled());
            if (!this.isPublic && fields.hasField(this.asIndexer(RootSection._canEditDescriptionTemplates))) m.setCanEditDescriptionTemplates(d.getCanEditDescriptionTemplates());
            if (!this.isPublic && !descriptionTemplatesFields.isEmpty() && d.getDescriptionTemplates() != null)
                m.setDescriptionTemplates(this.builderFactory.builder(BlueprintDescriptionTemplateBuilder.class).authorize(this.authorize).build(descriptionTemplatesFields, d.getDescriptionTemplates()));
            if (!fieldSetsFields.isEmpty() && d.getFieldSets() != null)
                m.setFieldSets(this.builderFactory.builder(FieldSetBuilder.class).authorize(this.authorize).build(fieldSetsFields, d.getFieldSets()));
            if (d.getSections() != null)
                m.setSections(this.builderFactory.builder(SectionBuilder.class).authorize(this.authorize).build(fields, d.getSections()));
            if (!this.isPublic && !prefillingSourcesFields.isEmpty() && d.getPrefillingSourcesIds() != null) {
                List<PrefillingSourceEntity> prefillingSourceEntities = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().authorize(this.authorize).ids(d.getPrefillingSourcesIds()).collectAs(prefillingSourcesFields);
                m.setPrefillingSources(this.builderFactory.builder(PrefillingSourceBuilder.class).authorize(this.authorize).build(prefillingSourcesFields, prefillingSourceEntities));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
