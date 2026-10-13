package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanUpdateRequestActionType;
import org.opencdmp.commons.enums.PlanUpdateRequestApplyDecision;
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
import java.util.Map;
import java.util.UUID;

public class PlanUpdateRequestApply {

    private UUID planUpdateRequestId;
    public static final String _planUpdateRequestId = "planUpdateRequestId";

    private PlanUpdateRequestApplyDecision decision;
    public static final String _decision = "decision";

    private PlanSuggestion suggestion;
    public static final String _suggestion = "suggestion";

    private List<UUID> acceptedChangeIds;
    public static final String _acceptedChangeIds = "acceptedChangeIds";

    private Map<UUID, UUID> newDescriptionTemplateSelections;
    public static final String _newDescriptionTemplateSelections = "newDescriptionTemplateSelections";

    private String previewPlanVersion;
    public static final String _previewPlanVersion = "previewPlanVersion";

    public UUID getPlanUpdateRequestId() {
        return planUpdateRequestId;
    }

    public void setPlanUpdateRequestId(UUID planUpdateRequestId) {
        this.planUpdateRequestId = planUpdateRequestId;
    }

    public PlanUpdateRequestApplyDecision getDecision() {
        return decision;
    }

    public void setDecision(PlanUpdateRequestApplyDecision decision) {
        this.decision = decision;
    }

    public PlanSuggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(PlanSuggestion suggestion) {
        this.suggestion = suggestion;
    }

    public List<UUID> getAcceptedChangeIds() {
        return acceptedChangeIds;
    }

    public void setAcceptedChangeIds(List<UUID> acceptedChangeIds) {
        this.acceptedChangeIds = acceptedChangeIds;
    }

    public Map<UUID, UUID> getNewDescriptionTemplateSelections() {
        return newDescriptionTemplateSelections;
    }

    public void setNewDescriptionTemplateSelections(Map<UUID, UUID> newDescriptionTemplateSelections) {
        this.newDescriptionTemplateSelections = newDescriptionTemplateSelections;
    }

    public String getPreviewPlanVersion() {
        return previewPlanVersion;
    }

    public void setPreviewPlanVersion(String previewPlanVersion) {
        this.previewPlanVersion = previewPlanVersion;
    }

    @Component(PlanUpdateRequestApplyValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanUpdateRequestApplyValidator extends BaseValidator<PlanUpdateRequestApply> {

        public static final String ValidatorName = "PlanUpdateRequestApplyValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected PlanUpdateRequestApplyValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanUpdateRequestApply> modelClass() {
            return PlanUpdateRequestApply.class;
        }

        @Override
        protected List<Specification> specifications(PlanUpdateRequestApply item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanUpdateRequestId()))
                            .failOn(PlanUpdateRequestApply._planUpdateRequestId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestApply._planUpdateRequestId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getDecision()))
                            .failOn(PlanUpdateRequestApply._decision)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestApply._decision}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getDecision() == PlanUpdateRequestApplyDecision.PartialAccept)
                            .must(() -> !this.isListNullOrEmpty(item.getAcceptedChangeIds()))
                            .failOn(PlanUpdateRequestApply._acceptedChangeIds)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanUpdateRequestApply._acceptedChangeIds}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> item.getSuggestion() != null)
                            .on(PlanUpdateRequestApply._suggestion)
                            .over(item.getSuggestion())
                            .using(() -> this.validatorFactory.validator(PlanSuggestion.PlanSuggestionValidator.class))
            );
        }
    }
}
