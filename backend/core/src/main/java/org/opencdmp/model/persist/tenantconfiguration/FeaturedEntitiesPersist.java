package org.opencdmp.model.persist.tenantconfiguration;


import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.featured.DescriptionTemplatePersist;
import org.opencdmp.model.persist.featured.PlanBlueprintPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class FeaturedEntitiesPersist {

    private List<PlanBlueprintPersist> planBlueprints;
    public static final String _planBlueprints = "planBlueprints";

    private List<DescriptionTemplatePersist> descriptionTemplates;
    public static final String _descriptionTemplates = "descriptionTemplates";

    public List<PlanBlueprintPersist> getPlanBlueprints() {
        return planBlueprints;
    }

    public void setPlanBlueprints(List<PlanBlueprintPersist> planBlueprints) {
        this.planBlueprints = planBlueprints;
    }

    public List<DescriptionTemplatePersist> getDescriptionTemplates() {
        return descriptionTemplates;
    }

    public void setDescriptionTemplates(List<DescriptionTemplatePersist> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    @Component(FeaturedEntitiesPersist.FeaturedEntitiesPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class FeaturedEntitiesPersistValidator extends BaseValidator<FeaturedEntitiesPersist> {

        public static final  String ValidatorName = "FeaturedEntitiesPersistValidator";

        private final MessageSource messageSource;

        public final ValidatorFactory validatorFactory;

        protected FeaturedEntitiesPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errorThesaurusProperties, MessageSource messageSource, ValidatorFactory validatorFactory){
            super(conventionService, errorThesaurusProperties);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<FeaturedEntitiesPersist> modelClass() { return FeaturedEntitiesPersist.class; }

        @Override
        protected List<Specification> specifications(FeaturedEntitiesPersist item) {
            return Arrays.asList(
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getPlanBlueprints()))
                            .on(FeaturedEntitiesPersist._planBlueprints)
                            .over(item.getPlanBlueprints())
                            .using((itm) -> this.validatorFactory.validator(PlanBlueprintPersist.PlanBlueprintPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .on(FeaturedEntitiesPersist._descriptionTemplates)
                            .over(item.getDescriptionTemplates())
                            .using((itm) -> this.validatorFactory.validator(DescriptionTemplatePersist.DescriptionTemplatePersistValidator.class))
            );
        }
    }
}
