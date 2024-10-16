package org.opencdmp.commons.types.notification;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum DataType {
	Integer(0),
	Decimal(1),
	Double(2),
	DateTime(3),
	TimeSpan(4),
	String(5);
	private static final Map<Integer, DataType> values = new HashMap<>();

	private final Integer mappedName;

	//For jackson parsing (used by MVC)
	@JsonValue
	public Integer getMappedName() {
		return mappedName;
	}

	static {
		for (DataType e : values()) {
			values.put(e.asInt(), e);
		}
	}

	private DataType(int mappedName) {
		this.mappedName = mappedName;
	}

	public Integer asInt() {
		return this.mappedName;
	}

	public static DataType fromString(Integer value) {
		return values.getOrDefault(value, Integer);
	}
}
