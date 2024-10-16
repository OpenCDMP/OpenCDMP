package org.opencdmp.model.persist.descriptionworkflow;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.IsActive;
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

public class DescriptionWorkflowPersist {
    private UUID id;

    private String name;
    public final static String _name = "name";

    private String description;
    public final static String _description = "description";

    private String hash;
    public final static String _hash = "hash";

    private DescriptionWorkflowDefinitionPersist definition;
    public final static String _definition = "definition";


    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getHash() { return hash; }

    public void setHash(String hash) { this.hash = hash; }

    public DescriptionWorkflowDefinitionPersist getDefinition() { return definition; }

    public void setDefinition(DescriptionWorkflowDefinitionPersist definition) { this.definition = definition; }

    @Component(DescriptionWorkflowPersist.DescriptionWorkflowPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionWorkflowPersistValidator extends BaseValidator<DescriptionWorkflowPersist> {

        public final static String ValidatorName = "DescriptionWorkflowPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected DescriptionWorkflowPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionWorkflowPersist> modelClass() { return DescriptionWorkflowPersist.class; }

        @Override
        protected List<Specification> specifications(DescriptionWorkflowPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(DescriptionWorkflowPersist._hash)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(DescriptionWorkflowPersist._hash)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getName()))
                            .failOn(DescriptionWorkflowPersist._name)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionWorkflowPersist._name}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(DescriptionWorkflowPersist._description)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DescriptionWorkflowDefinitionPersist.DescriptionWorkflowDefinitionPersistValidator.class))

            );
        }
    }
}
