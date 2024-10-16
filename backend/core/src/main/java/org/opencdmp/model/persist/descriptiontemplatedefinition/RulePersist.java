package org.opencdmp.model.persist.descriptiontemplatedefinition;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.ReferencePersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class RulePersist {

    private String target;

    public static final String _target = "target";

    private String textValue;
    public static final String _textValue = "textValue";

    private Instant dateValue;
    public static final String _dateValue = "dateValue";

    private Boolean booleanValue;
    public static final String _booleanValue = "booleanValue";

    private List<ReferencePersist> references;
    public static final String _references = "references";

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTextValue() {
        return this.textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Instant getDateValue() {
        return this.dateValue;
    }

    public void setDateValue(Instant dateValue) {
        this.dateValue = dateValue;
    }

    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public List<ReferencePersist> getReferences() {
        return this.references;
    }

    public void setReferences(List<ReferencePersist> references) {
        this.references = references;
    }

    @Component(RulePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RulePersistValidator extends BaseValidator<RulePersist> {

        public static final String ValidatorName = "DescriptionTemplate.RulePersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;
        private org.opencdmp.model.persist.descriptiontemplatedefinition.FieldPersist fieldEntity;
        private String fieldSetId;

        protected RulePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
	        this.validatorFactory = validatorFactory;
        }

        public RulePersistValidator withFieldPersist(org.opencdmp.model.persist.descriptiontemplatedefinition.FieldPersist fieldEntity) {
            this.fieldEntity = fieldEntity;
            return this;
        }

        public RulePersistValidator withFieldSetId(String fieldSetId) {
            this.fieldSetId = fieldSetId;
            return this;
        }

        @Override
        protected Class<RulePersist> modelClass() {
            return RulePersist.class;
        }

        @Override
        protected List<Specification> specifications(RulePersist item) {
            FieldType fieldType = this.fieldEntity != null && this.fieldEntity.getData() != null ? this.fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getTarget()))
                            .failOn(RulePersist._target).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RulePersist._target}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getTarget()))
                            .must(() -> !item.getTarget().equals(this.fieldEntity.getId()) && !item.getTarget().equals(this.fieldSetId))
                            .failOn(RulePersist._target).failWith(this.messageSource.getMessage("Validation_UnexpectedValue", new Object[]{RulePersist._target}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.isNull(fieldType))
                            .must(() -> !FieldType.TAGS.equals(fieldType) && !FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType) && !FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType) &&
                                    !FieldType.UPLOAD.equals(fieldType) && !FieldType.REFERENCE_TYPES.equals(fieldType) && !FieldType.DATASET_IDENTIFIER.equals(fieldType)
                                    && !FieldType.VALIDATION.equals(fieldType))
                            .failOn(RulePersist._target).failWith(this.messageSource.getMessage("Validation_UnexpectedValue", new Object[]{RulePersist._target}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isTextType(fieldType))
                            .must(() -> !this.isEmpty(item.getTextValue()))
                            .failOn(RulePersist._textValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RulePersist._textValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isTextListType(fieldType))
                            .must(() -> !this.isEmpty(item.getTextValue()))
                            .failOn(RulePersist._textValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RulePersist._textValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isDateType(fieldType))
                            .must(() -> !this.isNull(item.getDateValue()))
                            .failOn(RulePersist._dateValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RulePersist._dateValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isBooleanType(fieldType))
                            .must(() -> !this.isNull(item.getBooleanValue()))
                            .failOn(RulePersist._booleanValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RulePersist._booleanValue}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> FieldType.isReferenceType(fieldType) &&  !this.isListNullOrEmpty(item.getReferences()))
                            .on(RulePersist._references)
                            .over(item.getReferences())
                            .using((itm) -> this.validatorFactory.validator(ReferencePersist.ReferencePersistValidator.class))
            );
        }
    }

}
