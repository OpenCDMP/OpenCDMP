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

public class EvaluationResultMessagePersist {

    private String title;
    public static final String _title = "title";

    private String message;
    public static final String _message = "message";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Component(EvaluationResultMessagePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluationResultMessagePersistValidator extends BaseValidator<EvaluationResultMessagePersist> {

        public static final String ValidatorName = "EvaluationResultMessagePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected EvaluationResultMessagePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<EvaluationResultMessagePersist> modelClass() {
            return EvaluationResultMessagePersist.class;
        }

        @Override
        protected List<Specification> specifications(EvaluationResultMessagePersist item) {
            return Arrays.asList();
        }


    }


}
