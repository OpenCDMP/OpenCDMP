package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
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

public class UserTenantUsersInviteRequest {

    private List<UserInviteToTenantRequestPersist> users;
    public static final String _users = "users";

    public List<UserInviteToTenantRequestPersist> getUsers() {
        return users;
    }

    public void setUsers(List<UserInviteToTenantRequestPersist> users) {
        this.users = users;
    }

    @Component(UserTenantUsersInviteRequestValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserTenantUsersInviteRequestValidator extends BaseValidator<UserTenantUsersInviteRequest> {

        public static final String ValidatorName = "UserTenantUsersInviteRequestValidator";

        private final ValidatorFactory validatorFactory;
        private final MessageSource messageSource;


        protected UserTenantUsersInviteRequestValidator(ConventionService conventionService, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory, MessageSource messageSource) {
            super(conventionService, errors);
            this.validatorFactory = validatorFactory;
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UserTenantUsersInviteRequest> modelClass() {
            return UserTenantUsersInviteRequest.class;
        }

        @Override
        protected List<Specification> specifications(UserTenantUsersInviteRequest item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getUsers()))
                            .failOn(UserTenantUsersInviteRequest._users).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserTenantUsersInviteRequest._users}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getUsers()))
                            .on(UserTenantUsersInviteRequest._users)
                            .over(item.getUsers())
                            .using((itm) -> this.validatorFactory.validator(UserInviteToTenantRequestPersist.UserInviteToTenantRequestPersistValidator.class))
                    );
        }
    }

}

