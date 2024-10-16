package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UsageLimitEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.usagelimit.DefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UsageLimitPersist {

    private UUID id;
    public static final String _id = "id";

    private String label;
    public static final String _label = "label";

    private UsageLimitTargetMetric targetMetric;;
    public static final String _targetMetric = "targetMetric";

    private Long value;
    public static final String _value = "value";

    private DefinitionPersist definition;
    public static final String _definition = "definition";

    private String hash;
    public static final String _hash = "hash";

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

    public UsageLimitTargetMetric getTargetMetric() {
        return targetMetric;
    }

    public void setTargetMetric(UsageLimitTargetMetric targetMetric) {
        this.targetMetric = targetMetric;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public DefinitionPersist getDefinition() {
        return definition;
    }

    public void setDefinition(DefinitionPersist definition) {
        this.definition = definition;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(UsageLimitPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UsageLimitPersistValidator extends BaseValidator<UsageLimitPersist> {

        public static final String ValidatorName = "UsageLimitPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected UsageLimitPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<UsageLimitPersist> modelClass() {
            return UsageLimitPersist.class;
        }

        @Override
        protected List<Specification> specifications(UsageLimitPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(UsageLimitPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UsageLimitPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(UsageLimitPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(UsageLimitPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UsageLimitPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), UsageLimitEntity._labelLength))
                            .failOn(UsageLimitPersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{UsageLimitPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getTargetMetric()))
                            .failOn(UsageLimitPersist._targetMetric).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UsageLimitPersist._targetMetric}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getValue()))
                            .failOn(UsageLimitPersist._value).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UsageLimitPersist._value}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> item.getValue() > 0)
                            .failOn(UsageLimitPersist._value).failWith(this.messageSource.getMessage("Validation_UnexpectedValue", new Object[]{UsageLimitPersist._value}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(UsageLimitPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(org.opencdmp.model.persist.usagelimit.DefinitionPersist.DefinitionPersistValidator.class))
                    );
        }
    }

}
