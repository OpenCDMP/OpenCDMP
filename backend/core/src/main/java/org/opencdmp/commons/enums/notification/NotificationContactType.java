package org.opencdmp.commons.enums.notification;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum NotificationContactType implements DatabaseEnum<Short> {

    EMAIL((short) 0),
    SLACK_BROADCAST((short) 1),
    SMS((short) 2),
    IN_APP((short) 3);

    private final Short value;

    NotificationContactType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, NotificationContactType> map = EnumUtils.getEnumValueMap(NotificationContactType.class);

    public static NotificationContactType of(Short i) {
        return map.get(i);
    }

}
