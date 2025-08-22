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
import java.util.Map;

public class RankResultPersist {

    private double rank;
    public static final String _rank = "rank";

    private String details;
    public static final String _details = "details";

    private List<EvaluationResultPersist> results;
    public static final String _results = "results";

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<EvaluationResultPersist> getResults() {
        return results;
    }

    public void setResults(List<EvaluationResultPersist> results) {
        this.results = results;
    }

    @Component(RankResultPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RankResultPersistValidator extends BaseValidator<RankResultPersist> {

        public static final String ValidatorName = "EvaluationRankModelPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected RankResultPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<RankResultPersist> modelClass() {
            return RankResultPersist.class;
        }

        @Override
        protected List<Specification> specifications(RankResultPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getRank()))
                            .failOn(RankResultPersist._rank).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RankResultPersist._rank}, LocaleContextHolder.getLocale()))

            );
        }
    }

}
