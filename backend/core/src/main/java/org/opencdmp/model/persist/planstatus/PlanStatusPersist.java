package org.opencdmp.model.persist.planstatus;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlanStatusPersist {
    private UUID id;

    private String name;

    public static final String _name = "name";

    private String description;
    public static final String _description = "description";

    private String action;
    public static final String _action = "action";

    private Integer ordinal;
    public static final String _ordinal = "ordinal";

    private org.opencdmp.commons.enums.PlanStatus internalStatus;
    public static final String _internalStatus = "internalStatus";

    private PlanStatusDefinitionPersist definition;
    public static final String _definition = "definition";
    private String hash;
    public static final String _hash = "hash";


    public UUID getId() {
        return this.id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public org.opencdmp.commons.enums.PlanStatus getInternalStatus() { return this.internalStatus; }
    public void setInternalStatus(org.opencdmp.commons.enums.PlanStatus internalStatus) { this.internalStatus = internalStatus; }

    public PlanStatusDefinitionPersist getDefinition() { return this.definition; }
    public void setDefinition(PlanStatusDefinitionPersist definition) { this.definition = definition; }

    public String getHash() {
        return this.hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(org.opencdmp.model.persist.planstatus.PlanStatusPersist.PlanStatusPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanStatusPersistValidator extends BaseValidator<org.opencdmp.model.persist.planstatus.PlanStatusPersist> {

        public static final String ValidatorName = "PlanStatusPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PlanStatusPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<org.opencdmp.model.persist.planstatus.PlanStatusPersist> modelClass() {
            return org.opencdmp.model.persist.planstatus.PlanStatusPersist.class;
        }

        @Override
        protected List<Specification> specifications(org.opencdmp.model.persist.planstatus.PlanStatusPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(org.opencdmp.model.persist.planstatus.PlanStatusPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{org.opencdmp.model.persist.planstatus.PlanStatusPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(org.opencdmp.model.persist.planstatus.PlanStatusPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{org.opencdmp.model.persist.planstatus.PlanStatusPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getName()))
                            .failOn(org.opencdmp.model.persist.planstatus.PlanStatusPersist._name).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{org.opencdmp.model.persist.planstatus.PlanStatusPersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getName()))
                            .must(() -> this.lessEqualLength(item.getName(), PlanStatusEntity._nameLength))
                            .failOn(org.opencdmp.model.persist.planstatus.PlanStatusPersist._name).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{org.opencdmp.model.persist.planstatus.PlanStatusPersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getAction()))
                            .must(() -> this.lessEqualLength(item.getAction(), PlanStatusEntity._actionLength))
                            .failOn(PlanStatusPersist._action).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{org.opencdmp.model.persist.planstatus.PlanStatusPersist._action}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(PlanStatusPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{org.opencdmp.model.persist.planstatus.PlanStatusPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getInternalStatus() == org.opencdmp.commons.enums.PlanStatus.Finalized)
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(org.opencdmp.model.persist.planstatus.PlanStatusPersist._definition).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{org.opencdmp.model.persist.planstatus.PlanStatusPersist._definition}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(org.opencdmp.model.persist.planstatus.PlanStatusPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(PlanStatusDefinitionPersist.PlanStatusDefinitionPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> item.getInternalStatus() == org.opencdmp.commons.enums.PlanStatus.Finalized)
                            .on(org.opencdmp.model.persist.planstatus.PlanStatusPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(PlanStatusDefinitionPersist.PlanStatusDefinitionPersistValidator.class))
            );
        }
    }
}
