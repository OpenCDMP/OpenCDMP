package org.opencdmp.model.builder.commonmodels.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.SectionModel;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.types.planblueprint.*;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("planblueprint.SectionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SectionCommonModelBuilder extends BaseCommonModelBuilder<SectionModel, SectionEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public SectionCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SectionCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public SectionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<SectionModel, SectionEntity>> buildInternal(List<SectionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<SectionModel, SectionEntity>> models = new ArrayList<>();
        for (SectionEntity d : data) {
            SectionModel m = new SectionModel();
            m.setId(d.getId());
            m.setDescription(d.getDescription());
            m.setLabel(d.getLabel());
            m.setOrdinal(d.getOrdinal());
            m.setHasTemplates(d.getHasTemplates());
            m.setCanEditDescriptionTemplates(d.getCanEditDescriptionTemplates());

            if ( d.getFields() != null) {
                m.setFields(new ArrayList<>());
                List<SystemFieldEntity> systemFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.System.equals(x.getCategory())).map(x-> (SystemFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(SystemFieldCommonModelBuilder.class).authorize(this.authorize).build(systemFieldEntities));
                List<ExtraFieldEntity> extraFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.Extra.equals(x.getCategory())).map(x-> (ExtraFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(ExtraFieldCommonModelBuilder.class).authorize(this.authorize).build(extraFieldEntities));
                List<ReferenceTypeFieldEntity> referenceFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.ReferenceType.equals(x.getCategory())).map(x-> (ReferenceTypeFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(ReferenceTypeFieldCommonModelBuilder.class).authorize(this.authorize).build(referenceFieldEntities));
                List<UploadFieldEntity> uploadFieldEntities = d.getFields().stream().filter(x-> PlanBlueprintFieldCategory.Upload.equals(x.getCategory())).map(x-> (UploadFieldEntity)x).toList();
                m.getFields().addAll(this.builderFactory.builder(UploadFieldCommonModelBuilder.class).authorize(this.authorize).build(uploadFieldEntities));
            }

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
