package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
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

public class UserDescriptionTemplatePersist {

    private UUID userId = null;

    public static final String _userId = "userId";

    private UserDescriptionTemplateRole role;

    public static final String _role = "role";

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UserDescriptionTemplateRole getRole() {
        return role;
    }

    public void setRole(UserDescriptionTemplateRole role) {
        this.role = role;
    }

    @Component(UserDescriptionTemplatePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserDescriptionTemplatePersistValidator extends BaseValidator<UserDescriptionTemplatePersist> {

        public static final String ValidatorName = "UserDescriptionTemplatePersistValidator";

        private final MessageSource messageSource;

        protected UserDescriptionTemplatePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<UserDescriptionTemplatePersist> modelClass() {
            return UserDescriptionTemplatePersist.class;
        }

        @Override
        protected List<Specification> specifications(UserDescriptionTemplatePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getUserId()))
                            .failOn(UserDescriptionTemplatePersist._userId).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserDescriptionTemplatePersist._userId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getRole()))
                            .failOn(UserDescriptionTemplatePersist._role).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserDescriptionTemplatePersist._role}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
