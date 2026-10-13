package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.WebhookDeliveryStatus;

@Converter
public class WebhookDeliveryStatusConverter extends DatabaseEnumConverter<WebhookDeliveryStatus, Short> {
    public WebhookDeliveryStatus of(Short i) {
        return WebhookDeliveryStatus.of(i);
    }
}
