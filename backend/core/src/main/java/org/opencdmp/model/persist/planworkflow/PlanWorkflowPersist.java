package org.opencdmp.model.persist.planworkflow;

import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.IsActive;
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

public class PlanWorkflowPersist {

    private UUID id;

    public static final String _name = "name";
    private String name;

    public static final String _description = "description";
    private String description;

    public static final String _hash = "hash";
    private String hash;

    public static final String _definition = "definition";
    private PlanWorkflowDefinitionPersist definition;


    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getHash() { return hash; }

    public void setHash(String hash) { this.hash = hash; }

    public PlanWorkflowDefinitionPersist getDefinition() { return definition; }

    public void setDefinition(PlanWorkflowDefinitionPersist definition) { this.definition = definition; }

    @Component(PlanWorkflowPersist.PlanWorkflowPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanWorkflowPersistValidator extends BaseValidator<PlanWorkflowPersist> {

        public static final String ValidatorName = "PlanWorkflowPersistValidator";

        private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanWorkflowPersistValidator.class));

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected PlanWorkflowPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanWorkflowPersist> modelClass() { return PlanWorkflowPersist.class; }

        @Override
        protected List<Specification> specifications(PlanWorkflowPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(PlanWorkflowPersist._hash)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWorkflowPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(PlanWorkflowPersist._hash)
                            .failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{PlanWorkflowPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getName()) && !this.isEmpty(item.getName()))
                            .failOn(PlanWorkflowPersist._name)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWorkflowPersist._name}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(PlanWorkflowPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(PlanWorkflowDefinitionPersist.PlanWorkflowDefinitionPersistValidator.class))
            );
        }
    }
}
