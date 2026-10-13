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

public class TermsAndConditionsTenantConfigurationPersist {

    private String content;
    public static final String _content = "content";

    private Short version;
    public static final String _version = "version";

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    @Component(TermsAndConditionsTenantConfigurationPersist.TermsAndConditionsTenantConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class TermsAndConditionsTenantConfigurationPersistValidator extends BaseValidator<TermsAndConditionsTenantConfigurationPersist> {

        public static final String ValidatorName = "TermsAndConditionsTenantConfigurationPersistValidator";

        private final MessageSource messageSource;

        protected TermsAndConditionsTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<TermsAndConditionsTenantConfigurationPersist> modelClass() {
            return TermsAndConditionsTenantConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(TermsAndConditionsTenantConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getContent()))
                            .failOn(TermsAndConditionsTenantConfigurationPersist._content)
                            .failWith(messageSource.getMessage("Validation_Required", new Object[]{TermsAndConditionsTenantConfigurationPersist._content}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> item.getVersion() != null && item.getVersion() > 0)
                            .failOn(TermsAndConditionsTenantConfigurationPersist._version)
                            .failWith(messageSource.getMessage("Validation_Required", new Object[]{TermsAndConditionsTenantConfigurationPersist._version}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
