package org.opencdmp.integrationevent;

import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.Paging;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.QueueInboxEntity;
import org.opencdmp.data.QueueOutboxEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.query.QueueInboxQuery;
import org.opencdmp.query.QueueOutboxQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CleanupTask implements Closeable, ApplicationListener<ApplicationReadyEvent> {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(CleanupTask.class));
	private final CleanupTaskProperties properties;
	private final ApplicationContext applicationContext;
	@PersistenceUnit
	private final EntityManagerFactory entityManagerFactory;
	private ScheduledExecutorService scheduler;
	private final JsonHandlingService jsonHandlingService;

	public CleanupTask(
            CleanupTaskProperties properties, ApplicationContext applicationContext, EntityManagerFactory entityManagerFactory, JsonHandlingService jsonHandlingService) {
        this.properties = properties;
        this.applicationContext = applicationContext;

		this.entityManagerFactory = entityManagerFactory;
        this.jsonHandlingService = jsonHandlingService;
    }

	@Override
	public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
		long intervalSeconds = this.properties.getIntervalSeconds();
		if (this.properties.getEnable() && intervalSeconds > 0) {
			logger.info("Kpi indicator point tasks run in {} seconds", intervalSeconds);

			this.scheduler = Executors.newScheduledThreadPool(1);

			ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
			ZonedDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
			if(now.compareTo(nextRun) > 0)
				nextRun = nextRun.plusDays(1).plusSeconds(this.properties.getStartTimeSeconds());
			Duration duration = Duration.between(now, nextRun);
			long initialDelay = duration.getSeconds();
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(this::process, initialDelay, intervalSeconds, TimeUnit.SECONDS);
		} else {
			this.scheduler = null;
		}
	}
	
	@Override
	public void close() throws IOException {
		if (this.scheduler != null) this.scheduler.close();
	}

	protected void process() {
		if (!this.properties.getEnable()) return;

		EntityTransaction transaction = null;
		try (FakeRequestScope ignored = new FakeRequestScope()) {
			TenantEntityManager tenantEntityManager = null;
			EntityManager entityManager = null;

			try {
				tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
				entityManager = this.entityManagerFactory.createEntityManager();

				tenantEntityManager.setEntityManager(entityManager);
				tenantEntityManager.disableTenantFilters();

				QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);

				List<QueueInboxEntity> queueInboxEntities;
				QueueInboxQuery queueInboxQuery = queryFactory.query(QueueInboxQuery.class).status(this.properties.getQueueInboxStatus());
				if (this.properties.getRetentionDays() != null) queueInboxQuery.createdBefore(Instant.now().minus(this.properties.getRetentionDays(), ChronoUnit.DAYS));
				queueInboxQuery.ordering(new Ordering().addAscending(QueueInboxEntity._createdAt));
				do {
					queueInboxQuery.setPage(new Paging(0, this.properties.getBatchSize()));
					queueInboxEntities = queueInboxQuery.collect();
					for (QueueInboxEntity entity : queueInboxEntities) {
						try {
							transaction = entityManager.getTransaction();
							transaction.begin();

							entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
							entityManager.flush();
							transaction.commit();
						} catch (Exception ex) {
							if (transaction != null)
								transaction.rollback();
							logger.error("Problem processing specific queue inbox row: " + this.jsonHandlingService.toJsonSafe(entity) , ex);
						}
					}
				} while (!queueInboxEntities.isEmpty());

				List<QueueOutboxEntity> queueOutboxEntities;
				QueueOutboxQuery queueOutboxQuery = queryFactory.query(QueueOutboxQuery.class).notifyStatus(this.properties.getQueueOutboxStatus());
				if (this.properties.getRetentionDays() != null) queueOutboxQuery.createdBefore(Instant.now().minus(this.properties.getRetentionDays(), ChronoUnit.DAYS));
				queueOutboxQuery.ordering(new Ordering().addAscending(QueueInboxEntity._createdAt));
				do {
					queueOutboxQuery.setPage(new Paging(0, this.properties.getBatchSize()));
					queueOutboxEntities = queueOutboxQuery.collect();
					for (QueueOutboxEntity entity : queueOutboxEntities) {
						try {
							transaction = entityManager.getTransaction();
							transaction.begin();

							entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
							entityManager.flush();
							transaction.commit();
						} catch (Exception ex) {
							if (transaction != null)
								transaction.rollback();
							logger.error("Problem processing specific queue outbox row: " + this.jsonHandlingService.toJsonSafe(entity) , ex);
						}
					}
				} while (!queueOutboxEntities.isEmpty());

			} catch (Exception ex) {
				if (transaction != null)
					transaction.rollback();
				logger.error("Problem processing queue inbox, outbox rows. Breaking for next interval", ex);
			} finally {
				if (entityManager != null) entityManager.close();
				if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
			}
		} catch (Exception ex) {
			logger.error("Problem executing cleanup tak.", ex);
		}
	}
	
}
