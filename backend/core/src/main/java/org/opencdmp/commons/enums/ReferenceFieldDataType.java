package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum ReferenceFieldDataType implements DatabaseEnum<Short> {
	Text((short) 0),
	Date((short) 1);
	private final Short value;

	ReferenceFieldDataType(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, ReferenceFieldDataType> map = EnumUtils.getEnumValueMap(ReferenceFieldDataType.class);

	public static ReferenceFieldDataType of(Short i) {
		return map.get(i);
	}
}
