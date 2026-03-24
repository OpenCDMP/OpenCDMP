package org.opencdmp.model.builder.commonmodels.dto.plan;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.plan.PlanProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PlanPropertiesCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanPropertiesCommonModelBuilder extends BaseCommonModelBuilder<PlanProperties, PlanPropertiesModel> {
    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanPropertiesCommonModelBuilder(
		    ConventionService conventionService,  BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanPropertiesCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PlanPropertiesCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanProperties, PlanPropertiesModel>> buildInternal(List<PlanPropertiesModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanProperties, PlanPropertiesModel>> models = new ArrayList<>();
        for (PlanPropertiesModel d : data) {
            PlanProperties m = new PlanProperties();
            if (d.getPlanBlueprintValues() != null) m.setPlanBlueprintValues(this.builderFactory.builder(PlanBlueprintValueCommonModelBuilder.class).authorize(this.authorize).build(d.getPlanBlueprintValues()));
            if (d.getContacts() != null) m.setContacts(this.builderFactory.builder(PlanContactCommonModelBuilder.class).authorize(this.authorize).build(d.getContacts()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
