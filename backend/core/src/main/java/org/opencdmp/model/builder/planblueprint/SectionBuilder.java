package org.opencdmp.model.builder.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.commons.types.planblueprint.ReferenceTypeFieldEntity;
import org.opencdmp.commons.types.planblueprint.SectionEntity;
import org.opencdmp.commons.types.planblueprint.SystemFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PrefillingSourceEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.prefillingsource.PrefillingSourceBuilder;
import org.opencdmp.model.planblueprint.Section;
import org.opencdmp.query.PrefillingSourceQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("planblueprintdefinitionsectionbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SectionBuilder extends BaseBuilder<Section, SectionEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public SectionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SectionBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
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
        FieldSet descriptionTemplatesFields = fields.extractPrefixed(this.asPrefix(Section._descriptionTemplates));
        FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(Section._fields));
        FieldSet prefillingSourcesFields = fields.extractPrefixed(this.asPrefix(Section._prefillingSources));

        List<Section> models = new ArrayList<>();
        for (SectionEntity d : data) {
            Section m = new Section();
            if (fields.hasField(this.asIndexer(Section._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Section._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(Section._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(Section._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(Section._hasTemplates))) m.setHasTemplates(d.getHasTemplates());
            if (fields.hasField(this.asIndexer(Section._prefillingSourcesEnabled))) m.setPrefillingSourcesEnabled(d.getPrefillingSourcesEnabled());
            if (!descriptionTemplatesFields.isEmpty() && d.getDescriptionTemplates() != null) m.setDescriptionTemplates(this.builderFactory.builder(BlueprintDescriptionTemplateBuilder.class).authorize(this.authorize).build(descriptionTemplatesFields, d.getDescriptionTemplates()));
            if (!fieldsFields.isEmpty() && d.getFields() != null) {
                m.setFields(new ArrayList<>());
                List<SystemFieldEntity> systemFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.System.equals(x.getCategory())).map(x-> (SystemFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(SystemFieldBuilder.class).authorize(this.authorize).build(fieldsFields, systemFieldEntities));
                List<ExtraFieldEntity> extraFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.Extra.equals(x.getCategory())).map(x-> (ExtraFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(ExtraFieldBuilder.class).authorize(this.authorize).build(fieldsFields, extraFieldEntities));
                List<ReferenceTypeFieldEntity> referenceFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.ReferenceType.equals(x.getCategory())).map(x-> (ReferenceTypeFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(ReferenceFieldBuilder.class).authorize(this.authorize).build(fieldsFields, referenceFieldEntities));
            }
            if (!prefillingSourcesFields.isEmpty() && d.getPrefillingSourcesIds() != null) {
                List<PrefillingSourceEntity> prefillingSourceEntities = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().authorize(this.authorize).ids(d.getPrefillingSourcesIds()).collectAs(prefillingSourcesFields);
                m.setPrefillingSources(this.builderFactory.builder(PrefillingSourceBuilder.class).authorize(this.authorize).build(prefillingSourcesFields, prefillingSourceEntities));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
