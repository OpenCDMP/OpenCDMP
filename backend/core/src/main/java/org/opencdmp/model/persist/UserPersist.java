package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserPersist {

    private UUID id;

    private String name;

    public static final String _name = "name";

    private String hash;

    public static final String _hash = "hash";

    private UserAdditionalInfoPersist additionalInfo;

    public static final String _additionalInfo = "additionalInfo";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public UserAdditionalInfoPersist getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(UserAdditionalInfoPersist additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Component(UserPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserPersistValidator extends BaseValidator<UserPersist> {

        public static final String ValidatorName = "UserPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected UserPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<UserPersist> modelClass() {
            return UserPersist.class;
        }

        @Override
        protected List<Specification> specifications(UserPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(UserPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(UserPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getName()))
                            .failOn(UserPersist._name).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserPersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getName()))
                            .must(() -> this.lessEqualLength(item.getName(), UserEntity._nameLength))
                            .failOn(UserPersist._name).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{UserPersist._name}, LocaleContextHolder.getLocale())),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getAdditionalInfo()))
                            .on(UserPersist._additionalInfo)
                            .over(item.getAdditionalInfo())
                            .using(() -> this.validatorFactory.validator(UserAdditionalInfoPersist.UserAdditionalInfoPersistValidator.class))
            );
        }
    }

}

