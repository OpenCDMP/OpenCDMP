package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.HashMap;
import java.util.Map;

public enum SensitiveDataType implements DatabaseEnum<String> {

    YES("yes"),
    NO("no"),
    UNKNOWN("unknown");
    private final String value;
    private final static Map<String, SensitiveDataType> CONSTANTS = new HashMap<String, SensitiveDataType>();

    SensitiveDataType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, SensitiveDataType> map = EnumUtils.getEnumValueMap(SensitiveDataType.class);

    public static SensitiveDataType of(String i) {
        return map.get(i);
    }

    public static SensitiveDataType fromValue(String value) {
        SensitiveDataType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }

}
