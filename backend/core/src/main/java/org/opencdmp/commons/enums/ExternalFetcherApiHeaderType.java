package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum ExternalFetcherApiHeaderType implements DatabaseEnum<String> {
	ACCEPT(Names.Accept),
	CONNECTION(Names.Connection),
	ALLOW_HEADER(Names.AllowHeader),
	ALLOW_ORIGIN(Names.AllowOrigin),
	ALLOW_CREDENTIALS(Names.AllowCredentials),
	ORIGIN(Names.Origin);

	private final String value;

	public static class Names {
		public static final String Accept = "accept";
		public static final String Connection = "connection";
		public static final String AllowHeader = "allowHeader";
		public static final String AllowOrigin = "allowOrigin";
		public static final String AllowCredentials = "allowCredentials";
		public static final String Origin = "origin";
	}

	ExternalFetcherApiHeaderType(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return this.value;
	}

	private static final Map<String, ExternalFetcherApiHeaderType> map = EnumUtils.getEnumValueMap(ExternalFetcherApiHeaderType.class);

	public static ExternalFetcherApiHeaderType of(String i) {
		return map.get(i);
	}

}
