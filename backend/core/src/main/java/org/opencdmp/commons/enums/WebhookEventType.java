package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum WebhookEventType implements DatabaseEnum<Short> {

    PlanFinalized((short) 0),
    PlanStatusChanged((short) 1),
    PlanUnfinalized((short) 2),
    PlanDeposited((short) 3),
    DescriptionFinalized((short) 4),
    Ping((short) 100);

    private final Short value;

    WebhookEventType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, WebhookEventType> map = EnumUtils.getEnumValueMap(WebhookEventType.class);

    public static WebhookEventType of(Short i) {
        return map.get(i);
    }
}
