package org.opencdmp.integrationevent.outbox.indicator;


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

public class FilterColumnConfig {

    private String code;
    public static final String _code = "code";

    private String dependsOnCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDependsOnCode() {
        return dependsOnCode;
    }

    public void setDependsOnCode(String dependsOnCode) {
        this.dependsOnCode = dependsOnCode;
    }

    @Component(FilterColumnConfigValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class FilterColumnConfigValidator extends BaseValidator<FilterColumnConfig> {

        public static final String ValidatorName = "Indicator.FilterColumnConfigPersistValidator";

        private final MessageSource messageSource;

        protected FilterColumnConfigValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<FilterColumnConfig> modelClass() {
            return FilterColumnConfig.class;
        }

        @Override
        protected List<Specification> specifications(FilterColumnConfig item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(FilterColumnConfig._code).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FilterColumnConfig._code}, LocaleContextHolder.getLocale()))
                    );
        }
    }
}
