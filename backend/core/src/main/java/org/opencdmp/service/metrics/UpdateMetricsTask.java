package org.opencdmp.service.metrics;

import gr.cite.tools.logging.LoggerService;
import io.prometheus.client.Gauge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.TenantEntityManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UpdateMetricsTask implements Closeable, ApplicationListener<ApplicationReadyEvent> {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UpdateMetricsTask.class));
	private final UpdateMetricsTaskProperties _config;
	private final ApplicationContext applicationContext;

	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;
	private ScheduledExecutorService scheduler;
	private Map<String, Gauge> gauges;

	public UpdateMetricsTask(
			UpdateMetricsTaskProperties config,
			ApplicationContext applicationContext) {
		this._config = config;
		this.applicationContext = applicationContext;
		this.gauges = null;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		long intervalSeconds = this._config .getIntervalSeconds();
		if (this._config .getEnable() && intervalSeconds > 0) {
			logger.info("File clean up run in {} seconds", intervalSeconds);

			this.scheduler = Executors.newScheduledThreadPool(1);
			this.scheduler.scheduleAtFixedRate(this::process, 10, intervalSeconds, TimeUnit.SECONDS);
		} else {
			this.scheduler = null;
		}
	}
	
	@Override
	public void close() throws IOException {
		if (this.scheduler != null) this.scheduler.close();
	}

	protected void process() {
		if (!this._config.getEnable()) return;
		try {
			this.ensureGauges();
			this.calculate();
		} catch (Exception ex) {
			logger.error("Problem processing file cleanups. Breaking for next interval", ex);
		}
	}

	private void ensureGauges() {
		if (this.gauges != null) return;

		try (FakeRequestScope ignored = new FakeRequestScope()) {
			TenantEntityManager tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
			try (EntityManager entityManager = this.entityManagerFactory.createEntityManager()) {
				tenantEntityManager.setEntityManager(entityManager);
				tenantEntityManager.disableTenantFilters();

				MetricsService metricsService = this.applicationContext.getBean(MetricsService.class);
				this.gauges = metricsService.gaugesBuild();
			} finally {
				tenantEntityManager.reloadTenantFilters();
			}
		} catch (Exception ex) {
			logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
		}
	}

	private void calculate() {
		try (FakeRequestScope ignored = new FakeRequestScope()) {
			TenantEntityManager tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
			try (EntityManager entityManager = this.entityManagerFactory.createEntityManager()) {
				tenantEntityManager.setEntityManager(entityManager);
				tenantEntityManager.disableTenantFilters();
				MetricsService metricsService = this.applicationContext.getBean(MetricsService.class);

				metricsService.calculate(this.gauges);
			} finally {
				tenantEntityManager.reloadTenantFilters();
			}
		} catch (Exception ex) {
			logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
		}
	}

}
