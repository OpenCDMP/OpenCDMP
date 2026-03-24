package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.SectionModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.descriptiontemplate.Section;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.SectionCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SectionCommonModelBuilder extends BaseCommonModelBuilder<Section, SectionModel> {
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
    protected List<CommonModelBuilderItemResponse<Section, SectionModel>> buildInternal(List<SectionModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Section, SectionModel>> models = new ArrayList<>();
        for (SectionModel d : data) {
            Section m = new Section();
            m.setId(d.getId());
            m.setDescription(d.getDescription());
            m.setOrdinal(d.getOrdinal());
            m.setTitle(d.getTitle());
            m.setExtendedDescription(d.getExtendedDescription());
            if (d.getSections() != null) m.setSections(this.builderFactory.builder(SectionCommonModelBuilder.class).authorize(this.authorize).build(d.getSections()));
            if (d.getFieldSets() != null) m.setFieldSets(this.builderFactory.builder(FieldSetCommonModelBuilder.class).authorize(this.authorize).build(d.getFieldSets()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
