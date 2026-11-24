package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum EntityDoiStatus implements DatabaseEnum<Short> {
	Pending((short) 0),
	Published((short) 1),
	Failed((short) 2);
	private final Short value;

	EntityDoiStatus(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, EntityDoiStatus> map = EnumUtils.getEnumValueMap(EntityDoiStatus.class);

	public static EntityDoiStatus of(Short i) {
		return map.get(i);
	}
}
