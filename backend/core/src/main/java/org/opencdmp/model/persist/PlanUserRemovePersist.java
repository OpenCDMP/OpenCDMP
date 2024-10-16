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

public class PlanUserRemovePersist {

    private UUID id;

    public static final String _id = "id";

    private UUID planId;

    public static final String _planId = "planId";

    private PlanUserRole role;

    public static final String _role = "role";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public PlanUserRole getRole() {
        return role;
    }

    public void setRole(PlanUserRole role) {
        this.role = role;
    }

    @Component(PlanUserRemovePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanUserRemovePersistValidator extends BaseValidator<PlanUserRemovePersist> {

        public static final String ValidatorName = "PlanUserRemovePersistValidator";

        private final MessageSource messageSource;

        protected PlanUserRemovePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanUserRemovePersist> modelClass() {
            return PlanUserRemovePersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanUserRemovePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(PlanUserRemovePersist._id).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserRemovePersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanId()))
                            .failOn(PlanUserRemovePersist._planId).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserRemovePersist._planId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getRole()))
                            .failOn(PlanUserRemovePersist._role).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanUserRemovePersist._role}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
