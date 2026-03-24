package org.opencdmp.model.builder.commonmodels.dto;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.DescriptionTemplateTypeModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.DescriptionTemplateType;
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

@Component("dto.DescriptionTemplateTypeCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateTypeCommonModelBuilder extends BaseCommonModelBuilder<DescriptionTemplateType, DescriptionTemplateTypeModel> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DescriptionTemplateTypeCommonModelBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionTemplateTypeCommonModelBuilder.class)));
    }

    public DescriptionTemplateTypeCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<DescriptionTemplateType, DescriptionTemplateTypeModel>> buildInternal(List<DescriptionTemplateTypeModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<DescriptionTemplateType, DescriptionTemplateTypeModel>> models = new ArrayList<>();
        for (DescriptionTemplateTypeModel d : data) {
            DescriptionTemplateType m = new DescriptionTemplateType();
            m.setId(d.getId());
            m.setName(d.getName());
            m.setIsActive(IsActive.Active);
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
