package org.opencdmp.model.persist.planproperties;

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

public class PlanContactPersist {

    private String firstName;
    public static final String _firstName = "firstName";

    private String lastName;
    public static final String _lastName = "lastName";

    private String email;
    public static final String _email = "email";

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Component(PlanContactPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanContactPersistValidator extends BaseValidator<PlanContactPersist> {

        public static final String ValidatorName = "PlanContactPersistValidator";
        private final MessageSource messageSource;

        protected PlanContactPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanContactPersist> modelClass() {
            return PlanContactPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanContactPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEmail()))
                            .failOn(PlanContactPersist._email).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanContactPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getEmail()))
                            .must(() -> this.isValidEmail(item.getEmail()))
                            .failOn(PlanContactPersist._email).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PlanContactPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getFirstName()))
                            .failOn(PlanContactPersist._firstName).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanContactPersist._firstName}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLastName()))
                            .failOn(PlanContactPersist._lastName).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanContactPersist._lastName}, LocaleContextHolder.getLocale()))
                    );
        }
    }

}
