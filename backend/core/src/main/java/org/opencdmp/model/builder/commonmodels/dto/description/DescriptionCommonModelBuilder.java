package org.opencdmp.model.builder.commonmodels.dto.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.description.Description;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.DescriptionCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionCommonModelBuilder extends BaseCommonModelBuilder<Description, DescriptionModel> {

    private final BuilderFactory builderFactory;
    
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DescriptionCommonModelBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public DescriptionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<Description, DescriptionModel>> buildInternal(List<DescriptionModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Description, DescriptionModel>> models = new ArrayList<>();
        for (DescriptionModel d : data) {
            Description m = new Description();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            m.setStatus(this.builderFactory.builder(DescriptionStatusCommonModelBuilder.class).authorize(this.authorize).build(d.getStatus()));
            m.setCreatedAt(d.getCreatedAt());
            m.setDescription(d.getDescription());
            m.setIsActive(IsActive.Active);
            m.setPlan(this.builderFactory.builder(org.opencdmp.model.builder.commonmodels.dto.plan.PlanCommonModelBuilder.class).authorize(this.authorize).build(d.getPlan()));
            m.setPlanDescriptionTemplate(this.buildPlanDescriptionTemplate(d));
            m.setDescriptionTemplate(this.builderFactory.builder(org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.DescriptionTemplateCommonModelBuilder.class).authorize(this.authorize).build(d.getDescriptionTemplate()));
            if (d.getDescriptionTemplate() != null) m.setProperties(this.builderFactory.builder(PropertyDefinitionCommonModelBuilder.class).withDefinitionModel(d.getDescriptionTemplate().getDefinition()).authorize(this.authorize).build(d.getProperties()));
            if (d.getTags() != null && !d.getTags().isEmpty()) {
                List<DescriptionTag> descriptionTags = new ArrayList<>();
                for (String value: d.getTags()) {
                    DescriptionTag descriptionTag = new DescriptionTag();
                    descriptionTag.setIsActive(IsActive.Active);
                    Tag tag = new Tag();
                    tag.setLabel(value);
                    tag.setIsActive(IsActive.Active);
                    descriptionTag.setTag(tag);

                    descriptionTags.add(descriptionTag);
                }
                m.setDescriptionTags(descriptionTags);

            }
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private PlanDescriptionTemplate buildPlanDescriptionTemplate(DescriptionModel data) {
        PlanDescriptionTemplate planDescriptionTemplate = new PlanDescriptionTemplate();

        if (data.getDescriptionTemplate() != null) planDescriptionTemplate.setDescriptionTemplateGroupId(data.getDescriptionTemplate().getGroupId());

        planDescriptionTemplate.setSectionId(data.getSectionId());
        planDescriptionTemplate.setPlan(this.builderFactory.builder(org.opencdmp.model.builder.commonmodels.dto.plan.PlanCommonModelBuilder.class).authorize(this.authorize).build(data.getPlan()));
        planDescriptionTemplate.setCurrentDescriptionTemplate(this.builderFactory.builder(org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.DescriptionTemplateCommonModelBuilder.class).authorize(this.authorize).build(data.getDescriptionTemplate()));
        planDescriptionTemplate.setIsActive(IsActive.Active);

        return planDescriptionTemplate;
    }

}
