package org.opencdmp.model.persist.planstatus;

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
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;

public class PlanStatusDefinitionPersist {
    public final static String _authorization = "authorization";
    private PlanStatusDefinitionAuthorizationPersist authorization = null;


    public PlanStatusDefinitionAuthorizationPersist getAuthorization() { return authorization; }

    public void setAuthorization(PlanStatusDefinitionAuthorizationPersist authorization) { this.authorization = authorization; }

    @Component(PlanStatusDefinitionPersist.PlanStatusDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanStatusDefinitionPersistValidator extends BaseValidator<PlanStatusDefinitionPersist> {
        public static final String ValidatorName = "PlanStatus.PlanStatusDefinitionPersistValidator";
        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        public PlanStatusDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanStatusDefinitionPersist> modelClass() {
            return PlanStatusDefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanStatusDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getAuthorization()))
                            .failOn(PlanStatusDefinitionPersist._authorization).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanStatusDefinitionPersist._authorization}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getAuthorization()))
                            .on(PlanStatusDefinitionPersist._authorization)
                            .over(item.getAuthorization())
                            .using(() -> this.validatorFactory.validator(PlanStatusDefinitionAuthorizationPersist.PlanStatusDefinitionAuthorizationPersistValidator.class))
            );
        }

        @Override
        public Errors validateObject(Object target) {
            return super.validateObject(target);
        }
    }
}
