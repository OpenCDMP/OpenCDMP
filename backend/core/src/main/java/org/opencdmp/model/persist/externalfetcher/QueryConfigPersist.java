package org.opencdmp.model.persist.externalfetcher;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class QueryConfigPersist {

    private String name;
    public static final String _name = "name";
    private String defaultValue;
    public static final String _defaultValue = "defaultValue";
    List<QueryCaseConfigPersist> cases;
    public static final String _cases = "cases";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<QueryCaseConfigPersist> getCases() {
        return cases;
    }

    public void setCases(List<QueryCaseConfigPersist> cases) {
        this.cases = cases;
    }

    @Component(QueryConfigPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class QueryConfigPersistValidator extends BaseValidator<QueryConfigPersist> {

        public static final String ValidatorName = "QueryConfigPersistPersistValidator";
        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected QueryConfigPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
	        this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<QueryConfigPersist> modelClass() {
            return QueryConfigPersist.class;
        }

        @Override
        protected List<Specification> specifications(QueryConfigPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getName()))
                            .failOn(QueryConfigPersist._name).failWith(messageSource.getMessage("Validation_Required", new Object[]{QueryConfigPersist._name}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getCases()))
                            .on(QueryConfigPersist._cases)
                            .over(item.getCases())
                            .using((itm) -> this.validatorFactory.validator(QueryCaseConfigPersist.QueryCaseConfigPersistValidator.class))
            );
        }
    }

}
