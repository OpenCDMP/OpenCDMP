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
import java.util.UUID;

public class DescriptionDeletePersist {

    private UUID id;
    public static final String _id = "id";

    private PlanUpdateRequestApply planUpdateRequestApply;
    public static final String _planUpdateRequestApply = "planUpdateRequestApply";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlanUpdateRequestApply getPlanUpdateRequestApply() {
        return planUpdateRequestApply;
    }

    public void setPlanUpdateRequestApply(PlanUpdateRequestApply planUpdateRequestApply) {
        this.planUpdateRequestApply = planUpdateRequestApply;
    }

    @Component(DescriptionDeletePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionDeletePersistValidator extends BaseValidator<DescriptionDeletePersist> {

        public static final String ValidatorName = "DescriptionDeletePersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected DescriptionDeletePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionDeletePersist> modelClass() {
            return DescriptionDeletePersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionDeletePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getId()))
                            .failOn(DescriptionDeletePersist._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionDeletePersist._id}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> item.getPlanUpdateRequestApply() != null)
                            .on(DescriptionDeletePersist._planUpdateRequestApply)
                            .over(item.getPlanUpdateRequestApply())
                            .using(() -> this.validatorFactory.validator(PlanUpdateRequestApply.PlanUpdateRequestApplyValidator.class))
            );
        }
    }
}
