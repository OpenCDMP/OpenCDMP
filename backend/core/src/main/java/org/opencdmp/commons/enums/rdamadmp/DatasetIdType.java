package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.HashMap;
import java.util.Map;

public enum DatasetIdType implements DatabaseEnum<String> {

    HANDLE("handle"),
    DOI("doi"),
    ARK("ark"),
    URL("url"),
    OTHER("other");
    private final String value;
    private final static Map<String, DatasetIdType> CONSTANTS = new HashMap<String, DatasetIdType>();

    DatasetIdType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, DatasetIdType> map = EnumUtils.getEnumValueMap(DatasetIdType.class);

    public static DatasetIdType of(String i) {
        return map.get(i);
    }

    public static DatasetIdType fromValue(String value) {
        DatasetIdType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }

}
