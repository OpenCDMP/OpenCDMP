package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlanSuggestion {

    private UUID planUpdateRequestId;
    public static final String _planUpdateRequestId = "planUpdateRequestId";

    private UUID blueprintId;
    public static final String _blueprintId = "blueprintId";

    private List<DescriptionCommonModelConfig> descriptions;
    public static final String _descriptions = "descriptions";

    public UUID getPlanUpdateRequestId() {
        return planUpdateRequestId;
    }

    public void setPlanUpdateRequestId(UUID planUpdateRequestId) {
        this.planUpdateRequestId = planUpdateRequestId;
    }

    public UUID getBlueprintId() {
        return blueprintId;
    }

    public void setBlueprintId(UUID blueprintId) {
        this.blueprintId = blueprintId;
    }

    public List<DescriptionCommonModelConfig> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<DescriptionCommonModelConfig> descriptions) {
        this.descriptions = descriptions;
    }

    @Component(PlanSuggestionValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanSuggestionValidator extends BaseValidator<PlanSuggestion> {

        public static final String ValidatorName = "PlanSuggestionValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected PlanSuggestionValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanSuggestion> modelClass() {
            return PlanSuggestion.class;
        }

        @Override
        protected List<Specification> specifications(PlanSuggestion item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanUpdateRequestId()))
                            .failOn(PlanSuggestion._planUpdateRequestId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanSuggestion._planUpdateRequestId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getBlueprintId()))
                            .failOn(PlanSuggestion._blueprintId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanSuggestion._blueprintId}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptions()))
                            .on(PlanSuggestion._descriptions)
                            .over(item.getDescriptions())
                            .using((itm) -> this.validatorFactory.validator(DescriptionCommonModelConfig.DescriptionCommonModelConfigValidator.class))
            );
        }
    }
}
