package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import io.swagger.v3.oas.annotations.Hidden;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.WebhookEventType;
import org.opencdmp.data.WebhookDeliveryEntity;
import org.opencdmp.data.WebhookSubscriptionEntity;
import org.opencdmp.model.builder.webhook.WebhookDeliveryBuilder;
import org.opencdmp.model.builder.webhook.WebhookSubscriptionBuilder;
import org.opencdmp.model.censorship.webhook.WebhookDeliveryCensor;
import org.opencdmp.model.censorship.webhook.WebhookSubscriptionCensor;
import org.opencdmp.model.persist.WebhookSubscriptionPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.model.webhook.WebhookDelivery;
import org.opencdmp.model.webhook.WebhookSubscription;
import org.opencdmp.query.WebhookDeliveryQuery;
import org.opencdmp.query.WebhookSubscriptionQuery;
import org.opencdmp.query.lookup.WebhookDeliveryLookup;
import org.opencdmp.query.lookup.WebhookSubscriptionLookup;
import org.opencdmp.service.webhook.WebhookService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/webhook-subscription")
@Hidden
public class WebhookSubscriptionController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(WebhookSubscriptionController.class));

    private final BuilderFactory builderFactory;
    private final AuditService auditService;
    private final WebhookService webhookService;
    private final CensorFactory censorFactory;
    private final QueryFactory queryFactory;
    private final MessageSource messageSource;

    public WebhookSubscriptionController(
            BuilderFactory builderFactory,
            AuditService auditService,
            WebhookService webhookService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.webhookService = webhookService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    public QueryResult<WebhookSubscription> query(@RequestBody WebhookSubscriptionLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", WebhookSubscription.class.getSimpleName());

        this.censorFactory.censor(WebhookSubscriptionCensor.class).censor(lookup.getProject());

        WebhookSubscriptionQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<WebhookSubscriptionEntity> data = query.collectAs(lookup.getProject());
        List<WebhookSubscription> models = this.builderFactory.builder(WebhookSubscriptionBuilder.class).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.WebhookSubscription_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    public WebhookSubscription get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + WebhookSubscription.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(WebhookSubscriptionCensor.class).censor(fieldSet);

        WebhookSubscriptionQuery query = this.queryFactory.query(WebhookSubscriptionQuery.class).authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        WebhookSubscription model = this.builderFactory.builder(WebhookSubscriptionBuilder.class).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, WebhookSubscription.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.WebhookSubscription_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("event-types")
    public List<WebhookEventType> eventTypes() {
        return Arrays.stream(WebhookEventType.values()).filter(x -> !WebhookEventType.Ping.equals(x)).toList();
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = WebhookSubscriptionPersist.WebhookSubscriptionPersistValidator.ValidatorName, argumentName = "model")
    public WebhookSubscription persist(@RequestBody WebhookSubscriptionPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + WebhookSubscription.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

        WebhookSubscription persisted = this.webhookService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.WebhookSubscription_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("deleting" + WebhookSubscription.class.getSimpleName()).And("id", id));

        this.webhookService.deleteAndSave(id);

        this.auditService.track(AuditableAction.WebhookSubscription_Delete, "id", id);
    }

    @PostMapping("{id}/ping")
    @Transactional
    public void ping(@PathVariable("id") UUID id) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("pinging webhook subscription").And("id", id));

        this.webhookService.sendPing(id);

        this.auditService.track(AuditableAction.WebhookSubscription_Ping, "id", id);
    }

    @PostMapping("delivery/query")
    public QueryResult<WebhookDelivery> queryDeliveries(@RequestBody WebhookDeliveryLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", WebhookDelivery.class.getSimpleName());

        this.censorFactory.censor(WebhookDeliveryCensor.class).censor(lookup.getProject());

        WebhookDeliveryQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<WebhookDeliveryEntity> data = query.collectAs(lookup.getProject());
        List<WebhookDelivery> models = this.builderFactory.builder(WebhookDeliveryBuilder.class).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.WebhookDelivery_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @PostMapping("delivery/{id}/redeliver")
    @Transactional
    public void redeliver(@PathVariable("id") UUID id) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("redelivering webhook delivery").And("id", id));

        this.webhookService.redeliver(id);

        this.auditService.track(AuditableAction.WebhookDelivery_Redeliver, "id", id);
    }
}
