package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

import java.util.Map;

public enum ExternalFetcherSourceType implements DatabaseEnum<Short> {
	@XmlEnumValue(value = "0")
	API((short) 0),
	@XmlEnumValue(value = "1")
	STATIC((short) 1);
	private final Short value;

	public static class Names {
		public static final String API = "api";
		public static final String STATIC = "static";
	}

	ExternalFetcherSourceType(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, ExternalFetcherSourceType> map = EnumUtils.getEnumValueMap(ExternalFetcherSourceType.class);

	public static ExternalFetcherSourceType of(Short i) {
		return map.get(i);
	}
}
