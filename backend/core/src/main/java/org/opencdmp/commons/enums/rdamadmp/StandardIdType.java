package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.HashMap;
import java.util.Map;

public enum StandardIdType implements DatabaseEnum<String> {

    URL("url"),
    OTHER("other");
    private final String value;
    private final static Map<String, StandardIdType> CONSTANTS = new HashMap<String, StandardIdType>();

    StandardIdType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, StandardIdType> map = EnumUtils.getEnumValueMap(StandardIdType.class);

    public static StandardIdType of(String i) {
        return map.get(i);
    }

    public static StandardIdType fromValue(String value) {
        StandardIdType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }
}
