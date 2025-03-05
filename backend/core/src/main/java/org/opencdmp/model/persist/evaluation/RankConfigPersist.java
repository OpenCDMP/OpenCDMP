package org.opencdmp.model.persist.evaluation;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.evaluatorbase.enums.RankType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class  RankConfigPersist {

    private RankType rankType;
    public static final String _rankType = "rankType";

    private ValueRangeConfigurationPersist valueRangeConfiguration;
    public static final String _valueRangeConfiguration = "valueRangeConfiguration";

    private SelectionConfigurationPersist selectionConfiguration;
    public static final String _selectionConfiguration = "selectionConfiguration";

    public RankType getRankType() {
        return rankType;
    }

    public void setRankType(RankType rank) {
        this.rankType = rank;
    }

    public ValueRangeConfigurationPersist getValueRangeConfiguration() {
        return valueRangeConfiguration;
    }

    public void setValueRangeConfiguration(ValueRangeConfigurationPersist valueRangeConfiguration) {
        this.valueRangeConfiguration = valueRangeConfiguration;
    }

    public SelectionConfigurationPersist getSelectionConfiguration() {
        return selectionConfiguration;
    }

    public void setSelectionConfiguration(SelectionConfigurationPersist selectionConfiguration) {
        this.selectionConfiguration = selectionConfiguration;
    }

    @Component(RankConfigPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RankConfigPersistValidator extends BaseValidator<RankConfigPersist> {

        public static final String ValidatorName = "RankConfigPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected RankConfigPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<RankConfigPersist> modelClass() {
            return RankConfigPersist.class;
        }

        @Override
        protected List<Specification> specifications(RankConfigPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getRankType()))
                            .failOn(RankConfigPersist._rankType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RankConfigPersist._rankType}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getValueRangeConfiguration()))
                            .on(RankConfigPersist._valueRangeConfiguration)
                            .over(item.getValueRangeConfiguration())
                            .using(() -> this.validatorFactory.validator(ValueRangeConfigurationPersist.ValueRangeConfigurationPersistValidator.class)),

                    this.refSpec()
                            .iff(() -> !this.isNull(item.getSelectionConfiguration()))
                            .on(RankConfigPersist._selectionConfiguration)
                            .over(item.getSelectionConfiguration())
                            .using(() -> this.validatorFactory.validator(SelectionConfigurationPersist.SelectionConfigurationPersistValidator.class))

            );
        }
    }

}
