package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum MaintenanceTaskRunStatus implements DatabaseEnum<Short> {
	Queued((short) 0),
	Running((short) 1),
	Succeeded((short) 2),
	Failed((short) 3),
	Skipped((short) 4);
	private final Short value;

	MaintenanceTaskRunStatus(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, MaintenanceTaskRunStatus> map = EnumUtils.getEnumValueMap(MaintenanceTaskRunStatus.class);

	public static MaintenanceTaskRunStatus of(Short i) {
		return map.get(i);
	}
}
