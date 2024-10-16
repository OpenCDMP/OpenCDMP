package org.opencdmp.model.persist;

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

public class UserMergeRequestPersist {


    private String email;
    public static final String _email = "email";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Component(UserMergeRequestPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserMergeRequestPersistValidator extends BaseValidator<UserMergeRequestPersist> {

        public static final String ValidatorName = "UserMergeRequestPersistValidator";

        private final MessageSource messageSource;


        protected UserMergeRequestPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UserMergeRequestPersist> modelClass() {
            return UserMergeRequestPersist.class;
        }

        @Override
        protected List<Specification> specifications(UserMergeRequestPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEmail()))
                            .failOn(UserMergeRequestPersist._email).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserMergeRequestPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getEmail()))
                            .must(() -> this.isValidEmail(item.getEmail()))
                            .failOn(UserMergeRequestPersist._email).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{UserMergeRequestPersist._email}, LocaleContextHolder.getLocale()))
            );
        }
    }

}

