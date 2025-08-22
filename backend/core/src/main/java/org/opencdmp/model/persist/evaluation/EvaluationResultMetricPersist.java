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

public class EvaluationResultMetricPersist {

    private double rank;
    public static final String _rank = "rank";

    private String metricTitle;
    public static final String _metricTitle = "metricTitle";

    private String metricDetails;
    public static final String _metricDetails = "metricDetails";

    private List<EvaluationResultMessagePersist> messages;
    public static final String _messages = "messages";

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getMetricTitle() {
        return metricTitle;
    }

    public void setMetricTitle(String metricTitle) {
        this.metricTitle = metricTitle;
    }

    public String getMetricDetails() {
        return metricDetails;
    }

    public void setMetricDetails(String metricDetails) {
        this.metricDetails = metricDetails;
    }

    public List<EvaluationResultMessagePersist> getMessages() {
        return messages;
    }

    public void setMessages(List<EvaluationResultMessagePersist> messages) {
        this.messages = messages;
    }

    @Component(EvaluationResultMetricPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluationResultMetricPersistValidator extends BaseValidator<EvaluationResultMetricPersist> {

        public static final String ValidatorName = "EvaluationResultMetricPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected EvaluationResultMetricPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<EvaluationResultMetricPersist> modelClass() {
            return EvaluationResultMetricPersist.class;
        }

        @Override
        protected List<Specification> specifications(EvaluationResultMetricPersist item) {
            return Arrays.asList();
        }


    }


}
