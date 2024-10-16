package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

import java.util.Map;

public enum ExternalFetcherApiHTTPMethodType implements DatabaseEnum<Short> {
	@XmlEnumValue(value = "0")
	GET((short) 0),
	@XmlEnumValue(value = "1")
	POST((short) 1);
	private final Short value;

	ExternalFetcherApiHTTPMethodType(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, ExternalFetcherApiHTTPMethodType> map = EnumUtils.getEnumValueMap(ExternalFetcherApiHTTPMethodType.class);

	public static ExternalFetcherApiHTTPMethodType of(Short i) {
		return map.get(i);
	}
}
