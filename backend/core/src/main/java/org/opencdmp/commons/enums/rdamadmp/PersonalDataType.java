package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.HashMap;
import java.util.Map;

public enum PersonalDataType implements DatabaseEnum<String> {

    YES("yes"),
    NO("no"),
    UNKNOWN("unknown");
    private final String value;
    private final static Map<String, PersonalDataType> CONSTANTS = new HashMap<String, PersonalDataType>();

    PersonalDataType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, PersonalDataType> map = EnumUtils.getEnumValueMap(PersonalDataType.class);

    public static PersonalDataType of(String i) {
        return map.get(i);
    }

    public static PersonalDataType fromValue(String value) {
        PersonalDataType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }

}
