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

public class RankModelPersist {

    private double rank;
    public static final String _rank = "rank";

    private Map<String, String> messages;
    public static final String _messages = "messages";

    private String details;
    public static final String _details = "details";

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Component(RankModelPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RankModelPersistValidator extends BaseValidator<RankModelPersist> {

        public static final String ValidatorName = "EvaluationRankModelPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected RankModelPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<RankModelPersist> modelClass() {
            return RankModelPersist.class;
        }

        @Override
        protected List<Specification> specifications(RankModelPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getRank()))
                            .failOn(RankModelPersist._rank).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RankModelPersist._rank}, LocaleContextHolder.getLocale()))

            );
        }
    }

}
