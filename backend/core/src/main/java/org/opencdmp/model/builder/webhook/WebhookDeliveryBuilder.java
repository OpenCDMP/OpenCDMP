package org.opencdmp.model.builder.webhook;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.WebhookDeliveryEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.webhook.WebhookDelivery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WebhookDeliveryBuilder extends BaseBuilder<WebhookDelivery, WebhookDeliveryEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public WebhookDeliveryBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(WebhookDeliveryBuilder.class)));
    }

    public WebhookDeliveryBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<WebhookDelivery> build(FieldSet fields, List<WebhookDeliveryEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<WebhookDelivery> models = new ArrayList<>();
        for (WebhookDeliveryEntity d : data) {
            WebhookDelivery m = new WebhookDelivery();
            if (fields.hasField(this.asIndexer(WebhookDelivery._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(WebhookDelivery._subscriptionId)))
                m.setSubscriptionId(d.getSubscriptionId());
            if (fields.hasField(this.asIndexer(WebhookDelivery._eventType)))
                m.setEventType(d.getEventType());
            if (fields.hasField(this.asIndexer(WebhookDelivery._payload)))
                m.setPayload(d.getPayload());
            if (fields.hasField(this.asIndexer(WebhookDelivery._status)))
                m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(WebhookDelivery._attempts)))
                m.setAttempts(d.getAttempts());
            if (fields.hasField(this.asIndexer(WebhookDelivery._lastHttpStatus)))
                m.setLastHttpStatus(d.getLastHttpStatus());
            if (fields.hasField(this.asIndexer(WebhookDelivery._lastError)))
                m.setLastError(d.getLastError());
            if (fields.hasField(this.asIndexer(WebhookDelivery._nextRetryAt)))
                m.setNextRetryAt(d.getNextRetryAt());
            if (fields.hasField(this.asIndexer(WebhookDelivery._deliveredAt)))
                m.setDeliveredAt(d.getDeliveredAt());
            if (fields.hasField(this.asIndexer(WebhookDelivery._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(WebhookDelivery._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(WebhookDelivery._isActive)))
                m.setIsActive(d.getIsActive());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
