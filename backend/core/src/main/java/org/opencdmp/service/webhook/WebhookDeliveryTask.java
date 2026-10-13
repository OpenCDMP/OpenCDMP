package org.opencdmp.service.webhook;

import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.Paging;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.WebhookDeliveryStatus;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.WebhookDeliveryEntity;
import org.opencdmp.data.WebhookSubscriptionEntity;
import org.opencdmp.query.WebhookDeliveryQuery;
import org.opencdmp.query.WebhookSubscriptionQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Drains the webhook delivery outbox: claims due Pending rows cross-tenant, signs
 * the stored payload with the subscription secret (HMAC-SHA256) and POSTs it,
 * retrying with exponential backoff plus jitter until success or the attempt cap,
 * after which the row dead-letters as Failed (redeliverable from the admin UI).
 * Enable on a single instance in clustered deployments — rows are not locked
 * across processes.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class WebhookDeliveryTask implements Closeable, ApplicationListener<ApplicationReadyEvent> {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(WebhookDeliveryTask.class));

    private static final long MAX_BACKOFF_SECONDS = 6 * 60 * 60;

    private final WebhookProperties properties;
    private final ApplicationContext applicationContext;
    @PersistenceUnit
    private final EntityManagerFactory entityManagerFactory;
    private ScheduledExecutorService scheduler;
    private HttpClient httpClient;

    public WebhookDeliveryTask(WebhookProperties properties, ApplicationContext applicationContext, EntityManagerFactory entityManagerFactory) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        if (!this.properties.getEnabled() || this.properties.getTask() == null || !this.properties.getTask().getEnable() || this.properties.getTask().getIntervalSeconds() <= 0) {
            this.scheduler = null;
            return;
        }

        long intervalSeconds = this.properties.getTask().getIntervalSeconds();
        logger.info("webhook delivery task runs every {} seconds", intervalSeconds);

        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(Math.max(1, this.properties.getTimeoutSeconds())))
                .build();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduler.scheduleAtFixedRate(this::process, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws IOException {
        if (this.scheduler != null) this.scheduler.close();
    }

    protected void process() {
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManagerFactory tenantEntityManagerFactory = null;
            EntityManager entityManager = null;

            try {
                tenantEntityManagerFactory = this.applicationContext.getBean(TenantEntityManagerFactory.class);
                entityManager = this.entityManagerFactory.createEntityManager();

                tenantEntityManagerFactory.getInstance().setEntityManager(entityManager);
                tenantEntityManagerFactory.getInstance().disableTenantFilters();

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);

                WebhookDeliveryQuery query = queryFactory.query(WebhookDeliveryQuery.class)
                        .statuses(WebhookDeliveryStatus.Pending)
                        .isActive(List.of(IsActive.Active))
                        .dueBefore(Instant.now())
                        .ordering(new Ordering().addAscending(WebhookDeliveryEntity._createdAt));
                query.setPage(new Paging(0, Math.max(1, this.properties.getTask().getBatchSize())));

                List<WebhookDeliveryEntity> deliveries = query.collect();
                if (deliveries == null || deliveries.isEmpty()) return;

                for (WebhookDeliveryEntity delivery : deliveries) {
                    this.deliverOne(entityManager, queryFactory, delivery);
                }
            } catch (Exception ex) {
                logger.error("Problem processing webhook deliveries. Breaking for next interval", ex);
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManagerFactory != null) tenantEntityManagerFactory.getInstance().reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem executing webhook delivery task.", ex);
        }
    }

    private void deliverOne(EntityManager entityManager, QueryFactory queryFactory, WebhookDeliveryEntity delivery) {
        EntityTransaction transaction = null;
        try {
            WebhookSubscriptionEntity subscription = queryFactory.query(WebhookSubscriptionQuery.class)
                    .disableTracking()
                    .ids(delivery.getSubscriptionId())
                    .first();

            transaction = entityManager.getTransaction();
            transaction.begin();

            if (subscription == null || !IsActive.Active.equals(subscription.getIsActive()) || !Boolean.TRUE.equals(subscription.getEnabled())) {
                this.markFailed(delivery, null, "subscription is deleted or disabled");
            } else {
                this.attempt(delivery, subscription);
            }

            delivery.setUpdatedAt(Instant.now());
            entityManager.merge(delivery);
            entityManager.flush();
            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null) transaction.rollback();
            logger.error("Problem processing webhook delivery " + delivery.getId() + ". Skipping", ex);
        }
    }

    private void attempt(WebhookDeliveryEntity delivery, WebhookSubscriptionEntity subscription) {
        delivery.setAttempts((delivery.getAttempts() != null ? delivery.getAttempts() : 0) + 1);
        try {
            URI uri = URI.create(subscription.getUrl());
            if (this.properties.getDenyPrivateAddresses() && isPrivateTarget(uri)) {
                this.markFailed(delivery, null, "target resolves to a private or local address");
                return;
            }

            String payload = delivery.getPayload() != null ? delivery.getPayload() : "";
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String signature = sign(subscription.getSecret(), timestamp + "." + payload);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(Math.max(1, this.properties.getTimeoutSeconds())))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "OpenCDMP-Webhook")
                    .header("X-OpenCDMP-Event", delivery.getEventType() != null ? delivery.getEventType().name() : "")
                    .header("X-OpenCDMP-Delivery", delivery.getId().toString())
                    .header("X-OpenCDMP-Timestamp", timestamp)
                    .header("X-OpenCDMP-Signature", "sha256=" + signature)
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<Void> response = this.httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            delivery.setLastHttpStatus(response.statusCode());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                delivery.setStatus(WebhookDeliveryStatus.Delivered);
                delivery.setDeliveredAt(Instant.now());
                delivery.setLastError(null);
                delivery.setNextRetryAt(null);
            } else {
                this.retryOrFail(delivery, "unexpected http status " + response.statusCode());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            this.retryOrFail(delivery, "delivery interrupted");
        } catch (Exception ex) {
            delivery.setLastHttpStatus(null);
            this.retryOrFail(delivery, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        }
    }

    private void retryOrFail(WebhookDeliveryEntity delivery, String error) {
        if (delivery.getAttempts() >= Math.max(1, this.properties.getMaxAttempts())) {
            this.markFailed(delivery, delivery.getLastHttpStatus(), error);
            return;
        }
        long backoffSeconds = Math.min(MAX_BACKOFF_SECONDS, 60L * (1L << Math.min(20, delivery.getAttempts() - 1)));
        long jitter = ThreadLocalRandom.current().nextLong(backoffSeconds / 5 + 1);
        delivery.setNextRetryAt(Instant.now().plusSeconds(backoffSeconds + jitter));
        delivery.setLastError(truncate(error));
    }

    private void markFailed(WebhookDeliveryEntity delivery, Integer httpStatus, String error) {
        delivery.setStatus(WebhookDeliveryStatus.Failed);
        delivery.setLastHttpStatus(httpStatus);
        delivery.setLastError(truncate(error));
        delivery.setNextRetryAt(null);
    }

    private static String truncate(String value) {
        if (value == null) return null;
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private static boolean isPrivateTarget(URI uri) {
        try {
            String host = uri.getHost();
            if (host == null) return true;
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isAnyLocalAddress()) return true;
            }
            return false;
        } catch (Exception ex) {
            // Unresolvable targets are treated as unsafe, so the delivery is marked Failed.
            return true;
        }
    }

    private static String sign(String secret, String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(raw.length * 2);
        for (byte b : raw) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
