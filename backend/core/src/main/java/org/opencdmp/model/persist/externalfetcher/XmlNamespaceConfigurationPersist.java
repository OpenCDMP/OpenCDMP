package org.opencdmp.model.persist.externalfetcher;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.service.externalfetcher.config.entities.XmlNamespaceConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class XmlNamespaceConfigurationPersist implements XmlNamespaceConfiguration {

    private String prefix;
    public static final String _prefix = "prefix";

    private String uri;
    public static final String _uri = "uri";

    @Override
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Component(XmlNamespaceConfigurationPersist.XmlNamespaceConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class XmlNamespaceConfigurationPersistValidator extends BaseValidator<XmlNamespaceConfigurationPersist> {

        public static final String ValidatorName = "XmlNamespaceConfigurationPersistValidator";

        private final MessageSource messageSource;

        protected XmlNamespaceConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<XmlNamespaceConfigurationPersist> modelClass() {
            return XmlNamespaceConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(XmlNamespaceConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getPrefix()))
                            .failOn(XmlNamespaceConfigurationPersist._prefix).failWith(messageSource.getMessage("Validation_Required", new Object[]{XmlNamespaceConfigurationPersist._prefix}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getUri()))
                            .failOn(XmlNamespaceConfigurationPersist._uri).failWith(messageSource.getMessage("Validation_Required", new Object[]{XmlNamespaceConfigurationPersist._uri}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
