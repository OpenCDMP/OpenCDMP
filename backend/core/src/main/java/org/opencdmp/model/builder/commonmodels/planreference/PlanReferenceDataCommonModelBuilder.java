package org.opencdmp.model.builder.commonmodels.planreference;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceDataModel;
import org.opencdmp.commons.types.planreference.PlanReferenceDataEntity;
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
public class PlanReferenceDataCommonModelBuilder extends BaseCommonModelBuilder<PlanReferenceDataModel, PlanReferenceDataEntity> {

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
    protected List<CommonModelBuilderItemResponse<PlanReferenceDataModel, PlanReferenceDataEntity>> buildInternal(List<PlanReferenceDataEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanReferenceDataModel, PlanReferenceDataEntity>> models = new ArrayList<>();
        for (PlanReferenceDataEntity d : data) {
            PlanReferenceDataModel m = new PlanReferenceDataModel();
            m.setBlueprintFieldId(d.getBlueprintFieldId());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
