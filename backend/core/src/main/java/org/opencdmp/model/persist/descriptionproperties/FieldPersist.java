package org.opencdmp.model.persist.descriptionproperties;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.ReferencePersist;
import org.opencdmp.service.visibility.VisibilityService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class FieldPersist {

    private String textValue;
    public static final String _textValue = "textValue";

    private List<String> textListValue;
    public static final String _textListValue = "textListValue";

    private Instant dateValue;
    public static final String _dateValue = "dateValue";

	private Boolean booleanValue;
    public static final String _booleanValue = "booleanValue";
    
    private List<ReferencePersist> references;
    public static final String _references = "references";

    private ReferencePersist reference;
    public static final String _reference = "reference";

    private List<String> tags;
    public static final String _tags = "tags";

    private ExternalIdentifierPersist externalIdentifier;
    public static final String _externalIdentifier = "externalIdentifier";

    public String getTextValue() {
        return this.textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public List<String> getTextListValue() {
        return this.textListValue;
    }

    public void setTextListValue(List<String> textListValue) {
        this.textListValue = textListValue;
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

    public ReferencePersist getReference() {
        return this.reference;
    }

    public void setReference(ReferencePersist reference) {
        this.reference = reference;
    }

    public ExternalIdentifierPersist getExternalIdentifier() {
        return this.externalIdentifier;
    }

    public void setExternalIdentifier(ExternalIdentifierPersist externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Component(PersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PersistValidator extends BaseValidator<FieldPersist> {

        public static final String ValidatorName = "Description.FieldPersistValidator";
        private final ValidatorFactory validatorFactory;

        private final MessageSource messageSource;
        private FieldEntity fieldEntity;
        private DescriptionStatus status;
        private VisibilityService visibilityService;
        private Integer ordinal;

        protected PersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory, MessageSource messageSource) {
            super(conventionService, errors);
	        this.validatorFactory = validatorFactory;
	        this.messageSource = messageSource;
        }

        @Override
        protected Class<FieldPersist> modelClass() {
            return FieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(FieldPersist item) {
            FieldType fieldType = this.fieldEntity != null && this.fieldEntity.getData() != null ? this.fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
	        boolean required = this.fieldEntity != null && this.fieldEntity.getValidations() != null ? this.fieldEntity.getValidations().contains(FieldValidationType.Required) : false;
            boolean isUrlRequired = this.fieldEntity != null && this.fieldEntity.getValidations() != null ? this.fieldEntity.getValidations().contains(FieldValidationType.Url) : false;
            boolean isVisible =  this.fieldEntity != null ? this.visibilityService.isVisible(this.fieldEntity.getId(), this.ordinal) : true;
            return Arrays.asList(
                    this.spec()
                            .iff(()-> FieldType.isTextType(fieldType) && DescriptionStatus.Finalized.equals(this.status) && isVisible && required)
                            .must(() -> !this.isEmpty(item.getTextValue()))
                            .failOn(FieldPersist._textValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._textValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> fieldType.equals(FieldType.FREE_TEXT) && DescriptionStatus.Finalized.equals(this.status) && isVisible && isUrlRequired && (required || !this.isEmpty(item.getTextValue())))
                            .must(() -> this.isValidURL(item.getTextValue()))
                            .failOn(FieldPersist._textValue).failWith(this.messageSource.getMessage("Validation_UrlRequired", new Object[]{FieldPersist._textValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isDateType(fieldType) && DescriptionStatus.Finalized.equals(this.status) && isVisible && required)
                            .must(() -> !this.isNull(item.getDateValue()))
                            .failOn(FieldPersist._dateValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._dateValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isBooleanType(fieldType) && DescriptionStatus.Finalized.equals(this.status) && isVisible && required)
                            .must(() -> !this.isNull(item.getBooleanValue()))
                            .failOn(FieldPersist._booleanValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._booleanValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isExternalIdentifierType(fieldType) && DescriptionStatus.Finalized.equals(this.status) && isVisible  && required)
                            .must(() -> !this.isNull(item.getExternalIdentifier()))
                            .failOn(FieldPersist._externalIdentifier).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._externalIdentifier}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isTextListType(fieldType) && DescriptionStatus.Finalized.equals(this.status) && isVisible && required)
                            .must(() -> !this.isListNullOrEmpty(item.getTextListValue()) || !this.isEmpty(item.getTextValue()))
                            .failOn(FieldPersist._textListValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._textListValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> FieldType.isTagType(fieldType) && DescriptionStatus.Finalized.equals(this.status) && isVisible && required)
                            .must(() -> !this.isListNullOrEmpty(item.getTags()))
                            .failOn(FieldPersist._textListValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._textListValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()->  FieldType.isReferenceType(fieldType) && DescriptionStatus.Finalized.equals(this.status) && isVisible && required)
                            .must(() -> !this.isListNullOrEmpty(item.getReferences()) || !this.isNull(item.getReference()))
                            .failOn(FieldPersist._textListValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._textListValue}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> !this.isNull(item.getTextListValue()) && (fieldType.equals(FieldType.INTERNAL_ENTRIES_PLANS)  || fieldType.equals(FieldType.INTERNAL_ENTRIES_DESCRIPTIONS)))
                            .must(() -> item.getTextListValue().stream().allMatch(this::isUUID))
                            .failOn(FieldPersist._textListValue).failWith(this.messageSource.getMessage("Validation_UnexpectedValue", new Object[]{FieldPersist._textListValue}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> FieldType.isReferenceType(fieldType) &&  !this.isListNullOrEmpty(item.getReferences()))
                            .on(FieldPersist._references)
                            .over(item.getReferences())
                            .using((itm) -> this.validatorFactory.validator(ReferencePersist.ReferenceWithoutTypePersistValidator.class)),
                    this.refSpec()
                            .iff(() -> FieldType.isReferenceType(fieldType) &&  !this.isNull(item.getReference()))
                            .on(FieldPersist._reference)
                            .over(item.getReference())
                            .using(() -> this.validatorFactory.validator(ReferencePersist.ReferenceWithoutTypePersistValidator.class)),
                    this.refSpec()
                            .iff(() -> FieldType.isExternalIdentifierType(fieldType) && !this.isNull(item.getExternalIdentifier()) && DescriptionStatus.Finalized.equals(this.status) && isVisible && required)
                            .on(FieldPersist._externalIdentifier)
                            .over(item.getExternalIdentifier())
                            .using(() -> this.validatorFactory.validator(ExternalIdentifierPersist.PersistValidator.class))
            );
        }

        public PersistValidator withFieldEntity(FieldEntity fieldEntity) {
            this.fieldEntity = fieldEntity;
            return this;
        }

        public PersistValidator setStatus(DescriptionStatus status) {
            this.status = status;
            return this;
        }

        public PersistValidator withVisibilityService(VisibilityService visibilityService) {
            this.visibilityService = visibilityService;
            return this;
        }

        public PersistValidator withOrdinal(Integer ordinal) {
            this.ordinal = ordinal;
            return this;
        }

        boolean isValidURL(String url){
            try {
                new URL(url).toURI();
                return true;
            } catch (MalformedURLException e) {
                return false;
            } catch (URISyntaxException e) {
                return false;
            }
        }
    }

}


