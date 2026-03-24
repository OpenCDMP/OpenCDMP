package org.opencdmp.model.builder.commonmodels.dto.description;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.DescriptionStatusModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.DescriptionStatusCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusCommonModelBuilder extends BaseCommonModelBuilder<DescriptionStatus, DescriptionStatusModel> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public DescriptionStatusCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionStatusCommonModelBuilder.class)));
    }

    public DescriptionStatusCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    @Override
    protected List<CommonModelBuilderItemResponse<DescriptionStatus, DescriptionStatusModel>> buildInternal(List<DescriptionStatusModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<DescriptionStatus, DescriptionStatusModel>> models = new ArrayList<>();
        for (DescriptionStatusModel d : data) {
            DescriptionStatus m = new DescriptionStatus();
            m.setId(d.getId());
            m.setName(d.getName());
            m.setIsActive(IsActive.Active);
            if (d.getInternalStatus() != null) {
                switch (d.getInternalStatus()){
                    case Finalized -> m.setInternalStatus(org.opencdmp.commons.enums.DescriptionStatus.Finalized);
                    case Draft -> m.setInternalStatus(org.opencdmp.commons.enums.DescriptionStatus.Draft);
                    case Canceled -> m.setInternalStatus(org.opencdmp.commons.enums.DescriptionStatus.Canceled);
                    default -> throw new MyApplicationException("unrecognized type " + d.getInternalStatus());
                }
            }

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
