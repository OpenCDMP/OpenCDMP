package org.opencdmp.model.builder.commonmodels.dto.planreference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.dto.reference.ReferenceCommonModelBuilder;
import org.opencdmp.model.planreference.PlanReference;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PlanReferenceCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanReferenceCommonModelBuilder extends BaseCommonModelBuilder<PlanReference, PlanReferenceModel> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanReferenceCommonModelBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, JsonHandlingService jsonHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanReferenceCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
	    this.jsonHandlingService = jsonHandlingService;
    }

    public PlanReferenceCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanReference, PlanReferenceModel>> buildInternal(List<PlanReferenceModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanReference, PlanReferenceModel>> models = new ArrayList<>();
        for (PlanReferenceModel d : data) {
            PlanReference m = new PlanReference();
            m.setId(d.getId());
            m.setIsActive(IsActive.Active);
            m.setData(this.builderFactory.builder(PlanReferenceDataCommonModelBuilder.class).authorize(this.authorize).build(d.getData()));
            m.setReference(this.builderFactory.builder(ReferenceCommonModelBuilder.class).authorize(this.authorize).build(d.getReference()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
