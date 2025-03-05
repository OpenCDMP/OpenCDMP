package org.opencdmp.model.persist.evaluation;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.evaluatorbase.enums.NumberType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ValueRangeConfigurationPersist {

    private NumberType numberType;
    public static final String _numberType = "numberType";

    private double min;
    public static final String _min = "min";

    private double max;
    public static final String _max = "max";

    private double minPassValue;
    public static final String _minPassValue = "minPassValue";

    public NumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(NumberType numberType) {
        this.numberType = numberType;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMinPassValue() {
        return minPassValue;
    }

    public void setMinPassValue(double minPassValue) {
        this.minPassValue = minPassValue;
    }

    @Component(ValueRangeConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ValueRangeConfigurationPersistValidator extends BaseValidator<ValueRangeConfigurationPersist> {

        public static final String ValidatorName = "ValueRangeConfigurationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected ValueRangeConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ValueRangeConfigurationPersist> modelClass() {
            return ValueRangeConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(ValueRangeConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getMin()))
                            .failOn(ValueRangeConfigurationPersist._min).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ValueRangeConfigurationPersist._min}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getMax()))
                            .failOn(ValueRangeConfigurationPersist._max).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ValueRangeConfigurationPersist._max}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getMinPassValue()))
                            .failOn(ValueRangeConfigurationPersist._minPassValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ValueRangeConfigurationPersist._minPassValue}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
