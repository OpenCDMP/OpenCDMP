package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.PlanStatus;
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

public class PlanDescriptionTemplatePersist {

    private UUID descriptionTemplateGroupId;

    public static final String _descriptionTemplateGroupId = "descriptionTemplateGroupId";

    private UUID sectionId;

    public static final String _sectionId = "sectionId";

    public UUID getDescriptionTemplateGroupId() {
        return this.descriptionTemplateGroupId;
    }

    public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) {
        this.descriptionTemplateGroupId = descriptionTemplateGroupId;
    }

    public UUID getSectionId() {
        return this.sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    @Component(PlanDescriptionTemplatePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanDescriptionTemplatePersistValidator extends BaseValidator<PlanDescriptionTemplatePersist> {

        public static final String ValidatorName = "PlanDescriptionTemplatePersistValidator";

        private final MessageSource messageSource;

        private PlanStatus status;

        protected PlanDescriptionTemplatePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanDescriptionTemplatePersist> modelClass() {
            return PlanDescriptionTemplatePersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanDescriptionTemplatePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getDescriptionTemplateGroupId()))
                            .failOn(PlanDescriptionTemplatePersist._descriptionTemplateGroupId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanDescriptionTemplatePersist._descriptionTemplateGroupId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getSectionId()))
                            .failOn(PlanDescriptionTemplatePersist._sectionId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanDescriptionTemplatePersist._sectionId}, LocaleContextHolder.getLocale()))
            );
        }

    }

}
