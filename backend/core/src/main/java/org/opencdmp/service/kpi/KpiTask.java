package org.opencdmp.service.kpi;

import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEventHandler;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KpiTask implements Closeable, ApplicationListener<ApplicationReadyEvent> {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(KpiTask.class));
	private final KpiProperties properties;
	private final ApplicationContext applicationContext;
	@PersistenceUnit
	private final EntityManagerFactory entityManagerFactory;
	private ScheduledExecutorService scheduler;
	private final KpiService kpiService;
	private final IndicatorResetEventHandler indicatorResetEventHandler;

	public KpiTask(
            KpiProperties properties, ApplicationContext applicationContext, EntityManagerFactory entityManagerFactory, KpiService kpiService, IndicatorResetEventHandler indicatorResetEventHandler) {
        this.properties = properties;
        this.applicationContext = applicationContext;

		this.entityManagerFactory = entityManagerFactory;
        this.kpiService = kpiService;
        this.indicatorResetEventHandler = indicatorResetEventHandler;
    }

	@Override
	public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
		long intervalSeconds = this.properties.getTask().getIntervalSeconds();
		if (this.properties.getTask().getEnable() && intervalSeconds > 0) {
			logger.info("Kpi indicator point tasks run in {} seconds", intervalSeconds);

			this.scheduler = Executors.newScheduledThreadPool(1);

			ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
			ZonedDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
			if(now.compareTo(nextRun) > 0)
				nextRun = nextRun.plusDays(1).plusSeconds(this.properties.getTask().getStartTimeSeconds());
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
		if (!this.properties.getTask().getEnable()) return;

		EntityTransaction transaction = null;
		try (FakeRequestScope ignored = new FakeRequestScope()) {
			TenantEntityManager tenantEntityManager = null;
			EntityManager entityManager = null;

			try {
				tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
				entityManager = this.entityManagerFactory.createEntityManager();

				tenantEntityManager.setEntityManager(entityManager);
				tenantEntityManager.disableTenantFilters();

				transaction = entityManager.getTransaction();
				transaction.begin();

				this.indicatorResetEventHandler.handle(this.kpiService.resetIndicator());
				this.kpiService.sendIndicatorPointPlanCountEntryEvents();
				this.kpiService.sendIndicatorPointDescriptionCountEntryEvents();
				this.kpiService.sendIndicatorPointUserCountEntryEvents();
				this.kpiService.sendIndicatorPointReferenceCountEntryEvents();
				this.kpiService.sendIndicatorPointPlanBlueprintCountEntryEvents();
				this.kpiService.sendIndicatorPointDescriptionTemplateCountEntryEvents();

				transaction.commit();
			} catch (Exception ex) {
				if (transaction != null)
					transaction.rollback();
				logger.error("Problem processing kpi indicator point tasks. Breaking for next interval", ex);
			} finally {
				if (entityManager != null) entityManager.close();
				if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
			}
		} catch (Exception ex) {
			if (transaction != null)
				transaction.rollback();
			logger.error("Problem executing kpi tak.", ex);
		}
	}
	
}
