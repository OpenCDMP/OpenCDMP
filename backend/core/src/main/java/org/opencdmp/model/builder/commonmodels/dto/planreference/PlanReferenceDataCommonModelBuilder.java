package org.opencdmp.model.builder.commonmodels.dto.planreference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.planreference.PlanReferenceData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PlanReferenceDataCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanReferenceDataCommonModelBuilder extends BaseCommonModelBuilder<PlanReferenceData, PlanReferenceDataModel> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanReferenceDataCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanReferenceDataCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PlanReferenceDataCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanReferenceData, PlanReferenceDataModel>> buildInternal(List<PlanReferenceDataModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanReferenceData, PlanReferenceDataModel>> models = new ArrayList<>();
        for (PlanReferenceDataModel d : data) {
            PlanReferenceData m = new PlanReferenceData();
            m.setBlueprintFieldId(d.getBlueprintFieldId());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
