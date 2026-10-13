package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum WebhookDeliveryStatus implements DatabaseEnum<Short> {

    Pending((short) 0),
    Delivered((short) 1),
    Failed((short) 2);

    private final Short value;

    WebhookDeliveryStatus(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, WebhookDeliveryStatus> map = EnumUtils.getEnumValueMap(WebhookDeliveryStatus.class);

    public static WebhookDeliveryStatus of(Short i) {
        return map.get(i);
    }
}
