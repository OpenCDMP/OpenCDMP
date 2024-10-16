package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
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

public class LockPersist {

    private UUID id;

    private UUID target;

    public static final String _target = "target";

    private LockTargetType targetType;

    public static final String _targetType = "targetType";

    private String hash;

    public static final String _hash = "hash";

    public LockPersist() {
    }

    public LockPersist(UUID target, LockTargetType targetType) {
        this.target = target;
        this.targetType = targetType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTarget() {
        return target;
    }

    public void setTarget(UUID target) {
        this.target = target;
    }

    public LockTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(LockTargetType targetType) {
        this.targetType = targetType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(LockPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class LockPersistValidator extends BaseValidator<LockPersist> {

        public static final String ValidatorName = "LockPersistValidator";

        private final MessageSource messageSource;

        protected LockPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<LockPersist> modelClass() {
            return LockPersist.class;
        }

        @Override
        protected List<Specification> specifications(LockPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(LockPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{LockPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(LockPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getTarget()))
                            .failOn(LockPersist._target).failWith(messageSource.getMessage("Validation_Required", new Object[]{LockPersist._target}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getTargetType()))
                            .failOn(LockPersist._targetType).failWith(messageSource.getMessage("Validation_Required", new Object[]{LockPersist._targetType}, LocaleContextHolder.getLocale()))
            );
        }
    }

}

