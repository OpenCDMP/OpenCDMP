package org.opencdmp.model.persist;


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

/**
 * Wrapper persist DTO that carries a plan plus the descriptions edited alongside it, so the editor can
 * save both atomically in a single transaction (see PlanController.PersistWithDescriptions). The plan
 * is validated with the existing {@link PlanPersist.PlanPersistValidator} and each description with the
 * existing {@link DescriptionPersist.DescriptionPersistValidator}, so per-entity validation, hash
 * (optimistic-concurrency) and lock checks behave exactly as on the standalone persist endpoints.
 */
public class PlanWithDescriptionsPersist {

    private PlanPersist plan;
    public static final String _plan = "plan";

    private List<DescriptionPersist> descriptions;
    public static final String _descriptions = "descriptions";

    public PlanPersist getPlan() {
        return plan;
    }

    public void setPlan(PlanPersist plan) {
        this.plan = plan;
    }

    public List<DescriptionPersist> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<DescriptionPersist> descriptions) {
        this.descriptions = descriptions;
    }

    @Component(PlanWithDescriptionsPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanWithDescriptionsPersistValidator extends BaseValidator<PlanWithDescriptionsPersist> {

        public static final String ValidatorName = "PlanWithDescriptionsPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PlanWithDescriptionsPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanWithDescriptionsPersist> modelClass() {
            return PlanWithDescriptionsPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanWithDescriptionsPersist item) {

            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getPlan()))
                            .failOn(PlanWithDescriptionsPersist._plan).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanWithDescriptionsPersist._plan}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getPlan()))
                            .on(PlanWithDescriptionsPersist._plan)
                            .over(item.getPlan())
                            .using(() -> this.validatorFactory.validator(PlanPersist.PlanPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptions()))
                            .on(PlanWithDescriptionsPersist._descriptions)
                            .over(item.getDescriptions())
                            .using((itm) -> this.validatorFactory.validator(DescriptionPersist.DescriptionPersistValidator.class))
            );
        }

    }

}
