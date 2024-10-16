package org.opencdmp.commons.enums.annotation;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum AnnotationEntityType implements DatabaseEnum<String> {
	Description(EntityType.description),
	Plan(EntityType.plan);
	private final String value;

	public static class EntityType {
		public static final String description = "description";
		public static final String plan = "plan";
	}

	AnnotationEntityType(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return this.value;
	}

	private static final Map<String, AnnotationEntityType> map = EnumUtils.getEnumValueMap(AnnotationEntityType.class);

	public static AnnotationEntityType of(String i) {
		return map.get(i);
	}
}
