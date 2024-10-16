package org.opencdmp.model.persist.planproperties;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.commons.types.planblueprint.FieldEntity;
import org.opencdmp.commons.types.planblueprint.ReferenceTypeFieldEntity;
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
import java.util.UUID;

public class PlanBlueprintValuePersist {
    private UUID fieldId;

    public static final String _fieldId = "fieldId";
    private String fieldValue;
    public static final String _fieldValue = "fieldValue";

    private Instant dateValue;
    public static final String _dateValue = "dateValue";

    private Double numberValue;
    public static final String _numberValue = "numberValue";

    private List<ReferencePersist> references;
    public static final String _references = "references";
    private ReferencePersist reference;
    public static final String _reference = "reference";

    public UUID getFieldId() {
        return this.fieldId;
    }

    public void setFieldId(UUID fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldValue() {
        return this.fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public Instant getDateValue() {
        return this.dateValue;
    }

    public void setDateValue(Instant dateValue) {
        this.dateValue = dateValue;
    }

    public Double getNumberValue() {
        return this.numberValue;
    }

    public void setNumberValue(Double numberValue) {
        this.numberValue = numberValue;
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

    @Component(PlanBlueprintValuePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanBlueprintValuePersistValidator extends BaseValidator<PlanBlueprintValuePersist> {

        private final ValidatorFactory validatorFactory;
        private final QueryFactory queryFactory;
        private final MessageSource messageSource;

        public static final String ValidatorName = "PlanBlueprintValuePersistValidator";

        private DefinitionEntity definition;
        private FieldEntity fieldEntity;
        private ExtraFieldEntity extraFieldEntity;

        protected PlanBlueprintValuePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory, QueryFactory queryFactory, MessageSource messageSource) {
            super(conventionService, errors);
            this.validatorFactory = validatorFactory;
            this.queryFactory = queryFactory;
            this.messageSource = messageSource;
        }

        @Override
        protected Class<PlanBlueprintValuePersist> modelClass() {
            return PlanBlueprintValuePersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanBlueprintValuePersist item) {
	        this.fieldEntity = this.definition != null && this.isValidGuid(item.getFieldId())? this.definition.getFieldById(item.getFieldId()).stream().findFirst().orElse(null) : null;
            boolean required = this.fieldEntity != null && this.fieldEntity.isRequired();
            if (this.fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.Extra)) this.extraFieldEntity = (ExtraFieldEntity) this.fieldEntity;

            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getFieldId()))
                            .failOn(PlanBlueprintValuePersist._fieldId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintValuePersist._fieldId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isTextType(this.extraFieldEntity.getType()) && this.isListNullOrEmpty(item.getReferences()) && required)
                            .must(() -> !this.isEmpty(item.getFieldValue()))
                            .failOn(PlanBlueprintValuePersist._fieldValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{this.fieldEntity.getLabel()}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isDateType(this.extraFieldEntity.getType()) && this.isListNullOrEmpty(item.getReferences()) && required)
                            .must(() -> !this.isNull(item.getDateValue()))
                            .failOn(PlanBlueprintValuePersist._dateValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{this.fieldEntity.getLabel()}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isNumberType(this.extraFieldEntity.getType()) && this.isListNullOrEmpty(item.getReferences()) && required)
                            .must(() -> !this.isNull(item.getNumberValue()))
                            .failOn(PlanBlueprintValuePersist._numberValue).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{this.fieldEntity.getLabel()}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.ReferenceType) && this.isEmpty(item.getFieldValue()) && ((ReferenceTypeFieldEntity) this.fieldEntity).getMultipleSelect() && required)
                            .must(() -> !this.isListNullOrEmpty(item.getReferences()))
                            // TODO: Cast Exception
                            .failOn(PlanBlueprintValuePersist._references).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{!this.isEmpty(this.fieldEntity.getLabel()) ? this.fieldEntity.getLabel() : PlanBlueprintValuePersist._references}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> this.fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.ReferenceType) && this.isEmpty(item.getFieldValue()) && !((ReferenceTypeFieldEntity) this.fieldEntity).getMultipleSelect() && required)
                            .must(() -> !this.isNull(item.getReference()))
                            // TODO: Cast Exception
                            .failOn(PlanBlueprintValuePersist._reference).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{!this.isEmpty(this.fieldEntity.getLabel()) ? this.fieldEntity.getLabel() : PlanBlueprintValuePersist._reference}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getReferences()))
                            .on(PlanBlueprintValuePersist._references)
                            .over(item.getReferences())
                            .using((itm) -> this.validatorFactory.validator(ReferencePersist.ReferenceWithoutTypePersistValidator.class)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getReference()))
                            .on(PlanBlueprintValuePersist._reference)
                            .over(item.getReferences())
                            .using(() -> this.validatorFactory.validator(ReferencePersist.ReferenceWithoutTypePersistValidator.class))
            );
        }

        public PlanBlueprintValuePersistValidator withDefinition(DefinitionEntity definition) {
            this.definition = definition;
            return this;
        }
    }

}
