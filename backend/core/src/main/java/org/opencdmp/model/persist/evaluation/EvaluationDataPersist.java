package org.opencdmp.model.persist.evaluation;

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

public class EvaluationDataPersist {

    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";

    private RankConfigPersist rankConfig;
    public static final String _rankConfig = "rankConfig";

    private RankResultPersist rankResult;
    public static final String _rankResult = "rankResult";

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public RankConfigPersist getRankConfig() {
        return rankConfig;
    }

    public void setRankConfig(RankConfigPersist rankConfig) {
        this.rankConfig = rankConfig;
    }

    public RankResultPersist getRankResult() {
        return rankResult;
    }

    public void setRankResult(RankResultPersist rankResult) {
        this.rankResult = rankResult;
    }

    @Component(EvaluationDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluationDataPersistValidator extends BaseValidator<EvaluationDataPersist> {

        public static final String ValidatorName = "EvaluationDataPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected EvaluationDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<EvaluationDataPersist> modelClass() {
            return EvaluationDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(EvaluationDataPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getEvaluatorId()))
                            .failOn(EvaluationDataPersist._evaluatorId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationDataPersist._evaluatorId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getRankConfig()))
                            .failOn(EvaluationDataPersist._rankConfig).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationDataPersist._rankConfig}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getRankConfig()))
                            .on(EvaluationDataPersist._rankConfig)
                            .over(item.getRankConfig())
                            .using(() -> this.validatorFactory.validator(RankConfigPersist.RankConfigPersistValidator.class)),
                    this.spec()
                            .must(() -> !this.isNull(item.getRankResult()))
                            .failOn(EvaluationDataPersist._rankResult).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{EvaluationDataPersist._rankResult}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getRankResult()))
                            .on(EvaluationDataPersist._rankResult)
                            .over(item.getRankResult())
                            .using(() -> this.validatorFactory.validator(RankResultPersist.RankResultPersistValidator.class))

                    );
        }
    }

}






