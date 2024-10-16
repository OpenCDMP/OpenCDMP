package org.opencdmp.model.persist.descriptionworkflow;

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

public class DescriptionWorkflowDefinitionTransitionPersist {

    private UUID fromStatusId;
    public final static String _fromStatusId = "fromStatusId";

    private UUID toStatusId;
    public final static String _toStatusId = "toStatusId";


    public UUID getFromStatusId() { return fromStatusId; }

    public void setFromStatusId(UUID fromStatusId) { this.fromStatusId = fromStatusId; }

    public UUID getToStatusId() { return toStatusId; }

    public void setToStatusId(UUID toStatusId) { this.toStatusId = toStatusId; }

    @Component(DescriptionWorkflowDefinitionTransitionPersist.DescriptionWorkflowDefinitionTransitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionWorkflowDefinitionTransitionPersistValidator extends BaseValidator<DescriptionWorkflowDefinitionTransitionPersist> {

        public static final String ValidatorName = "DescriptionWorkflowPersistValidator.DescriptionWorkflowDefinitionPersistValidator.DescriptionWorkflowDefinitionTransitionPersistValidator";
        private final MessageSource messageSource;
        protected DescriptionWorkflowDefinitionTransitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionWorkflowDefinitionTransitionPersist> modelClass() {
            return DescriptionWorkflowDefinitionTransitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionWorkflowDefinitionTransitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getFromStatusId()))
                            .failOn(DescriptionWorkflowDefinitionTransitionPersist._fromStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowDefinitionTransitionPersist._fromStatusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getToStatusId()))
                            .failOn(DescriptionWorkflowDefinitionTransitionPersist._toStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowDefinitionTransitionPersist._toStatusId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getFromStatusId()) && this.isValidGuid(item.getToStatusId()))
                            .must(() -> !item.getFromStatusId().equals(item.getToStatusId()))
                            .failOn(DescriptionWorkflowDefinitionTransitionPersist._toStatusId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowDefinitionTransitionPersist._toStatusId}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
