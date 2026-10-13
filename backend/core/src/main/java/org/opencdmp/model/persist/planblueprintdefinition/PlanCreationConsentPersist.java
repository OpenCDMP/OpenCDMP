package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


public class PlanCreationConsentPersist {

    private Boolean enabled;
    public static final String _enabled = "enabled";

    private String message;
    public static final String _message = "message";

    private String details;
    public static final String _details = "details";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Component(PlanCreationConsentPersist.PlanCreationConsentPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanCreationConsentPersistValidator extends BaseValidator<PlanCreationConsentPersist> {

        public static final String ValidatorName = "PlanCreationConsentPersist";

        private final MessageSource messageSource;

        protected PlanCreationConsentPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanCreationConsentPersist> modelClass() {
            return PlanCreationConsentPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanCreationConsentPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> !this.isNull(item.getEnabled()) && item.getEnabled().equals(true))
                            .must(() -> !this.isEmpty(item.getMessage()))
                            .failOn(PlanCreationConsentPersist._message).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanCreationConsentPersist._message}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
