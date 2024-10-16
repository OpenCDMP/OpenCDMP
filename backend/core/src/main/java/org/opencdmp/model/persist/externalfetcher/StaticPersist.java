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

public class StaticPersist {

    private List<StaticOptionPersist> options;
    public final static String _options = "options";

    public List<StaticOptionPersist> getOptions() {
        return options;
    }

    public void setOptions(List<StaticOptionPersist> options) {
        this.options = options;
    }

    @Component(StaticPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class StaticPersistValidator extends BaseValidator<StaticPersist> {

        public static final String ValidatorName = "StaticPersistValidatorValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected StaticPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<StaticPersist> modelClass() {
            return StaticPersist.class;
        }

        @Override
        protected List<Specification> specifications(StaticPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getOptions()))
                            .failOn(StaticPersist._options).failWith(messageSource.getMessage("Validation_Required", new Object[]{StaticPersist._options}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getOptions()))
                            .on(StaticPersist._options)
                            .over(item.getOptions())
                            .using((itm) -> this.validatorFactory.validator(StaticOptionPersist.StaticOptionPersistValidator.class))
            );
        }
    }

}
