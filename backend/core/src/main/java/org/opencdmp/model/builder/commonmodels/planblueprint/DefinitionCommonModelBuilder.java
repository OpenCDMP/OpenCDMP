package org.opencdmp.model.builder.commonmodels.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.DefinitionModel;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
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

@Component("planblueprint.DefinitionCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefinitionCommonModelBuilder extends BaseCommonModelBuilder<DefinitionModel, DefinitionEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public DefinitionCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefinitionCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public DefinitionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<DefinitionModel, DefinitionEntity>> buildInternal(List<DefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<DefinitionModel, DefinitionEntity>> models = new ArrayList<>();
        for (DefinitionEntity d : data) {
            DefinitionModel m = new DefinitionModel();
            if (d.getSections() != null) m.setSections(this.builderFactory.builder(SectionCommonModelBuilder.class).authorize(this.authorize).build(d.getSections()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
