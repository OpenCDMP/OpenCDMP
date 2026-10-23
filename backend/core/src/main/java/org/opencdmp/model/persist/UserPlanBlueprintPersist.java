package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.UserPlanBlueprintRole;
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

public class UserPlanBlueprintPersist {

    private UUID userId = null;

    public static final String _userId = "userId";

    private UserPlanBlueprintRole role;

    public static final String _role = "role";

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UserPlanBlueprintRole getRole() {
        return role;
    }

    public void setRole(UserPlanBlueprintRole role) {
        this.role = role;
    }

    @Component(UserPlanBlueprintPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserPlanBlueprintPersistValidator extends BaseValidator<UserPlanBlueprintPersist> {

        public static final String ValidatorName = "UserPlanBlueprintPersistValidator";

        private final MessageSource messageSource;

        protected UserPlanBlueprintPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UserPlanBlueprintPersist> modelClass() {
            return UserPlanBlueprintPersist.class;
        }

        @Override
        protected List<Specification> specifications(UserPlanBlueprintPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getUserId()))
                            .failOn(UserPlanBlueprintPersist._userId).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserPlanBlueprintPersist._userId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getRole()))
                            .failOn(UserPlanBlueprintPersist._role).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserPlanBlueprintPersist._role}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
