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

public class UserInviteToTenantRequestPersist {

    private String email;
    public static final String _email = "email";


    private String tenantCode;
    public static final String _tenantCode = "tenantCode";

    private List<String> roles;
    public static final String _roles = "roles";


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Component(UserInviteToTenantRequestPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserInviteToTenantRequestPersistValidator extends BaseValidator<UserInviteToTenantRequestPersist> {

        public static final String ValidatorName = "UserInviteToTenantRequestPersistValidator";

        private final MessageSource messageSource;


        protected UserInviteToTenantRequestPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UserInviteToTenantRequestPersist> modelClass() {
            return UserInviteToTenantRequestPersist.class;
        }

        @Override
        protected List<Specification> specifications(UserInviteToTenantRequestPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEmail()))
                            .failOn(UserInviteToTenantRequestPersist._email).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserInviteToTenantRequestPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getEmail()))
                            .must(() -> this.isValidEmail(item.getEmail()))
                            .failOn(UserInviteToTenantRequestPersist._email).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{UserInviteToTenantRequestPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getRoles()))
                            .failOn(UserInviteToTenantRequestPersist._roles).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserInviteToTenantRequestPersist._roles}, LocaleContextHolder.getLocale()))
                    );
        }
    }

}

