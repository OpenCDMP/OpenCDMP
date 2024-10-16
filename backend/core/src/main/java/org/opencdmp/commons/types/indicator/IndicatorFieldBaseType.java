package org.opencdmp.commons.types.indicator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum IndicatorFieldBaseType {
	String("string"),
	Keyword("keyword"),
	Integer("integer"),
	Double("double"),
	Date("date"),
	IntegerMap("integerMap"),
	DoubleMap("doubleMap"),
	IntegerArray("integerArray"),
	DoubleArray("doubleArray"),
	KeywordArray("keywordArray"),
	;
	private static final Map<String, IndicatorFieldBaseType> values = new HashMap<>();

	private final String mappedName;

	//For jackson parsing (used by MVC)
	@JsonValue
	public java.lang.String getMappedName() {
		return mappedName;
	}

	static {
		for (IndicatorFieldBaseType e : values()) {
			values.put(e.asString(), e);
		}
	}

	private IndicatorFieldBaseType(String mappedName) {
		this.mappedName = mappedName;
	}

	public String asString() {
		return this.mappedName;
	}

	public static IndicatorFieldBaseType fromString(String value) {
		return values.getOrDefault(value, String);
	}
}
