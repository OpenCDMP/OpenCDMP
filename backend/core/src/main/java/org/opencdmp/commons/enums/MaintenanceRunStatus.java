package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum MaintenanceRunStatus implements DatabaseEnum<Short> {
	Running((short) 0),
	Succeeded((short) 1),
	Failed((short) 2),
	Stopped((short) 3),
	Interrupted((short) 4);
	private final Short value;

	MaintenanceRunStatus(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, MaintenanceRunStatus> map = EnumUtils.getEnumValueMap(MaintenanceRunStatus.class);

	public static MaintenanceRunStatus of(Short i) {
		return map.get(i);
	}
}
