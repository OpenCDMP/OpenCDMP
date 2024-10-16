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

import java.util.Arrays;
import java.util.List;

public class PlanStatusDefinitionAuthorizationPersist {

    public final static String _edit = "edit";
    private PlanStatusDefinitionAuthorizationItemPersist edit = null;


    public PlanStatusDefinitionAuthorizationItemPersist getEdit() {
        return this.edit;
    }

    public void setEdit(PlanStatusDefinitionAuthorizationItemPersist edit) {
        this.edit = edit;
    }

    @Component(PlanStatusDefinitionAuthorizationPersist.PlanStatusDefinitionAuthorizationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanStatusDefinitionAuthorizationPersistValidator extends BaseValidator<PlanStatusDefinitionAuthorizationPersist> {
        public static final String ValidatorName = "PlanStatus.PlanStatusDefinitionAuthorizationPersistValidator";
        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        public PlanStatusDefinitionAuthorizationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanStatusDefinitionAuthorizationPersist> modelClass() {
            return PlanStatusDefinitionAuthorizationPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanStatusDefinitionAuthorizationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getEdit()))
                            .failOn(PlanStatusDefinitionAuthorizationPersist._edit).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanStatusDefinitionAuthorizationPersist._edit}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getEdit()))
                            .on(PlanStatusDefinitionAuthorizationPersist._edit)
                            .over(item.getEdit())
                            .using(() -> this.validatorFactory.validator(PlanStatusDefinitionAuthorizationItemPersist.PlanStatusDefinitionAuthorizationItemPersistValidator.class))
            );
        }
    }
}