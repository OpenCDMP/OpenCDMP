package org.opencdmp.service.updatecheck;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.InstallationEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.InstallationQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.management.InvalidApplicationException;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UpdateCheckTask implements Closeable, ApplicationListener<ApplicationReadyEvent> {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UpdateCheckTask.class));

    private final UpdateCheckProperties properties;
    private final ApplicationContext applicationContext;
    @PersistenceUnit
    private final EntityManagerFactory entityManagerFactory;
    private final ObjectProvider<BuildProperties> buildProperties;
    private ScheduledExecutorService scheduler;
    private HttpClient httpClient;
    private volatile UpdateFeed feed;
    private volatile Instant checkedAt;

    public UpdateCheckTask(UpdateCheckProperties properties, ApplicationContext applicationContext, EntityManagerFactory entityManagerFactory, ObjectProvider<BuildProperties> buildProperties) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.entityManagerFactory = entityManagerFactory;
        this.buildProperties = buildProperties;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        long intervalSeconds = this.properties.getIntervalSeconds();
        if (this.properties.getEnabled() && intervalSeconds > 0) {
            logger.info("update check runs every {} seconds", intervalSeconds);

            this.httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(Math.max(1, this.properties.getTimeoutSeconds())))
                    .build();
            this.scheduler = Executors.newScheduledThreadPool(1);
            this.scheduler.scheduleAtFixedRate(this::process, 60, intervalSeconds, TimeUnit.SECONDS);
        } else {
            this.scheduler = null;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.scheduler != null) this.scheduler.close();
    }

    public UpdateFeed getFeed() {
        return this.feed;
    }

    public Instant getCheckedAt() {
        return this.checkedAt;
    }

    protected void process() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            HttpRequest request = HttpRequest.newBuilder(URI.create(this.properties.getUrl()))
                    .timeout(Duration.ofSeconds(Math.max(1, this.properties.getTimeoutSeconds())))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(this.request()), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.warn("Update check got status {} from {}", response.statusCode(), this.properties.getUrl());
                return;
            }
            this.feed = mapper.readValue(response.body(), UpdateFeed.class);
            this.checkedAt = Instant.now();
        } catch (Exception ex) {
            logger.warn("Update check failed: {}", ex.getMessage());
        }
    }

    private UpdateCheckRequest request() throws InvalidApplicationException {
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManagerFactory tenantEntityManagerFactory = this.applicationContext.getBean(TenantEntityManagerFactory.class);
            try (EntityManager entityManager = this.entityManagerFactory.createEntityManager()) {
                tenantEntityManagerFactory.getInstance().setEntityManager(entityManager);
                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
                InstallationEntity installation = queryFactory.query(InstallationQuery.class).first();

                BuildProperties build = this.buildProperties.getIfAvailable();
                UpdateCheckRequest request = new UpdateCheckRequest();
                request.setInstallation(installation.getId());
                request.setVersion(build == null ? null : build.getVersion());
                if (!installation.getShareUsage()) return request;

                String installationUrl = this.properties.getInstallationUrl();
                request.setUrl(installationUrl == null || installationUrl.isBlank() ? null : installationUrl);
                // Counts span every organisation
                tenantEntityManagerFactory.getInstance().disableTenantFilters();
                try {
                    UpdateCheckUsage usage = new UpdateCheckUsage();
                    usage.setTenants(queryFactory.query(TenantQuery.class).isActive(IsActive.Active).count());
                    usage.setUsers(queryFactory.query(UserQuery.class).isActive(IsActive.Active).count());
                    usage.setPlans(queryFactory.query(PlanQuery.class).isActive(IsActive.Active).versionStatuses(PlanVersionStatus.Current).count());
                    usage.setDescriptions(queryFactory.query(DescriptionQuery.class).isActive(IsActive.Active)
                            .planSubQuery(queryFactory.query(PlanQuery.class).isActive(IsActive.Active).versionStatuses(PlanVersionStatus.Current)).count());
                    usage.setJavaVersion(System.getProperty("java.version"));
                    usage.setDatabaseVersion(entityManager.unwrap(Session.class).doReturningWork(connection -> connection.getMetaData().getDatabaseProductVersion()));
                    request.setUsage(usage);
                } finally {
                    tenantEntityManagerFactory.getInstance().reloadTenantFilters();
                }
                return request;
            }
        }
    }
}
