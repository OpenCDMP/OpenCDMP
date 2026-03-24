package org.opencdmp.model.builder.commonmodels.dto.plan;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plan.PlanContactModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.plan.PlanContact;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PlanContactCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanContactCommonModelBuilder extends BaseCommonModelBuilder<PlanContact, PlanContactModel> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    @Autowired
    public PlanContactCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanContactCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public PlanContactCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanContact, PlanContactModel>> buildInternal(List<PlanContactModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanContact, PlanContactModel>> models = new ArrayList<>();
        for (PlanContactModel d : data) {
            PlanContact m = new PlanContact();
            m.setEmail(d.getEmail());
            m.setFirstName(d.getFirstName());
            m.setLastName(d.getLastName());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
