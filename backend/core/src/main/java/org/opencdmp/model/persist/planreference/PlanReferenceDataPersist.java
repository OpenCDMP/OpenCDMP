package org.opencdmp.model.persist.planreference;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
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

public class PlanReferenceDataPersist {

    private UUID blueprintFieldId;

    public static final String _blueprintFieldId = "blueprintFieldId";

    public UUID getBlueprintFieldId() {
        return blueprintFieldId;
    }

    public void setBlueprintFieldId(UUID blueprintFieldId) {
        this.blueprintFieldId = blueprintFieldId;
    }

    @Component(PlanReferenceDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanReferenceDataPersistValidator extends BaseValidator<PlanReferenceDataPersist> {

        public static final String ValidatorName = "PlanReferenceDataPersistValidator";
        private final MessageSource messageSource;

        protected PlanReferenceDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
	        this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanReferenceDataPersist> modelClass() {
            return PlanReferenceDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanReferenceDataPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getBlueprintFieldId()))
                            .failOn(PlanReferenceDataPersist._blueprintFieldId).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanReferenceDataPersist._blueprintFieldId}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
