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

public class ContactSupportPersist {
    private String subject;
    public static final String _subject = "subject";
    private String description;

    public static final String _description = "description";

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Component(ContactSupportPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ContactSupportPersistValidator extends BaseValidator<ContactSupportPersist> {

        public static final String ValidatorName = "ContactSupportPersistValidator";

        private final MessageSource messageSource;

        protected ContactSupportPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<ContactSupportPersist> modelClass() {
            return ContactSupportPersist.class;
        }

        @Override
        protected List<Specification> specifications(ContactSupportPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSubject()))
                            .failOn(ContactSupportPersist._subject).failWith(messageSource.getMessage("Validation_Required", new Object[]{ContactSupportPersist._subject}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getDescription()))
                            .failOn(ContactSupportPersist._description).failWith(messageSource.getMessage("Validation_Required", new Object[]{ContactSupportPersist._description}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
