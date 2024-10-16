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

public class StaticOptionPersist {

    private String code;

    public static final String _code = "code";

    private String value;

    public static final String _value = "value";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Component(StaticOptionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class StaticOptionPersistValidator extends BaseValidator<StaticOptionPersist> {

        public static final String ValidatorName = "StaticOptionPersistValidator";

        private final MessageSource messageSource;

        protected StaticOptionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<StaticOptionPersist> modelClass() {
            return StaticOptionPersist.class;
        }

        @Override
        protected List<Specification> specifications(StaticOptionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(StaticOptionPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{StaticOptionPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getValue()))
                            .failOn(StaticOptionPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{StaticOptionPersist._value}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
