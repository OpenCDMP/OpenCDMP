package org.opencdmp.model.builder.commonmodels.description;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.DescriptionStatusModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;


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
public class DescriptionStatusCommonModelBuilder extends BaseCommonModelBuilder<DescriptionStatusModel, DescriptionStatusEntity> {
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
    protected List<CommonModelBuilderItemResponse<DescriptionStatusModel, DescriptionStatusEntity>> buildInternal(List<DescriptionStatusEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<DescriptionStatusModel, DescriptionStatusEntity>> models = new ArrayList<>();
        for (DescriptionStatusEntity d : data) {
            DescriptionStatusModel m = new DescriptionStatusModel();
            m.setId(d.getId());
            m.setName(d.getName());
            if (d.getInternalStatus() != null) {
                switch (d.getInternalStatus()){
                    case Finalized -> m.setInternalStatus(org.opencdmp.commonmodels.enums.DescriptionStatus.Finalized);
                    case Draft -> m.setInternalStatus(org.opencdmp.commonmodels.enums.DescriptionStatus.Draft);
                    case Canceled -> m.setInternalStatus(org.opencdmp.commonmodels.enums.DescriptionStatus.Canceled);
                    default -> throw new MyApplicationException("unrecognized type " + d.getInternalStatus());
                }
            }

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
