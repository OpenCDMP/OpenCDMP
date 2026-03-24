package org.opencdmp.model.builder.commonmodels.dto.plan;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plan.PlanStatusModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.planstatus.PlanStatus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.PlanStatusCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanStatusCommonModelBuilder extends BaseCommonModelBuilder<PlanStatus, PlanStatusModel> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PlanStatusCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanStatusCommonModelBuilder.class)));
    }

    public PlanStatusCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    @Override
    protected List<CommonModelBuilderItemResponse<PlanStatus, PlanStatusModel>> buildInternal(List<PlanStatusModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanStatus, PlanStatusModel>> models = new ArrayList<>();
        for (PlanStatusModel d : data) {
            PlanStatus m = new PlanStatus();
            m.setId(d.getId());
            m.setName(d.getName());
            m.setIsActive(IsActive.Active);
            if (d.getInternalStatus() != null) {
                switch (d.getInternalStatus()){
                    case Finalized -> m.setInternalStatus(org.opencdmp.commons.enums.PlanStatus.Finalized);
                    case Draft -> m.setInternalStatus(org.opencdmp.commons.enums.PlanStatus.Draft);
                    default -> throw new MyApplicationException("unrecognized type " + d.getInternalStatus());
                }
            }

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
