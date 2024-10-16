package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum FieldType implements DatabaseEnum<String> {
	SELECT(Names.Select),
	BOOLEAN_DECISION(Names.BooleanDecision),
	RADIO_BOX(Names.RadioBox),
	INTERNAL_ENTRIES_PLANS(Names.InternalEntitiesPlans),
	INTERNAL_ENTRIES_DESCRIPTIONS(Names.InternalEntitiesDescriptions),
	CHECK_BOX(Names.CheckBox),
	FREE_TEXT(Names.FreeText),
	TEXT_AREA(Names.TextArea),
	RICH_TEXT_AREA(Names.RichTextarea),
	UPLOAD(Names.Upload),
	DATE_PICKER(Names.DatePicker),
	TAGS(Names.Tags),
	REFERENCE_TYPES(Names.ReferenceTypes),
	DATASET_IDENTIFIER(Names.DatasetIdentifier),
	VALIDATION(Names.Validation);
	private final String value;
	
	public static class Names {
		public static final String Select = "select";
		public static final String BooleanDecision = "booleanDecision";
		public static final String RadioBox = "radiobox";
		public static final String InternalEntitiesPlans = "internalEntitiesPlans";
		public static final String InternalEntitiesDescriptions = "internalEntitiesDescriptions";
		public static final String CheckBox = "checkBox";
		public static final String FreeText = "freetext";
		public static final String TextArea = "textarea";
		public static final String RichTextarea = "richTextarea";
		public static final String Upload = "upload";
		public static final String DatePicker = "datePicker";
		public static final String Tags = "tags";
		public static final String DatasetIdentifier = "datasetIdentifier";
		public static final String Validation = "validation";
		public static final String ReferenceTypes = "referenceTypes";
	}

	FieldType(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return this.value;
	}

	private static final Map<String, FieldType> map = EnumUtils.getEnumValueMap(FieldType.class);

	public static FieldType of(String i) {
		return map.get(i);
	}


	public static boolean isReferenceType(FieldType fieldType){
		return fieldType.equals(FieldType.REFERENCE_TYPES);
	}

	public static boolean isTextType(FieldType fieldType){
		return  fieldType.equals(FieldType.FREE_TEXT)  || fieldType.equals(FieldType.TEXT_AREA) ||
				fieldType.equals(FieldType.RICH_TEXT_AREA) || fieldType.equals(FieldType.UPLOAD) ||
				fieldType.equals(FieldType.RADIO_BOX);
	}

	public static boolean isTextListType(FieldType fieldType){
		return  fieldType.equals(FieldType.SELECT)  || fieldType.equals(FieldType.INTERNAL_ENTRIES_PLANS) ||
				fieldType.equals(FieldType.INTERNAL_ENTRIES_DESCRIPTIONS);
	}

	public static boolean isTagType(FieldType fieldType){
		return fieldType.equals(FieldType.TAGS);
	}

	public static boolean isDateType(FieldType fieldType){
		return  fieldType.equals(FieldType.DATE_PICKER);
	}
	public static boolean isBooleanType(FieldType fieldType){
		return  fieldType.equals(FieldType.BOOLEAN_DECISION) || fieldType.equals(FieldType.CHECK_BOX);
	}

	public static boolean isExternalIdentifierType(FieldType fieldType){
		return  fieldType.equals(FieldType.VALIDATION) ||  fieldType.equals(FieldType.DATASET_IDENTIFIER) ;
	}
}
