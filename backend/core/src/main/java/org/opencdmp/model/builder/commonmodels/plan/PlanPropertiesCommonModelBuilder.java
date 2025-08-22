package org.opencdmp.model.builder.commonmodels.plan;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanPropertiesCommonModelBuilder extends BaseCommonModelBuilder<PlanPropertiesModel, PlanPropertiesEntity> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private DefinitionEntity definition;
    @Autowired
    public PlanPropertiesCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanPropertiesCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PlanPropertiesCommonModelBuilder withDefinition(DefinitionEntity definition) {
        this.definition = definition;
        return this;
    }

    public PlanPropertiesCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    private boolean useSharedStorage;
    public PlanPropertiesCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanPropertiesModel, PlanPropertiesEntity>> buildInternal(List<PlanPropertiesEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanPropertiesModel, PlanPropertiesEntity>> models = new ArrayList<>();
        for (PlanPropertiesEntity d : data) {
            PlanPropertiesModel m = new PlanPropertiesModel();
            if (d.getPlanBlueprintValues() != null) m.setPlanBlueprintValues(this.builderFactory.builder(PlanBlueprintValueCommonModelBuilder.class).useSharedStorage(useSharedStorage).withDefinition(definition).authorize(this.authorize).build(d.getPlanBlueprintValues()));
            if (d.getContacts() != null) m.setContacts(this.builderFactory.builder(PlanContactCommonModelBuilder.class).authorize(this.authorize).build(d.getContacts()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
