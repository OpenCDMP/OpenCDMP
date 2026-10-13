package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

import java.util.Map;

public enum ExternalFetcherApiResponseFormatType implements DatabaseEnum<Short> {
	@XmlEnumValue(value = "0")
	JSON((short) 0),
	@XmlEnumValue(value = "1")
	XML((short) 1);

	private final Short value;

	ExternalFetcherApiResponseFormatType(Short value) {
		this.value = value;
	}

	@JsonValue
	public Short getValue() {
		return value;
	}

	private static final Map<Short, ExternalFetcherApiResponseFormatType> map = EnumUtils.getEnumValueMap(ExternalFetcherApiResponseFormatType.class);

	public static ExternalFetcherApiResponseFormatType of(Short i) {
		return map.get(i);
	}
}
