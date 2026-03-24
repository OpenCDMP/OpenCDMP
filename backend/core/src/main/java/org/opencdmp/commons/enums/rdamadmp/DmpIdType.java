package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DmpIdType implements DatabaseEnum<String> {

    HANDLE("handle"),
    DOI("doi"),
    ARK("ark"),
    URL("url"),
    OTHER("other");
    private final String value;

    DmpIdType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, DmpIdType> map = EnumUtils.getEnumValueMap(DmpIdType.class);

    public static DmpIdType of(String i) {
        return map.get(i);
    }

}
