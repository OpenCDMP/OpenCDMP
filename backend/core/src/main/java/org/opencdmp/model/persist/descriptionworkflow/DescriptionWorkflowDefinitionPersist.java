package org.opencdmp.model.persist.descriptionworkflow;

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

public class DescriptionWorkflowDefinitionPersist {
    private UUID startingStatusId;
    public final static String _startingStatusId = "startingStatusId";

    private List<DescriptionWorkflowDefinitionTransitionPersist> statusTransitions;
    public final static String _statusTransitions = "statusTransitions";


    public UUID getStartingStatusId() { return startingStatusId; }

    public void setStartingStatusId(UUID startingStatusId) { this.startingStatusId = startingStatusId; }

    public List<DescriptionWorkflowDefinitionTransitionPersist> getStatusTransitions() { return statusTransitions; }

    public void setStatusTransitions(List<DescriptionWorkflowDefinitionTransitionPersist> statusTransitions) { this.statusTransitions = statusTransitions; }


    @Component(DescriptionWorkflowDefinitionPersist.DescriptionWorkflowDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionWorkflowDefinitionPersistValidator extends BaseValidator<DescriptionWorkflowDefinitionPersist> {

        public final static String ValidatorName = "DescriptionWorkflowPersistValidator.DescriptionWorkflowDefinitionPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;
        protected DescriptionWorkflowDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionWorkflowDefinitionPersist> modelClass() { return DescriptionWorkflowDefinitionPersist.class; }

        @Override
        protected List<Specification> specifications(DescriptionWorkflowDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getStartingStatusId()))
                            .failOn(DescriptionWorkflowDefinitionPersist._startingStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowDefinitionPersist._statusTransitions}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getStatusTransitions()))
                            .failOn(DescriptionWorkflowDefinitionPersist._statusTransitions)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowDefinitionPersist._statusTransitions}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff((() -> !this.isListNullOrEmpty(item.getStatusTransitions())))
                            .on(DescriptionWorkflowDefinitionPersist._statusTransitions)
                            .over(item.getStatusTransitions())
                            .using((itm) -> this.validatorFactory.validator(DescriptionWorkflowDefinitionTransitionPersist.DescriptionWorkflowDefinitionTransitionPersistValidator.class))
            );
        }
    }
}
