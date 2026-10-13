package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.WebhookDeliveryStatus;
import org.opencdmp.commons.enums.WebhookEventType;
import org.opencdmp.query.WebhookDeliveryQuery;

import java.util.List;
import java.util.UUID;

public class WebhookDeliveryLookup extends Lookup {

    private List<UUID> ids;
    private List<UUID> subscriptionIds;
    private List<WebhookDeliveryStatus> statuses;
    private List<WebhookEventType> eventTypes;
    private List<IsActive> isActive;

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getSubscriptionIds() {
        return subscriptionIds;
    }

    public void setSubscriptionIds(List<UUID> subscriptionIds) {
        this.subscriptionIds = subscriptionIds;
    }

    public List<WebhookDeliveryStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<WebhookDeliveryStatus> statuses) {
        this.statuses = statuses;
    }

    public List<WebhookEventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<WebhookEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public WebhookDeliveryQuery enrich(QueryFactory queryFactory) {
        WebhookDeliveryQuery query = queryFactory.query(WebhookDeliveryQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.subscriptionIds != null) query.subscriptionIds(this.subscriptionIds);
        if (this.statuses != null) query.statuses(this.statuses);
        if (this.eventTypes != null) query.eventTypes(this.eventTypes);
        if (this.isActive != null) query.isActive(this.isActive);

        this.enrichCommon(query);

        return query;
    }
}
