package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.planblueprintdefinition.DefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NewVersionPlanBlueprintPersist {

    private UUID id;

    private String label;

    public static final String _label = "label";

    private DefinitionPersist definition;

    public static final String _definition = "definition";

    private PlanBlueprintStatus status;

    public static final String _status = "status";

    private String hash;

    public static final String _hash = "hash";

    private String description;

    public static final String _description = "description";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DefinitionPersist getDefinition() {
        return this.definition;
    }

    public void setDefinition(DefinitionPersist definition) {
        this.definition = definition;
    }

    public PlanBlueprintStatus getStatus() {
        return this.status;
    }

    public void setStatus(PlanBlueprintStatus status) {
        this.status = status;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Component(NewVersionPlanBlueprintPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class NewVersionPlanBlueprintPersistValidator extends BaseValidator<NewVersionPlanBlueprintPersist> {

        public static final String ValidatorName = "NewVersionPlanBlueprintPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        public NewVersionPlanBlueprintPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<NewVersionPlanBlueprintPersist> modelClass() {
            return NewVersionPlanBlueprintPersist.class;
        }

        @Override
        protected List<Specification> specifications(NewVersionPlanBlueprintPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(NewVersionPlanBlueprintPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanBlueprintPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(NewVersionPlanBlueprintPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanBlueprintPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), PlanBlueprintEntity._labelLength))
                            .failOn(NewVersionPlanBlueprintPersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{NewVersionPlanBlueprintPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStatus()))
                            .failOn(NewVersionPlanBlueprintPersist._status).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanBlueprintPersist._status}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(NewVersionPlanBlueprintPersist._definition).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanBlueprintPersist._definition}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(NewVersionPlanBlueprintPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DefinitionPersist.DefinitionPersistValidator.class))
            );
        }
    }

}
