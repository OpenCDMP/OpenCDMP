package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.PlanUserRole;
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

public class PlanUserPersist {

    private UUID user;

    public static final String _user = "user";

    private String email;
    public static final String _email = "email";

    private UUID sectionId;
    public static final String _sectionId = "sectionId";

    private PlanUserRole role;

    public static final String _role = "role";

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PlanUserRole getRole() {
        return role;
    }

    public void setRole(PlanUserRole role) {
        this.role = role;
    }

    @Component(PlanUserPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanUserPersistValidator extends BaseValidator<PlanUserPersist> {

        public static final String ValidatorName = "PlanUserPersistValidator";

        private final MessageSource messageSource;

        protected PlanUserPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanUserPersist> modelClass() {
            return PlanUserPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanUserPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isEmpty(item.getEmail()))
                            .must(() -> this.isValidGuid(item.getUser()))
                            .failOn(PlanUserPersist._user).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserPersist._user}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getUser()))
                            .must(() -> !this.isEmpty(item.getEmail()))
                            .failOn(PlanUserPersist._email).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getRole()))
                            .failOn(PlanUserPersist._role).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserPersist._role}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getSectionId()))
                            .must(() -> this.isValidGuid(item.getSectionId()))
                            .failOn(PlanUserPersist._sectionId).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserPersist._sectionId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getEmail()))
                            .must(() -> this.isValidEmail(item.getEmail()))
                            .failOn(PlanUserPersist._email).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{PlanUserPersist._email}, LocaleContextHolder.getLocale()))
                    );
        }
    }

}
