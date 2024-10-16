package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum LockTargetType implements DatabaseEnum<Short> {
	Plan((short) 0),
	Description((short) 1),
	PlanBlueprint((short) 2),
	DescriptionTemplate((short) 3);
	private final Short value;

	LockTargetType(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, LockTargetType> map = EnumUtils.getEnumValueMap(LockTargetType.class);

	public static LockTargetType of(Short i) {
		return map.get(i);
	}
}
