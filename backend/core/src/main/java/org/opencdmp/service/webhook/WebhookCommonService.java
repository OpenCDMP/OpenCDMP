package org.opencdmp.service.webhook;

import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.WebhookEventType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serialization of the subscription event-type list (stored as a JSON array of the
 * enum short values), shared by the builder and the webhook service.
 */
@Service
public class WebhookCommonService {

    private final JsonHandlingService jsonHandlingService;

    public WebhookCommonService(JsonHandlingService jsonHandlingService) {
        this.jsonHandlingService = jsonHandlingService;
    }

    public List<WebhookEventType> parseEventTypes(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        Short[] values = this.jsonHandlingService.fromJsonSafe(Short[].class, json);
        if (values == null) return Collections.emptyList();
        List<WebhookEventType> eventTypes = new ArrayList<>();
        for (Short value : values) {
            WebhookEventType eventType = WebhookEventType.of(value);
            if (eventType != null) eventTypes.add(eventType);
        }
        return eventTypes;
    }

    public String serializeEventTypes(List<WebhookEventType> eventTypes) {
        List<Short> values = new ArrayList<>();
        if (eventTypes != null) for (WebhookEventType eventType : eventTypes) values.add(eventType.getValue());
        return this.jsonHandlingService.toJsonSafe(values);
    }
}
