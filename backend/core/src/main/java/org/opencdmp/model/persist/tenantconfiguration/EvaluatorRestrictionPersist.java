package org.opencdmp.model.persist.tenantconfiguration;

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

public class EvaluatorRestrictionPersist {

    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";

    private List<String> benchmarkIds;
    public static final String _benchmarkIds = "benchmarkIds";

    private List<String> testIds;
    public static final String _testIds = "testIds";

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public List<String> getBenchmarkIds() {
        return benchmarkIds;
    }

    public void setBenchmarkIds(List<String> benchmarkIds) {
        this.benchmarkIds = benchmarkIds;
    }

    public List<String> getTestIds() {
        return testIds;
    }

    public void setTestIds(List<String> testIds) {
        this.testIds = testIds;
    }

    @Component(EvaluatorRestrictionPersist.EvaluatorRestrictionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluatorRestrictionPersistValidator extends BaseValidator<EvaluatorRestrictionPersist> {

        public static final String ValidatorName = "EvaluatorRestrictionPersistValidator";

        private final MessageSource messageSource;

        protected EvaluatorRestrictionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<EvaluatorRestrictionPersist> modelClass() {
            return EvaluatorRestrictionPersist.class;
        }

        @Override
        protected List<Specification> specifications(EvaluatorRestrictionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEvaluatorId()))
                            .failOn(EvaluatorRestrictionPersist._evaluatorId).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorRestrictionPersist._evaluatorId}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
