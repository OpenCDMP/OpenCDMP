package org.opencdmp.model.persist.planworkflow;

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

public class PlanWorkflowDefinitionTransitionPersist {

    public final static String _fromStatusId = "fromStatusId";
    private UUID fromStatusId;

    public final static String _toStatusId = "toStatusId";
    private UUID toStatusId;

    public UUID getFromStatusId() { return fromStatusId; }

    public void setFromStatusId(UUID fromStatusId) { this.fromStatusId = fromStatusId; }

    public UUID getToStatusId() { return toStatusId; }

    public void setToStatusId(UUID toStatusId) { this.toStatusId = toStatusId; }


    @Component(PlanWorkflowDefinitionTransitionPersist.PlanWorkflowDefinitionTransitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanWorkflowDefinitionTransitionPersistValidator extends BaseValidator<PlanWorkflowDefinitionTransitionPersist> {

        public static final String ValidatorName = "PlanWorkflowPersistValidator.PlanWorkflowDefinitionPersistValidator.PlanWorkflowDefinitionTransitionPersistValidator";

        private final MessageSource messageSource;

        protected PlanWorkflowDefinitionTransitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanWorkflowDefinitionTransitionPersist> modelClass() { return PlanWorkflowDefinitionTransitionPersist.class; }

        @Override
        protected List<Specification> specifications(PlanWorkflowDefinitionTransitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getFromStatusId()))
                            .failOn(PlanWorkflowDefinitionTransitionPersist._fromStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWorkflowDefinitionTransitionPersist._fromStatusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getToStatusId()))
                            .failOn(PlanWorkflowDefinitionTransitionPersist._toStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWorkflowDefinitionTransitionPersist._toStatusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getFromStatusId()) && this.isValidGuid(item.getToStatusId()))
                            .must(() -> !item.getFromStatusId().equals(item.getToStatusId()))
                            .failOn(PlanWorkflowDefinitionTransitionPersist._toStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWorkflowDefinitionTransitionPersist._toStatusId}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
