package org.opencdmp.model.builder.commonmodels.dto.plan;


import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plan.PlanBlueprintValueModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.plan.PlanBlueprintValue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PlanBlueprintValueCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintValueCommonModelBuilder extends BaseCommonModelBuilder<PlanBlueprintValue, PlanBlueprintValueModel> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanBlueprintValueCommonModelBuilder(
            ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintValueCommonModelBuilder.class)));
    }

    public PlanBlueprintValueCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    @Override
    protected List<CommonModelBuilderItemResponse<PlanBlueprintValue, PlanBlueprintValueModel>> buildInternal(List<PlanBlueprintValueModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanBlueprintValue, PlanBlueprintValueModel>> models = new ArrayList<>();
        for (PlanBlueprintValueModel d : data) {
            PlanBlueprintValue m = new PlanBlueprintValue();
            m.setFieldId(d.getFieldId());
            m.setFieldValue(d.getValue());
            m.setDateValue(d.getDateValue());
            m.setNumberValue(d.getNumberValue());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
