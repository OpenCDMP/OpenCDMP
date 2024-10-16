package org.opencdmp.model.persist;

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

public class NewVersionPlanDescriptionPersist {

    private UUID descriptionId;
    public static final String _descriptionId = "descriptionId";

    private UUID blueprintSectionId;
    public static final String _blueprintSectionId = "blueprintSectionId";


    public UUID getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(UUID descriptionId) {
        this.descriptionId = descriptionId;
    }

    public UUID getBlueprintSectionId() {
        return blueprintSectionId;
    }

    public void setBlueprintSectionId(UUID blueprintSectionId) {
        this.blueprintSectionId = blueprintSectionId;
    }

    @Component(NewVersionPlanDescriptionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class NewVersionPlanDescriptionPersistValidator extends BaseValidator<NewVersionPlanDescriptionPersist> {

        public static final String ValidatorName = "NewVersionPlanDescriptionPersistValidator";

        private final MessageSource messageSource;

        protected NewVersionPlanDescriptionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<NewVersionPlanDescriptionPersist> modelClass() {
            return NewVersionPlanDescriptionPersist.class;
        }

        @Override
        protected List<Specification> specifications(NewVersionPlanDescriptionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getDescriptionId()))
                            .failOn(NewVersionPlanDescriptionPersist._descriptionId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanDescriptionPersist._descriptionId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getBlueprintSectionId()))
                            .failOn(NewVersionPlanDescriptionPersist._blueprintSectionId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanDescriptionPersist._blueprintSectionId}, LocaleContextHolder.getLocale()))
                    );
        }
    }

}
