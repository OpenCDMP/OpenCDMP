package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NewVersionPlanPersist {

    private UUID id;
    public static final String _id = "id";

    private String label;

    public static final String _label = "label";

    private String description;

    public static final String _description = "description";

    private UUID blueprintId;

    public static final String _blueprintId = "blueprintId";

    private List<NewVersionPlanDescriptionPersist> descriptions;

    public static final String _descriptions = "descriptions";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getBlueprintId() {
        return this.blueprintId;
    }

    public void setBlueprintId(UUID blueprintId) {
        this.blueprintId = blueprintId;
    }

    public List<NewVersionPlanDescriptionPersist> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<NewVersionPlanDescriptionPersist> descriptions) {
        this.descriptions = descriptions;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(NewVersionPlanPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class NewVersionPlanPersistValidator extends BaseValidator<NewVersionPlanPersist> {

        public static final String ValidatorName = "NewVersionPlanPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected NewVersionPlanPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<NewVersionPlanPersist> modelClass() {
            return NewVersionPlanPersist.class;
        }

        @Override
        protected List<Specification> specifications(NewVersionPlanPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(NewVersionPlanPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(NewVersionPlanPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), PlanEntity._labelLength))
                            .failOn(NewVersionPlanPersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{NewVersionPlanPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getBlueprintId()))
                            .failOn(NewVersionPlanPersist._blueprintId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{NewVersionPlanPersist._blueprintId}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptions()))
                            .on(NewVersionPlanPersist._descriptions)
                            .over(item.getDescriptions())
                            .using((itm) -> this.validatorFactory.validator(NewVersionPlanDescriptionPersist.NewVersionPlanDescriptionPersistValidator.class))
            );
        }
    }

}
