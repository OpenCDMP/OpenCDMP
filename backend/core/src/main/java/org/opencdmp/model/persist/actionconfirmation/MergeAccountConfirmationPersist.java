package org.opencdmp.model.persist.actionconfirmation;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

public class MergeAccountConfirmationPersist {

    private String email;

    public static final String _email = "email";


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Component(MergeAccountConfirmationPersistValidator.ValidatorName)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class MergeAccountConfirmationPersistValidator extends BaseValidator<MergeAccountConfirmationPersist> {

        public static final String ValidatorName = "MergeAccountConfirmationPersistValidator";

        private final MessageSource messageSource;

        protected MergeAccountConfirmationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<MergeAccountConfirmationPersist> modelClass() {
            return MergeAccountConfirmationPersist.class;
        }

        @Override
        protected List<Specification> specifications(MergeAccountConfirmationPersist item) {
            return Collections.singletonList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEmail()))
                            .failOn(MergeAccountConfirmationPersist._email).failWith(messageSource.getMessage("Validation_Required", new Object[]{MergeAccountConfirmationPersist._email}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
