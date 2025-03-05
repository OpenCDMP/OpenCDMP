package org.opencdmp.model.persist.evaluation;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.evaluatorbase.enums.SuccessStatus;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class SelectionConfigurationPersist {

    private List<ValueSetPersist> valueSetList;
    public static final String _valueSetList = "valueSetList";

    public List<ValueSetPersist> getValueSetList() {
        return valueSetList;
    }

    public void setValueSetList(List<ValueSetPersist> valueSetList) {
        this.valueSetList = valueSetList;
    }


    @Component(SelectionConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SelectionConfigurationPersistValidator extends BaseValidator<SelectionConfigurationPersist> {

        public static final String ValidatorName = "SelectionConfigurationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected SelectionConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<SelectionConfigurationPersist> modelClass() {
            return SelectionConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(SelectionConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getValueSetList()))
                            .failOn(SelectionConfigurationPersist._valueSetList).failWith(messageSource.getMessage("Validation_Required", new Object[]{SelectionConfigurationPersist._valueSetList}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getValueSetList()))
                            .on(SelectionConfigurationPersist._valueSetList)
                            .over(item.getValueSetList())
                            .using((itm) -> this.validatorFactory.validator(ValueSetPersist.ValueSetPersistValidator.class))
                    );
        }


    }

    public static class ValueSetPersist {
        private double key;
        public static final String _key = "key";

        private SuccessStatus successStatus;
        public static final String _successStatus = "successStatus";

        public double getKey() {
            return key;
        }

        public void setKey(double key) {
            this.key = key;
        }

        public SuccessStatus getSuccessStatus() {
            return successStatus;
        }

        public void setSuccessStatus(SuccessStatus successStatus) {
            this.successStatus = successStatus;
        }

        @Component(ValueSetPersistValidator.ValidatorName)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public static class ValueSetPersistValidator extends BaseValidator<ValueSetPersist> {

            public static final String ValidatorName = "SelectionConfigurationPersistValidator.ValueSetPersistValidator";

            private final MessageSource messageSource;

            protected ValueSetPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
                super(conventionService, errors);
                this.messageSource = messageSource;
            }

            @Override
            protected Class<ValueSetPersist> modelClass() {
                return ValueSetPersist.class;
            }

            @Override
            protected List<Specification> specifications(ValueSetPersist item) {
                return Arrays.asList(
                        this.spec()
                                .must(() -> !this.isNull(item.getKey()))
                                .failOn(ValueSetPersist._key).failWith(messageSource.getMessage("Validation_Required", new Object[]{ValueSetPersist._key}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .must(() -> !this.isNull(item.getSuccessStatus()))
                                .failOn(ValueSetPersist._successStatus).failWith(messageSource.getMessage("Validation_Required", new Object[]{ValueSetPersist._successStatus}, LocaleContextHolder.getLocale()))
                );
            }
        }
    }

}
