package org.opencdmp.service.webhook;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.commons.enums.WebhookEventType;
import org.opencdmp.model.persist.WebhookSubscriptionPersist;
import org.opencdmp.model.webhook.WebhookSubscription;

import javax.management.InvalidApplicationException;
import java.util.Map;
import java.util.UUID;

public interface WebhookService {

    WebhookSubscription persist(WebhookSubscriptionPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

    /**
     * Queues a callback for every enabled subscription of the current tenant that
     * listens to the given event type. Rows are written in the caller's transaction;
     * failures are swallowed so webhooks can never break a domain flow.
     */
    void enqueueEvent(WebhookEventType eventType, Map<String, Object> data);

    void redeliver(UUID deliveryId) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException;

    void sendPing(UUID subscriptionId) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException;
}
