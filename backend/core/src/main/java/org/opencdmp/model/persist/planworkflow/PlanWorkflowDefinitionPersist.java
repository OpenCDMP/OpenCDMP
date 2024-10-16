package org.opencdmp.model.persist.planworkflow;

import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlanWorkflowDefinitionPersist {

    public final static String _startingStatusId = "startingStatusId";
    private UUID startingStatusId;

    public final static String _statusTransitions = "statusTransitions";
    private List<PlanWorkflowDefinitionTransitionPersist> statusTransitions = null;

    public UUID getStartingStatusId() { return startingStatusId; }

    public void setStartingStatusId(UUID startingStatusId) { this.startingStatusId = startingStatusId; }

    public List<PlanWorkflowDefinitionTransitionPersist> getStatusTransitions() { return statusTransitions; }

    public void setStatusTransitions(List<PlanWorkflowDefinitionTransitionPersist> statusTransitions) { this.statusTransitions = statusTransitions; }

    @Component(PlanWorkflowDefinitionPersist.PlanWorkflowDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanWorkflowDefinitionPersistValidator extends BaseValidator<PlanWorkflowDefinitionPersist> {

        private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanWorkflowDefinitionPersistValidator.class));

        public static final String ValidatorName = "PlanWorkflowPersistValidator.PlanWorkflowDefinitionPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected PlanWorkflowDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanWorkflowDefinitionPersist> modelClass() { return PlanWorkflowDefinitionPersist.class; }

        @Override
        protected List<Specification> specifications(PlanWorkflowDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getStartingStatusId()))
                            .failOn(PlanWorkflowDefinitionPersist._startingStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWorkflowDefinitionPersist._startingStatusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getStatusTransitions()))
                            .failOn(PlanWorkflowDefinitionPersist._startingStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWorkflowDefinitionPersist._startingStatusId}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getStatusTransitions()))
                            .on(PlanWorkflowDefinitionPersist._statusTransitions)
                            .over(item.getStatusTransitions())
                            .using((itm) -> this.validatorFactory.validator(PlanWorkflowDefinitionTransitionPersist.PlanWorkflowDefinitionTransitionPersistValidator.class))
            );
        }
    }
}
