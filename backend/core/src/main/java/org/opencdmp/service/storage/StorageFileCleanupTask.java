package org.opencdmp.service.storage;

import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.StorageFile;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.query.TenantQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class StorageFileCleanupTask  implements Closeable, ApplicationListener<ApplicationReadyEvent> {
	private static class CandidateInfo
	{
		private UUID id;
		private Instant createdAt;

		public UUID getId() {
			return this.id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public Instant getCreatedAt() {
			return this.createdAt;
		}

		public void setCreatedAt(Instant createdAt) {
			this.createdAt = createdAt;
		}
	}

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StorageFileCleanupTask.class));
	private final StorageFileCleanupProperties _config;
	private final ApplicationContext applicationContext;
	@PersistenceUnit
	private final EntityManagerFactory entityManagerFactory;
	private ScheduledExecutorService scheduler;

	public StorageFileCleanupTask(
			StorageFileCleanupProperties config,
			ApplicationContext applicationContext, EntityManagerFactory entityManagerFactory) {
		this._config = config;
		this.applicationContext = applicationContext;


		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
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
			Instant lastCandidateCreationTimestamp = null;
			while (true) {

				CandidateInfo candidate = this.candidate(lastCandidateCreationTimestamp);
				if (candidate == null) break;
				lastCandidateCreationTimestamp = candidate.getCreatedAt();

				logger.debug("Clean up file: {}", candidate.getId());
				
				boolean successfullyProcessed = this.processStorageFile(candidate.getId());
				if (!successfullyProcessed) {
					logger.error("Problem processing file cleanups. {}", candidate.getId());
				}
			}
		} catch (Exception ex) {
			logger.error("Problem processing file cleanups. Breaking for next interval", ex);
		}
	}

	private boolean processStorageFile(UUID fileId) {
		EntityTransaction transaction = null;
		boolean success = false;
		try (FakeRequestScope fakeRequestScope = new FakeRequestScope()) {
			TenantEntityManager tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
			try (EntityManager entityManager = this.entityManagerFactory.createEntityManager()) {
				tenantEntityManager.setEntityManager(entityManager);
				tenantEntityManager.disableTenantFilters();
				
				
				transaction = entityManager.getTransaction();
				transaction.begin();

				QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
				StorageFileEntity item = queryFactory.query(StorageFileQuery.class).ids(fileId).isPurged(false).first();
				success = true;
				
				if (item != null) {
					TenantScope tenantScope = this.applicationContext.getBean(TenantScope.class);
					try {
						if (item.getTenantId() != null) {
							TenantEntity tenant = queryFactory.query(TenantQuery.class).ids(item.getTenantId()).first();
							tenantScope.setTempTenant(tenantEntityManager, tenant.getId(), tenant.getCode());
						} else {
							tenantScope.setTempTenant(tenantEntityManager, null, tenantScope.getDefaultTenantCode());
						}
						tenantEntityManager.reloadTenantFilters();
						StorageFileService storageFileService = this.applicationContext.getBean(StorageFileService.class);
						storageFileService.purgeSafe(fileId);
					} finally {
						tenantScope.removeTempTenant(tenantEntityManager);
					}
				}

				transaction.commit();
			} catch (OptimisticLockException ex) {
				// we get this if/when someone else already modified the notifications. We want to essentially ignore this, and keep working
				logger.debug("Concurrency exception getting file. Skipping: {} ", ex.getMessage());
				if (transaction != null) transaction.rollback();
				success = false;
			} catch (Exception ex) {
				logger.error("Problem getting list of file. Skipping: {}", ex.getMessage(), ex);
				if (transaction != null) transaction.rollback();
				success = false;
			} finally {
				tenantEntityManager.reloadTenantFilters();
			}
			
		} catch (Exception ex) {
			logger.error("Problem getting list of file. Skipping: {}", ex.getMessage(), ex);
		}

		return success;
	}

	private CandidateInfo candidate(Instant lastCandidateCreationTimestamp) {
		EntityTransaction transaction = null;
		CandidateInfo candidate = null;
		try (FakeRequestScope fakeRequestScope = new FakeRequestScope()) {
			TenantEntityManager tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
			try (EntityManager entityManager = this.entityManagerFactory.createEntityManager()) {
				tenantEntityManager.setEntityManager(entityManager);
				tenantEntityManager.disableTenantFilters();

				transaction = entityManager.getTransaction();
				transaction.begin();

				QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);

				StorageFileQuery query = queryFactory.query(StorageFileQuery.class)
						.canPurge(true)
						.isPurged(false)
						.createdAfter(lastCandidateCreationTimestamp);
				query.setOrder(new Ordering().addAscending(StorageFile._createdAt));
				StorageFileEntity item = query.first();

				if (item != null) {
					entityManager.flush();

					candidate = new CandidateInfo();
					candidate.setId(item.getId());
					candidate.setCreatedAt(item.getCreatedAt());
				}

				transaction.commit();
			} catch (OptimisticLockException ex) {
				// we get this if/when someone else already modified the notifications. We want to essentially ignore this, and keep working
				logger.debug("Concurrency exception getting file. Skipping: {} ", ex.getMessage());
				if (transaction != null) transaction.rollback();
				candidate = null;
			} catch (Exception ex) {
				logger.error("Problem getting list of file. Skipping: {}", ex.getMessage(), ex);
				if (transaction != null) transaction.rollback();
				candidate = null;
			} finally {
				tenantEntityManager.reloadTenantFilters();
			}
		} catch (Exception ex) {
			logger.error("Problem getting list of file. Skipping: {}", ex.getMessage(), ex);
		}

		return candidate;
	}
	
}
