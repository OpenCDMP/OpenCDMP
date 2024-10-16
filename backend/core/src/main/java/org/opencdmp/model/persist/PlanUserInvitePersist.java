package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
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

public class PlanUserInvitePersist {

    private List<PlanUserPersist> users;

    public static final String _users = "users";

    public List<PlanUserPersist> getUsers() {
        return users;
    }

    public void setUsers(List<PlanUserPersist> users) {
        this.users = users;
    }

    @Component(PlanUserInvitePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanUserInvitePersistValidator extends BaseValidator<PlanUserInvitePersist> {

        public static final String ValidatorName = "PlanUserInvitePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PlanUserInvitePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanUserInvitePersist> modelClass() {
            return PlanUserInvitePersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanUserInvitePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getUsers()))
                            .failOn(PlanUserInvitePersist._users).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserInvitePersist._users}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getUsers()))
                            .on(PlanUserInvitePersist._users)
                            .over(item.getUsers())
                            .using((itm) -> this.validatorFactory.validator(PlanUserPersist.PlanUserPersistValidator.class))
            );
        }
    }

}
