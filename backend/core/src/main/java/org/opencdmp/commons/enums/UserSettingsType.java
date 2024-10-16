package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum UserSettingsType implements DatabaseEnum<Short> {

	Settings((short) 0),
	Config((short) 1);

	private final Short value;

	UserSettingsType(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, UserSettingsType> map = EnumUtils.getEnumValueMap(UserSettingsType.class);

	public static UserSettingsType of(Short i) {
		return map.get(i);
	}
}

