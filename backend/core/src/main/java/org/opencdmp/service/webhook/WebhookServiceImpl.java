package org.opencdmp.service.webhook;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.WebhookDeliveryStatus;
import org.opencdmp.commons.enums.WebhookEventType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.WebhookDeliveryEntity;
import org.opencdmp.data.WebhookSubscriptionEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.builder.webhook.WebhookSubscriptionBuilder;
import org.opencdmp.model.deleter.WebhookSubscriptionDeleter;
import org.opencdmp.model.persist.WebhookSubscriptionPersist;
import org.opencdmp.model.webhook.WebhookSubscription;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.WebhookDeliveryQuery;
import org.opencdmp.query.WebhookSubscriptionQuery;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookServiceImpl implements WebhookService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(WebhookServiceImpl.class));

    private final WebhookProperties properties;
    private final AuthorizationService authorizationService;
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final JsonHandlingService jsonHandlingService;
    private final WebhookCommonService webhookCommonService;

    public WebhookServiceImpl(
            WebhookProperties properties,
            AuthorizationService authorizationService,
            QueryFactory queryFactory,
            BuilderFactory builderFactory,
            DeleterFactory deleterFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            ErrorThesaurusProperties errors,
            TenantEntityManagerFactory tenantEntityManagerFactory,
            TenantScopeFactory tenantScopeFactory,
            JsonHandlingService jsonHandlingService,
            WebhookCommonService webhookCommonService) {
        this.properties = properties;
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.errors = errors;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.webhookCommonService = webhookCommonService;
    }

    @Override
    public WebhookSubscription persist(WebhookSubscriptionPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        this.authorizationService.authorizeForce(Permission.EditWebhookSubscription);

        boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        String createdSecret = null;
        WebhookSubscriptionEntity data;
        if (isUpdate) {
            data = this.queryFactory.query(WebhookSubscriptionQuery.class).ids(model.getId()).isActive(IsActive.Active).first();
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), WebhookSubscription.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new WebhookSubscriptionEntity();
            data.setId(UUID.randomUUID());
            createdSecret = generateSecret();
            data.setSecret(createdSecret);
            data.setCreatedAt(Instant.now());
            data.setIsActive(IsActive.Active);
        }

        data.setLabel(model.getLabel());
        data.setUrl(model.getUrl());
        data.setEventTypes(this.webhookCommonService.serializeEventTypes(model.getEventTypes()));
        data.setEnabled(model.getEnabled());
        data.setUpdatedAt(Instant.now());

        if (isUpdate) this.tenantEntityManagerFactory.getInstance().merge(data);
        else this.tenantEntityManagerFactory.getInstance().persist(data);

        this.tenantEntityManagerFactory.getInstance().flush();

        WebhookSubscription persisted = this.builderFactory.builder(WebhookSubscriptionBuilder.class)
                .build(BaseFieldSet.build(fields, WebhookSubscription._id, WebhookSubscription._hash), data);
        // The secret is exposed exactly once — in the response of the creating call.
        if (createdSecret != null && persisted != null) persisted.setSecret(createdSecret);
        return persisted;
    }

    private static String generateSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        this.authorizationService.authorizeForce(Permission.DeleteWebhookSubscription);
        this.deleterFactory.deleter(WebhookSubscriptionDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    @Override
    public void enqueueEvent(WebhookEventType eventType, Map<String, Object> data) {
        if (!this.properties.getEnabled()) return;
        try {
            WebhookSubscriptionQuery query = this.queryFactory.query(WebhookSubscriptionQuery.class)
                    .disableTracking()
                    .isActive(IsActive.Active)
                    .enabled(true);
            // the tenant filter also matches default-tenant rows (tenant IS NULL), so pin the event's own tenant
            UUID tenantId = this.tenantScopeFactory.getInstance().getTenant();
            if (tenantId == null) query.tenantIsSet(false);
            else query.tenantSubQuery(this.queryFactory.query(TenantQuery.class).disableTracking().ids(tenantId));
            List<WebhookSubscriptionEntity> subscriptions = query.collect();
            if (subscriptions == null || subscriptions.isEmpty()) return;

            String tenantCode = this.tenantScopeFactory.getInstance().getTenantCode();

            for (WebhookSubscriptionEntity subscription : subscriptions) {
                if (!this.webhookCommonService.parseEventTypes(subscription.getEventTypes()).contains(eventType)) continue;
                this.enqueueDelivery(subscription, eventType, tenantCode, data);
            }
        } catch (Exception ex) {
            // Webhooks are observers — they must never break the domain flow that triggered them.
            logger.error("Problem enqueueing webhook event " + eventType + ": " + ex.getMessage(), ex);
        }
    }

    private void enqueueDelivery(WebhookSubscriptionEntity subscription, WebhookEventType eventType, String tenantCode, Map<String, Object> data) throws InvalidApplicationException {
        WebhookDeliveryEntity delivery = new WebhookDeliveryEntity();
        delivery.setId(UUID.randomUUID());
        delivery.setSubscriptionId(subscription.getId());
        delivery.setEventType(eventType);
        delivery.setStatus(WebhookDeliveryStatus.Pending);
        delivery.setAttempts(0);
        delivery.setCreatedAt(Instant.now());
        delivery.setUpdatedAt(Instant.now());
        delivery.setIsActive(IsActive.Active);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", delivery.getId());
        payload.put("eventType", eventType.name());
        payload.put("tenantCode", tenantCode);
        payload.put("timestamp", Instant.now().toString());
        payload.put("data", data != null ? data : Map.of());
        delivery.setPayload(this.jsonHandlingService.toJsonSafe(payload));

        this.tenantEntityManagerFactory.getInstance().persist(delivery);
    }

    @Override
    public void redeliver(UUID deliveryId) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        this.authorizationService.authorizeForce(Permission.EditWebhookSubscription);

        WebhookDeliveryEntity delivery = this.queryFactory.query(WebhookDeliveryQuery.class).ids(deliveryId).isActive(List.of(IsActive.Active)).first();
        if (delivery == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{deliveryId, WebhookDeliveryEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        delivery.setStatus(WebhookDeliveryStatus.Pending);
        delivery.setNextRetryAt(null);
        delivery.setUpdatedAt(Instant.now());
        this.tenantEntityManagerFactory.getInstance().merge(delivery);
        this.tenantEntityManagerFactory.getInstance().flush();
    }

    @Override
    public void sendPing(UUID subscriptionId) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        this.authorizationService.authorizeForce(Permission.EditWebhookSubscription);

        WebhookSubscriptionQuery query = this.queryFactory.query(WebhookSubscriptionQuery.class).disableTracking().ids(subscriptionId).isActive(IsActive.Active);
        UUID tenantId = this.tenantScopeFactory.getInstance().getTenant();
        if (tenantId == null) query.tenantIsSet(false);
        else query.tenantSubQuery(this.queryFactory.query(TenantQuery.class).disableTracking().ids(tenantId));
        WebhookSubscriptionEntity subscription = query.first();
        if (subscription == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{subscriptionId, WebhookSubscription.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.enqueueDelivery(subscription, WebhookEventType.Ping, this.tenantScopeFactory.getInstance().getTenantCode(), Map.of("message", "ping"));
        this.tenantEntityManagerFactory.getInstance().flush();
    }
}
