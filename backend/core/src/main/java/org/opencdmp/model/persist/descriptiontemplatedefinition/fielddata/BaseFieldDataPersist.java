package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "fieldType",
        visible = true,
        defaultImpl = LabelDataPersist.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.BooleanDecision),
        @JsonSubTypes.Type(value = LabelAndMultiplicityDataPersist.class, name = FieldType.Names.InternalEntitiesDescriptions),
        @JsonSubTypes.Type(value = LabelAndMultiplicityDataPersist.class, name = FieldType.Names.InternalEntitiesPlans),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.CheckBox),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.DatePicker),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.FreeText),
        @JsonSubTypes.Type(value = ReferenceTypeDataPersist.class, name = FieldType.Names.ReferenceTypes),
        @JsonSubTypes.Type(value = RadioBoxDataPersist.class, name = FieldType.Names.RadioBox),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.RichTextarea),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.Tags),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.TextArea),
        @JsonSubTypes.Type(value = UploadDataPersist.class, name = FieldType.Names.Upload),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.Validation),
        @JsonSubTypes.Type(value = LabelDataPersist.class, name = FieldType.Names.DatasetIdentifier),
        @JsonSubTypes.Type(value = SelectDataPersist.class, name = FieldType.Names.Select)
})
public abstract class BaseFieldDataPersist {

    private String label;

    public static final String _label = "label";

    private FieldType fieldType;

    public static final String _fieldType = "fieldType";

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FieldType getFieldType() {
        return this.fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public abstract static class BaseFieldDataPersistValidator<T extends BaseFieldDataPersist> extends BaseValidator<T> {

        protected final MessageSource messageSource;

        protected BaseFieldDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        protected List<Specification> getBaseSpecifications(T item) {
	        return new ArrayList<>(Arrays.asList(
		            this.spec()
				            .must(() -> !this.isNull(item.getFieldType()))
				            .failOn(BaseFieldDataPersist._fieldType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{BaseFieldDataPersist._fieldType}, LocaleContextHolder.getLocale()))
            ));
        }

    }

}
