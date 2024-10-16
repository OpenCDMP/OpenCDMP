package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum ActionConfirmationStatus implements DatabaseEnum<Short> {

    Requested((short) 0),
    Accepted((short) 1);

    private final Short value;

    ActionConfirmationStatus(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, ActionConfirmationStatus> map = EnumUtils.getEnumValueMap(ActionConfirmationStatus.class);

    public static ActionConfirmationStatus of(Short i) {
        return map.get(i);
    }


}
