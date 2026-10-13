package org.opencdmp.model.builder.webhook;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.WebhookEventType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.WebhookSubscriptionEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.webhook.WebhookSubscription;
import org.opencdmp.service.webhook.WebhookCommonService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WebhookSubscriptionBuilder extends BaseBuilder<WebhookSubscription, WebhookSubscriptionEntity> {

    private final TenantScopeFactory tenantScopeFactory;
    private final WebhookCommonService webhookCommonService;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public WebhookSubscriptionBuilder(
            ConventionService conventionService,
            TenantScopeFactory tenantScopeFactory, WebhookCommonService webhookCommonService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(WebhookSubscriptionBuilder.class)));
        this.tenantScopeFactory = tenantScopeFactory;
        this.webhookCommonService = webhookCommonService;
    }

    public WebhookSubscriptionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<WebhookSubscription> build(FieldSet fields, List<WebhookSubscriptionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<WebhookSubscription> models = new ArrayList<>();
        for (WebhookSubscriptionEntity d : data) {
            WebhookSubscription m = new WebhookSubscription();
            if (fields.hasField(this.asIndexer(WebhookSubscription._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(WebhookSubscription._label)))
                m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(WebhookSubscription._url)))
                m.setUrl(d.getUrl());
            // The secret is deliberately never built — it is returned once, by the creating persist call.
            if (fields.hasField(this.asIndexer(WebhookSubscription._eventTypes))) {
                List<WebhookEventType> eventTypes = this.webhookCommonService.parseEventTypes(d.getEventTypes());
                m.setEventTypes(eventTypes.isEmpty() ? null : eventTypes);
            }
            if (fields.hasField(this.asIndexer(WebhookSubscription._enabled)))
                m.setEnabled(d.getEnabled());
            if (fields.hasField(this.asIndexer(WebhookSubscription._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(WebhookSubscription._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(WebhookSubscription._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(WebhookSubscription._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(WebhookSubscription._belongsToCurrentTenant)))
                m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScopeFactory.getInstance()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
