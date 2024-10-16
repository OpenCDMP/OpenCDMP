package org.opencdmp.model.persist.externalfetcher;

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

public class ResultFieldsMappingConfigurationPersist {

    private String code;

    public static final String _code = "code";

    private String responsePath;

    public static final String _responsePath = "responsePath";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getResponsePath() {
        return responsePath;
    }

    public void setResponsePath(String responsePath) {
        this.responsePath = responsePath;
    }

    @Component(ResultFieldsMappingConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ResultFieldsMappingConfigurationPersistValidator extends BaseValidator<ResultFieldsMappingConfigurationPersist> {

        public static final String ValidatorName = "ResultFieldsMappingConfigurationPersistValidator";

        private final MessageSource messageSource;

        protected ResultFieldsMappingConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<ResultFieldsMappingConfigurationPersist> modelClass() {
            return ResultFieldsMappingConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(ResultFieldsMappingConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(ResultFieldsMappingConfigurationPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{ResultFieldsMappingConfigurationPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getResponsePath()))
                            .failOn(ResultFieldsMappingConfigurationPersist._responsePath).failWith(messageSource.getMessage("Validation_Required", new Object[]{ResultFieldsMappingConfigurationPersist._responsePath}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
