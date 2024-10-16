package org.opencdmp.model.persist.actionconfirmation;

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

public class PlanInvitationPersist {

    private String email;

    public static final String _email = "email";

    private UUID planId;

    public static final String _planId = "planId";

    private UUID sectionId;

    public static final String _sectionId = "sectionId";

    private PlanUserRole role;

    public static final String _role = "role";

    public PlanInvitationPersist(String email, UUID planId, UUID sectionId, PlanUserRole role) {
        this.email = email;
        this.planId = planId;
        this.sectionId = sectionId;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public PlanUserRole getRole() {
        return role;
    }

    public void setRole(PlanUserRole role) {
        this.role = role;
    }

    @Component(PlanInvitationPersistValidator.ValidatorName)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanInvitationPersistValidator extends BaseValidator<PlanInvitationPersist> {

        public static final String ValidatorName = "PlanInvitationPersistValidator";

        private final MessageSource messageSource;

        protected PlanInvitationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanInvitationPersist> modelClass() {
            return PlanInvitationPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanInvitationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEmail()))
                            .failOn(PlanInvitationPersist._email).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanInvitationPersist._email}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getPlanId()))
                            .failOn(PlanInvitationPersist._planId).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanInvitationPersist._planId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getRole()))
                            .failOn(PlanInvitationPersist._role).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanInvitationPersist._role}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
