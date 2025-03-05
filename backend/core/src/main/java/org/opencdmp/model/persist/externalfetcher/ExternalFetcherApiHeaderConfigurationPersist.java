package org.opencdmp.model.persist.externalfetcher;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.ExternalFetcherApiHeaderType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.service.externalfetcher.config.entities.ExternalFetcherApiHeaderConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;

public class ExternalFetcherApiHeaderConfigurationPersist implements ExternalFetcherApiHeaderConfiguration {

    private ExternalFetcherApiHeaderType key;
    public static final String _key = "key";

    private String value;
    public static final String _value = "value";

    @Override
    public ExternalFetcherApiHeaderType getKey() {
        return key;
    }

    public void setKey(ExternalFetcherApiHeaderType key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Component(ExternalFetcherApiHeaderConfigurationPersist.ExternalFetcherApiHeaderConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ExternalFetcherApiHeaderConfigurationPersistValidator extends BaseValidator<ExternalFetcherApiHeaderConfigurationPersist>{

        public static final String ValidatorName = "ExternalFetcherApiHeaderConfigurationPersistValidator";

        private final MessageSource messageSource;

        protected ExternalFetcherApiHeaderConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;

        }

        @Override
        protected Class<ExternalFetcherApiHeaderConfigurationPersist> modelClass() {
            return ExternalFetcherApiHeaderConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(ExternalFetcherApiHeaderConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getValue()))
                            .failOn(ExternalFetcherApiHeaderConfigurationPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiHeaderConfigurationPersist._value}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getKey()))
                            .failOn(ExternalFetcherApiHeaderConfigurationPersist._key).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherApiHeaderConfigurationPersist._key}, LocaleContextHolder.getLocale()))
            );
        }


    }
}
