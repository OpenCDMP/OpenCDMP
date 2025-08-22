package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.types.planblueprint.*;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.PublicPlanBlueprintSection;
import org.opencdmp.model.builder.planblueprint.ExtraFieldBuilder;
import org.opencdmp.model.builder.planblueprint.ReferenceFieldBuilder;
import org.opencdmp.model.builder.planblueprint.SystemFieldBuilder;
import org.opencdmp.model.builder.planblueprint.UploadFieldBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanBlueprintSectionBuilder extends BaseBuilder<PublicPlanBlueprintSection, SectionEntity>{

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicPlanBlueprintSectionBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanBlueprintSectionBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PublicPlanBlueprintSectionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanBlueprintSection> build(FieldSet fields, List<SectionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(PublicPlanBlueprintSection._fields));

        List<PublicPlanBlueprintSection> models = new ArrayList<>();
        for (SectionEntity d : data) {
            PublicPlanBlueprintSection m = new PublicPlanBlueprintSection();
            if (fields.hasField(this.asIndexer(PublicPlanBlueprintSection._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicPlanBlueprintSection._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(PublicPlanBlueprintSection._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(PublicPlanBlueprintSection._hasTemplates))) m.setHasTemplates(d.getHasTemplates());
            if (!fieldsFields.isEmpty() && d.getFields() != null) {
                m.setFields(new ArrayList<>());
                List<SystemFieldEntity> systemFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.System.equals(x.getCategory())).map(x-> (SystemFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(SystemFieldBuilder.class).authorize(this.authorize).build(fieldsFields, systemFieldEntities));
                List<ExtraFieldEntity> extraFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.Extra.equals(x.getCategory())).map(x-> (ExtraFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(ExtraFieldBuilder.class).authorize(this.authorize).build(fieldsFields, extraFieldEntities));
                List<ReferenceTypeFieldEntity> referenceFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.ReferenceType.equals(x.getCategory())).map(x-> (ReferenceTypeFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(ReferenceFieldBuilder.class).authorize(this.authorize).build(fieldsFields, referenceFieldEntities));
                List<UploadFieldEntity> uploadFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.Upload.equals(x.getCategory())).map(x-> (UploadFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(UploadFieldBuilder.class).authorize(this.authorize).build(fieldsFields, uploadFieldEntities));

            }

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
