package org.opencdmp.model.persist.evaluation;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class EvaluationResultPersist {

    private double rank;
    public static final String _rank = "rank";

    private String benchmarkTitle;
    public static final String _benchmarkTitle = "benchmarkTitle";

    private String benchmarkDetails;
    public static final String _benchmarkDetails = "benchmarkDetails";

    private List<EvaluationResultMetricPersist> metrics;
    public static final String _metrics = "metrics";

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getBenchmarkTitle() {
        return benchmarkTitle;
    }

    public void setBenchmarkTitle(String benchmarkTitle) {
        this.benchmarkTitle = benchmarkTitle;
    }

    public String getBenchmarkDetails() {
        return benchmarkDetails;
    }

    public void setBenchmarkDetails(String benchmarkDetails) {
        this.benchmarkDetails = benchmarkDetails;
    }

    public List<EvaluationResultMetricPersist> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<EvaluationResultMetricPersist> metrics) {
        this.metrics = metrics;
    }

    @Component(EvaluationResultPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluationResultPersistValidator extends BaseValidator<EvaluationResultPersist> {

        public static final String ValidatorName = "EvaluationResultPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected EvaluationResultPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<EvaluationResultPersist> modelClass() {
            return EvaluationResultPersist.class;
        }

        @Override
        protected List<Specification> specifications(EvaluationResultPersist item) {
            return Arrays.asList();
        }


    }


}
