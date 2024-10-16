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

public class PublicContactSupportPersist {
    private String fullName;
    public static final String _fullName = "fullName";
    private String email;
    public static final String _email = "email";
    private String affiliation;
    public static final String _affiliation = "affiliation";
    private String message;
    public static final String _message = "message";

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Component(PublicContactSupportPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PublicContactSupportPersistValidator extends BaseValidator<PublicContactSupportPersist> {

        public static final String ValidatorName = "PublicContactSupportPersistValidator";

        private final MessageSource messageSource;

        protected PublicContactSupportPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PublicContactSupportPersist> modelClass() {
            return PublicContactSupportPersist.class;
        }

        @Override
        protected List<Specification> specifications(PublicContactSupportPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getFullName()))
                            .failOn(PublicContactSupportPersist._fullName).failWith(messageSource.getMessage("Validation_Required", new Object[]{PublicContactSupportPersist._fullName}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEmail()))
                            .failOn(PublicContactSupportPersist._email).failWith(messageSource.getMessage("Validation_Required", new Object[]{PublicContactSupportPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getAffiliation()))
                            .failOn(PublicContactSupportPersist._affiliation).failWith(messageSource.getMessage("Validation_Required", new Object[]{PublicContactSupportPersist._affiliation}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getMessage()))
                            .failOn(PublicContactSupportPersist._message).failWith(messageSource.getMessage("Validation_Required", new Object[]{PublicContactSupportPersist._message}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
