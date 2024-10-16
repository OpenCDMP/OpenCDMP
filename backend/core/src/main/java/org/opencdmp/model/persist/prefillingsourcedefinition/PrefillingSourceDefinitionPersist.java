package org.opencdmp.model.persist.prefillingsourcedefinition;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherApiSourceConfigurationPersist;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class PrefillingSourceDefinitionPersist {

    private ExternalFetcherApiSourceConfigurationPersist searchConfiguration;
    public static final String _searchConfiguration = "searchConfiguration";

    private ExternalFetcherApiSourceConfigurationPersist getConfiguration;
    public static final String _getConfiguration = "getConfiguration";

    private Boolean getEnabled;
    public static final String _getEnabled = "getEnabled";

    private List<PrefillingSourceDefinitionFieldPersist> fields;
    public static final String _fields = "fields";

    private List<PrefillingSourceDefinitionFixedValueFieldPersist> fixedValueFields;
    public static final String _fixedValueFields = "fixedValueFields";

    public ExternalFetcherApiSourceConfigurationPersist getSearchConfiguration() {
        return searchConfiguration;
    }

    public void setSearchConfiguration(ExternalFetcherApiSourceConfigurationPersist searchConfiguration) {
        this.searchConfiguration = searchConfiguration;
    }

    public ExternalFetcherApiSourceConfigurationPersist getGetConfiguration() {
        return getConfiguration;
    }

    public void setGetConfiguration(ExternalFetcherApiSourceConfigurationPersist getConfiguration) {
        this.getConfiguration = getConfiguration;
    }

    public Boolean getGetEnabled() {
        return getEnabled;
    }

    public void setGetEnabled(Boolean getEnabled) {
        this.getEnabled = getEnabled;
    }

    public List<PrefillingSourceDefinitionFieldPersist> getFields() {
        return fields;
    }

    public void setFields(List<PrefillingSourceDefinitionFieldPersist> fields) {
        this.fields = fields;
    }

    public List<PrefillingSourceDefinitionFixedValueFieldPersist> getFixedValueFields() {
        return fixedValueFields;
    }

    public void setFixedValueFields(List<PrefillingSourceDefinitionFixedValueFieldPersist> fixedValueFields) {
        this.fixedValueFields = fixedValueFields;
    }

    @Component(PrefillingSourceDefinitionPersist.PrefillingSourceDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PrefillingSourceDefinitionPersistValidator extends BaseValidator<PrefillingSourceDefinitionPersist> {

        public static final String ValidatorName = "PrefillingSourceDefinitionPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PrefillingSourceDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PrefillingSourceDefinitionPersist> modelClass() {
            return PrefillingSourceDefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(PrefillingSourceDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getSearchConfiguration()))
                            .failOn(PrefillingSourceDefinitionPersist._searchConfiguration).failWith(messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourceDefinitionPersist._searchConfiguration}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getSearchConfiguration()))
                            .on(PrefillingSourceDefinitionPersist._searchConfiguration)
                            .over(item.getSearchConfiguration())
                            .using(() -> this.validatorFactory.validator(ExternalFetcherApiSourceConfigurationPersist.ExternalFetcherApiSourceConfigurationPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getGetConfiguration()) && item.getGetEnabled())
                            .on(PrefillingSourceDefinitionPersist._getConfiguration)
                            .over(item.getGetConfiguration())
                            .using(() -> this.validatorFactory.validator(ExternalFetcherApiSourceConfigurationPersist.ExternalFetcherApiSourceConfigurationPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFields()))
                            .on(PrefillingSourceDefinitionPersist._fields)
                            .over(item.getFields())
                            .using((itm) -> this.validatorFactory.validator(PrefillingSourceDefinitionFieldPersist.PrefillingSourceDefinitionFieldPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFixedValueFields()))
                            .on(PrefillingSourceDefinitionPersist._fixedValueFields)
                            .over(item.getFixedValueFields())
                            .using((itm) -> this.validatorFactory.validator(PrefillingSourceDefinitionFixedValueFieldPersist.PrefillingSourceDefinitionFixedValueFieldPersistValidator.class))
            );
        }
    }
}
