package org.opencdmp.model.builder.commonmodels.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.RootSectionModel;
import org.opencdmp.commons.types.planblueprint.*;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.FieldSetCommonModelBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("planblueprint.SectionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RootSectionCommonModelBuilder extends BaseCommonModelBuilder<RootSectionModel, RootSectionEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public RootSectionCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RootSectionCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public RootSectionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<RootSectionModel, RootSectionEntity>> buildInternal(List<RootSectionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<RootSectionModel, RootSectionEntity>> models = new ArrayList<>();
        for (RootSectionEntity d : data) {
            RootSectionModel m = new RootSectionModel();
            m.setId(d.getId());
            m.setDescription(d.getDescription());
            m.setOrdinal(d.getOrdinal());
            m.setTitle(d.getTitle());
            m.setExtendedDescription(d.getExtendedDescription());
            m.setSemantics(d.getSemantics());
            if (d.getSections() != null) m.setSections(this.builderFactory.builder(org.opencdmp.model.builder.commonmodels.descriptiontemplate.SectionCommonModelBuilder.class).authorize(this.authorize).build(d.getSections()));
            if (d.getFieldSets() != null) m.setFieldSets(this.builderFactory.builder(FieldSetCommonModelBuilder.class).authorize(this.authorize).build(d.getFieldSets()));
            m.setHasTemplates(d.getHasTemplates());
            m.setCanEditDescriptionTemplates(d.getCanEditDescriptionTemplates());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
