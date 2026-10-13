package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.WebhookEventType;

@Converter
public class WebhookEventTypeConverter extends DatabaseEnumConverter<WebhookEventType, Short> {
    public WebhookEventType of(Short i) {
        return WebhookEventType.of(i);
    }
}
