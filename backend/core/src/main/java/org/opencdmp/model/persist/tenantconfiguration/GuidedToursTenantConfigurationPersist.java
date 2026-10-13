package org.opencdmp.model.persist.tenantconfiguration;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class GuidedToursTenantConfigurationPersist {

    private Boolean enabled;

    public static final String _enabled = "enabled";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Component(GuidedToursTenantConfigurationPersist.GuidedToursTenantConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class GuidedToursTenantConfigurationPersistValidator extends BaseValidator<GuidedToursTenantConfigurationPersist> {

        public static final String ValidatorName = "GuidedToursTenantConfigurationPersistValidator";

        private final MessageSource messageSource;

        protected GuidedToursTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<GuidedToursTenantConfigurationPersist> modelClass() {
            return GuidedToursTenantConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(GuidedToursTenantConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> item.getEnabled() != null)
                            .failOn(GuidedToursTenantConfigurationPersist._enabled)
                            .failWith(messageSource.getMessage("Validation_Required", new Object[]{GuidedToursTenantConfigurationPersist._enabled}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
