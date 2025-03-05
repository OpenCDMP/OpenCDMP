package org.opencdmp.model.persist.externalfetcher;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.service.externalfetcher.config.entities.ResultsConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ResultsConfigurationPersist implements ResultsConfiguration<ResultFieldsMappingConfigurationPersist> {

    private String resultsArrayPath;

    public static final String _resultsArrayPath = "resultsArrayPath";

    private List<ResultFieldsMappingConfigurationPersist> fieldsMapping;

    public static final String _fieldsMapping = "fieldsMapping";

    public String getResultsArrayPath() {
        return resultsArrayPath;
    }

    public void setResultsArrayPath(String resultsArrayPath) {
        this.resultsArrayPath = resultsArrayPath;
    }

    public List<ResultFieldsMappingConfigurationPersist> getFieldsMapping() {
        return fieldsMapping;
    }

    public void setFieldsMapping(List<ResultFieldsMappingConfigurationPersist> fieldsMapping) {
        this.fieldsMapping = fieldsMapping;
    }

    @Component(ResultsConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ResultsConfigurationPersistValidator extends BaseValidator<ResultsConfigurationPersist> {

        public static final String ValidatorName = "ResultsConfigurationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected ResultsConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ResultsConfigurationPersist> modelClass() {
            return ResultsConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(ResultsConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getResultsArrayPath()))
                            .failOn(ResultsConfigurationPersist._resultsArrayPath).failWith(messageSource.getMessage("Validation_Required", new Object[]{ResultsConfigurationPersist._resultsArrayPath}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getFieldsMapping()))
                            .failOn(ResultsConfigurationPersist._fieldsMapping).failWith(messageSource.getMessage("Validation_Required", new Object[]{ResultsConfigurationPersist._fieldsMapping}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFieldsMapping()))
                            .on(ResultsConfigurationPersist._fieldsMapping)
                            .over(item.getFieldsMapping())
                            .using((itm) -> this.validatorFactory.validator(ResultFieldsMappingConfigurationPersist.ResultFieldsMappingConfigurationPersistValidator.class))
            );
        }
    }

}
