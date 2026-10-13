package org.opencdmp.model.builder.commonmodels.dto.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.RootSectionModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.RootSectionCommonModelBuilder;
import org.opencdmp.model.planblueprint.RootSection;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.planblueprint.SectionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SectionCommonModelBuilder extends BaseCommonModelBuilder<RootSection, RootSectionModel> {
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
    protected List<CommonModelBuilderItemResponse<RootSection, RootSectionModel>> buildInternal(List<RootSectionModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<RootSection, RootSectionModel>> models = new ArrayList<>();
        for (RootSectionModel d : data) {
            RootSection m = new RootSection();
            m.setId(d.getId());
            m.setDescription(d.getDescription());
            m.setOrdinal(d.getOrdinal());
            m.setTitle(d.getTitle());
            m.setExtendedDescription(d.getExtendedDescription());
            m.setSemantics(d.getSemantics());
            if (d.getSections() != null) m.setSections(this.builderFactory.builder(RootSectionCommonModelBuilder.class).authorize(this.authorize).build(d.getSections()));

            m.setHasTemplates(d.getHasTemplates());
            m.setCanEditDescriptionTemplates(d.getCanEditDescriptionTemplates());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
