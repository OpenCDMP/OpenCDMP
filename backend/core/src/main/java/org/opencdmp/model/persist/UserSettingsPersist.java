package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.UserSettingsType;
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

public class UserSettingsPersist {

    private UUID id;

    private String key;

    public static final String _key = "key";

    private String value;

    public static final String _value = "value";

    private UUID entityId;

    public static final String _entityId = "entityId";

    private UserSettingsType type;

    public static final String _type = "type";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public UserSettingsType getType() {
        return type;
    }

    public void setType(UserSettingsType type) {
        this.type = type;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Component(UserSettingsPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserSettingsPersistValidator extends BaseValidator<UserSettingsPersist> {

        public static final String ValidatorName = "UserSettingsPersistValidator";

        private final MessageSource messageSource;

        protected UserSettingsPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UserSettingsPersist> modelClass() {
            return UserSettingsPersist.class;
        }

        @Override
        protected List<Specification> specifications(UserSettingsPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(UserSettingsPersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserSettingsPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(UserSettingsPersist._hash).failWith(messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getKey()))
                            .failOn(UserSettingsPersist._key).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserSettingsPersist._key}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getValue()))
                            .failOn(UserSettingsPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserSettingsPersist._value}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getEntityId()))
                            .failOn(UserSettingsPersist._entityId).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserSettingsPersist._entityId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getType()))
                            .failOn(UserSettingsPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserSettingsPersist._type}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
