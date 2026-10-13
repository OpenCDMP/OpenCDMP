package org.opencdmp.service.kpi;

import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorElasticEventHandler;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KpiBootstrapTask implements Closeable, ApplicationListener<ApplicationReadyEvent> {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(KpiBootstrapTask.class));

    private final KpiProperties kpiProperties;
    private final ApplicationContext applicationContext;
    @PersistenceUnit
    private final EntityManagerFactory entityManagerFactory;
    private ScheduledExecutorService scheduler;

    public KpiBootstrapTask(KpiProperties kpiProperties, ApplicationContext applicationContext, EntityManagerFactory entityManagerFactory) {
        this.kpiProperties = kpiProperties;
        this.applicationContext = applicationContext;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        if (this.kpiProperties.getTask() == null || !this.kpiProperties.getTask().getEnable() || !this.kpiProperties.getTask().getBootstrapEnable()) {
            this.scheduler = null;
            return;
        }

        long delaySeconds = Math.max(0, this.kpiProperties.getTask().getBootstrapDelaySeconds());
        logger.info("kpi bootstrap runs in {} seconds", delaySeconds);

        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduler.schedule(this::process, delaySeconds, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws IOException {
        if (this.scheduler != null) this.scheduler.close();
    }

    protected void process() {
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManagerFactory tenantEntityManagerFactory = this.applicationContext.getBean(TenantEntityManagerFactory.class);
            KpiService kpiService = this.applicationContext.getBean(KpiService.class);
            IndicatorElasticEventHandler indicatorElasticEventHandler = this.applicationContext.getBean(IndicatorElasticEventHandler.class);

            // Both calls end up persisting queue outbox rows through the tenant entity manager. On a scheduler
            // thread there is no request-bound EntityManager or transaction (the maintenance endpoints that do the
            // same work get both from @Transactional on the controller), so - like the other background tasks -
            // open our own EntityManager and transaction and hand it to the tenant entity manager; otherwise every
            // event is rejected with a TransactionRequiredException and quietly dropped.
            EntityManager entityManager = null;
            EntityTransaction transaction = null;
            try {
                entityManager = this.entityManagerFactory.createEntityManager();
                tenantEntityManagerFactory.getInstance().setEntityManager(entityManager);
                tenantEntityManagerFactory.getInstance().disableTenantFilters();

                transaction = entityManager.getTransaction();
                transaction.begin();

                indicatorElasticEventHandler.handle(kpiService.createIndicator());
                kpiService.sendDashboardTouchedEvents();

                transaction.commit();
                logger.info("kpi bootstrap completed: indicator definition and dashboard definitions published");
            } catch (Exception ex) {
                if (transaction != null && transaction.isActive()) transaction.rollback();
                throw ex;
            } finally {
                if (entityManager != null) entityManager.close();
                tenantEntityManagerFactory.getInstance().reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem executing kpi bootstrap task. Indicator and dashboard definitions can still be published through the maintenance actions", ex);
        }
    }
}
