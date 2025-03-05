package org.opencdmp.model.persist.descriptionstatus;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DescriptionStatusPersist {

    private UUID id;

    private String name;
    public final static String _name = "name";

    private String description;
    public final static String _description = "description";

    private String action;
    public static final String _action = "action";

    private Integer ordinal;
    public static final String _ordinal = "ordinal";

    private org.opencdmp.commons.enums.DescriptionStatus internalStatus;
    public final static String _internalStatus = "internalStatus";

    private DescriptionStatusDefinitionPersist definition;
    public final static String _definition = "definition";

    private String hash;
    public final static String _hash = "hash";


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public org.opencdmp.commons.enums.DescriptionStatus getInternalStatus() { return internalStatus; }
    public void setInternalStatus(org.opencdmp.commons.enums.DescriptionStatus internalStatus) { this.internalStatus = internalStatus; }

    public DescriptionStatusDefinitionPersist getDefinition() { return definition; }
    public void setDefinition(DescriptionStatusDefinitionPersist definition) { this.definition = definition; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    @Component(DescriptionStatusPersist.DescriptionStatusPersistValidation.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionStatusPersistValidation extends BaseValidator<DescriptionStatusPersist> {

        public final static String ValidatorName = "DescriptionStatusPersistValidation";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected DescriptionStatusPersistValidation(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionStatusPersist> modelClass() {
            return DescriptionStatusPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionStatusPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(DescriptionStatusPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(DescriptionStatusPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getName()))
                            .failOn(DescriptionStatusPersist._name).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getName()))
                            .must(() -> this.lessEqualLength(item.getName(), DescriptionStatusEntity._nameLength))
                            .failOn(DescriptionStatusPersist._name).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getAction()))
                            .must(() -> this.lessEqualLength(item.getAction(), DescriptionStatusEntity._actionLength))
                            .failOn(DescriptionStatusPersist._action).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._action}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(DescriptionStatusPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getInternalStatus() == org.opencdmp.commons.enums.DescriptionStatus.Finalized)
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(DescriptionStatusPersist._definition).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusPersist._definition}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(DescriptionStatusPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DescriptionStatusDefinitionPersist.DescriptionStatusDefinitionPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> item.getInternalStatus() == org.opencdmp.commons.enums.DescriptionStatus.Finalized)
                            .on(DescriptionStatusPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DescriptionStatusDefinitionPersist.DescriptionStatusDefinitionPersistValidator.class))
            );
        }
    }
}
