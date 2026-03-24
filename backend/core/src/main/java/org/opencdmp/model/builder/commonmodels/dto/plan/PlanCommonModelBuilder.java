package org.opencdmp.model.builder.commonmodels.dto.plan;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.*;
import org.opencdmp.model.builder.commonmodels.dto.EntityDoiCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.dto.PlanDescriptionTemplateCommonBuilder;
import org.opencdmp.model.builder.commonmodels.dto.PlanUserCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.dto.UserCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.dto.description.DescriptionCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.dto.planreference.PlanReferenceCommonModelBuilder;
import org.opencdmp.model.plan.Plan;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PlanCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanCommonModelBuilder extends BaseCommonModelBuilder<Plan, PlanModel> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanCommonModelBuilder(ConventionService conventionService,
                                  BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PlanCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<Plan, PlanModel>> buildInternal(List<PlanModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Plan, PlanModel>> models = new ArrayList<>();

        for (PlanModel d : data) {
            Plan m = new Plan();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setVersion(d.getVersion());
            m.setDescription(d.getDescription());
            m.setFinalizedAt(d.getFinalizedAt());
            m.setCreatedAt(d.getCreatedAt());
            m.setLanguage(d.getLanguage());
            m.setStatus(this.builderFactory.builder(PlanStatusCommonModelBuilder.class).authorize(this.authorize).build(d.getStatus()));
            m.setEntityDois(this.builderFactory.builder(EntityDoiCommonModelBuilder.class).authorize(this.authorize).build(d.getEntityDois()));
            m.setCreator(this.builderFactory.builder(UserCommonModelBuilder.class).authorize(this.authorize).build(d.getCreator()));
            m.setBlueprint(this.builderFactory.builder(org.opencdmp.model.builder.commonmodels.dto.planblueprint.PlanBlueprintCommonModelBuilder.class).authorize(this.authorize).build(d.getPlanBlueprint()));
            m.setProperties(this.builderFactory.builder(PlanPropertiesCommonModelBuilder.class).authorize(this.authorize).build(d.getProperties()));
            m.setPublicAfter(d.getPublicAfter());
            m.setUpdatedAt(d.getUpdatedAt());
            m.setIsActive(IsActive.Active);
            m.setPlanDescriptionTemplates(this.builderFactory.builder(PlanDescriptionTemplateCommonBuilder.class).authorize(this.authorize).build(d.getDescriptionTemplates()));

            switch (d.getAccessType()){
                case Public -> m.setAccessType(PlanAccessType.Public);
                case Restricted -> m.setAccessType(PlanAccessType.Restricted);
                case null -> m.setAccessType(null);
                default -> throw new MyApplicationException("unrecognized type " + d.getAccessType());
            }

            m.setPlanReferences(this.builderFactory.builder(PlanReferenceCommonModelBuilder.class).authorize(this.authorize).build(d.getReferences()));
            m.setPlanUsers(this.builderFactory.builder(PlanUserCommonModelBuilder.class).authorize(this.authorize).build(d.getUsers()));
            m.setDescriptions(this.builderFactory.builder(DescriptionCommonModelBuilder.class).authorize(this.authorize).build(d.getDescriptions()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
