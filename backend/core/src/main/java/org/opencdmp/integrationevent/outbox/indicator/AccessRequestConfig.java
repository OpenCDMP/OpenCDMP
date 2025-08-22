package org.opencdmp.integrationevent.outbox.indicator;


import gr.cite.tools.validation.ValidatorFactory;
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

public class AccessRequestConfig {

    private List<FilterColumnConfig> filterColumns;
    public static final String _filterColumns = "filterColumns";

    public List<FilterColumnConfig> getFilterColumns() {
        return filterColumns;
    }

    public void setFilterColumns(List<FilterColumnConfig> filterColumns) {
        this.filterColumns = filterColumns;
    }

    @Component(AccessRequestConfigValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class AccessRequestConfigValidator extends BaseValidator<AccessRequestConfig> {

        public static final String ValidatorName = "Indicator.AccessRequestConfigValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;


        protected AccessRequestConfigValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<AccessRequestConfig> modelClass() {
            return AccessRequestConfig.class;
        }

        @Override
        protected List<Specification> specifications(AccessRequestConfig item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getFilterColumns()))
                            .failOn(AccessRequestConfig._filterColumns).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{AccessRequestConfig._filterColumns}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFilterColumns()))
                            .on(AccessRequestConfig._filterColumns)
                            .over(item.getFilterColumns())
                            .using((itm) -> this.validatorFactory.validator(FilterColumnConfig.FilterColumnConfigValidator.class))
            );
        }
    }
}
