package org.opencdmp.model.persist;

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

public class UserRolePatchPersist {

    private UUID id;

    private List<String> roles;

    public static final String _roles = "roles";

    private Boolean hasTenantAdminMode;
    public static final String _hasTenantAdminMode = "hasTenantAdminMode";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Boolean getHasTenantAdminMode() {
        return hasTenantAdminMode;
    }

    public void setHasTenantAdminMode(Boolean hasTenantAdminMode) {
        this.hasTenantAdminMode = hasTenantAdminMode;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(UserRolePatchPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserRolePatchPersistValidator extends BaseValidator<UserRolePatchPersist> {

        public static final String ValidatorName = "UserRolePatchPersistValidator";

        private final MessageSource messageSource;

        protected UserRolePatchPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UserRolePatchPersist> modelClass() {
            return UserRolePatchPersist.class;
        }

        @Override
        protected List<Specification> specifications(UserRolePatchPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(UserRolePatchPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UserRolePatchPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(UserRolePatchPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getHasTenantAdminMode()))
                            .failOn(UserRolePatchPersist._hasTenantAdminMode).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getRoles()))
                            .failOn(UserRolePatchPersist._roles).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UserRolePatchPersist._roles}, LocaleContextHolder.getLocale()))
            );
        }
    }

}

