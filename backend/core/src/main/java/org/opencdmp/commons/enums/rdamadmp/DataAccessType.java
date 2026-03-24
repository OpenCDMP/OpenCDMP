package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DataAccessType implements DatabaseEnum<String> {

    OPEN("open"),
    SHARED("shared"),
    CLOSED("closed");
    private final String value;

    DataAccessType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, DataAccessType> map = EnumUtils.getEnumValueMap(DataAccessType.class);

    public static DataAccessType of(String i) {
        return map.get(i);
    }

}
