package org.opencdmp.model.builder.commonmodels.dto;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plandescriptiontemplate.PlanDescriptionTemplateModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PlanDescriptionTemplateCommonBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanDescriptionTemplateCommonBuilder extends BaseCommonModelBuilder<PlanDescriptionTemplate, PlanDescriptionTemplateModel> {

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanDescriptionTemplateCommonBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanDescriptionTemplateCommonBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PlanDescriptionTemplateCommonBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanDescriptionTemplate, PlanDescriptionTemplateModel>> buildInternal(List<PlanDescriptionTemplateModel> data) throws MyApplicationException {
        this.logger.debug("building for {} items ", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null)
            return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanDescriptionTemplate, PlanDescriptionTemplateModel>> models = new ArrayList<>();
        for (PlanDescriptionTemplateModel d : data) {
            PlanDescriptionTemplate m = new PlanDescriptionTemplate();

            m.setSectionId(d.getSectionId());
            m.setDescriptionTemplateGroupId(d.getDescriptionTemplateGroupId());
            m.setIsActive(IsActive.Active);

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }


}
