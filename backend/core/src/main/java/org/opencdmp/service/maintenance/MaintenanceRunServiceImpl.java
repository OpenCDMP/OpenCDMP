package org.opencdmp.service.maintenance;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.MaintenanceRunStatus;
import org.opencdmp.commons.enums.MaintenanceTask;
import org.opencdmp.commons.enums.MaintenanceTaskRunStatus;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.data.MaintenanceRunEntity;
import org.opencdmp.data.MaintenanceTaskRunEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.maintenance.MaintenanceRun;
import org.opencdmp.model.maintenance.MaintenanceTaskRun;
import org.opencdmp.model.persist.MaintenanceRunPersist;
import org.opencdmp.query.MaintenanceRunQuery;
import org.opencdmp.query.MaintenanceTaskRunQuery;
import org.opencdmp.service.elastic.ElasticService;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

// Runs the maintenance tasks on a background thread, one after another, as the admin who started the run. Progress
// is kept in the database, so any replica can report it and a run outlives the browser tab that started it.
@Service
public class MaintenanceRunServiceImpl implements MaintenanceRunService, Closeable {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(MaintenanceRunServiceImpl.class));

    private static final long HEARTBEAT_SECONDS = 30;
    // A run whose replica stopped beating this long ago died with it.
    private static final Duration STALE_AFTER = Duration.ofMinutes(5);
    private static final int ERROR_MAX_LENGTH = 4000;

    private final EntityManagerFactory entityManagerFactory;
    private final ApplicationContext applicationContext;
    private final AuthorizationService authorizationService;
    private final CurrentPrincipalResolverFactory currentPrincipalResolverFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final UserScopeFactory userScopeFactory;
    private final ErrorThesaurusProperties errors;
    private final MessageSource messageSource;
    private final QueryFactory queryFactory;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    private final ExecutorService runner = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor();

    public MaintenanceRunServiceImpl(EntityManagerFactory entityManagerFactory, ApplicationContext applicationContext, AuthorizationService authorizationService, CurrentPrincipalResolverFactory currentPrincipalResolverFactory, TenantScopeFactory tenantScopeFactory, UserScopeFactory userScopeFactory, ErrorThesaurusProperties errors, MessageSource messageSource, QueryFactory queryFactory, TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.applicationContext = applicationContext;
        this.authorizationService = authorizationService;
        this.currentPrincipalResolverFactory = currentPrincipalResolverFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.userScopeFactory = userScopeFactory;
        this.errors = errors;
        this.messageSource = messageSource;
        this.queryFactory = queryFactory;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    public void close() {
        this.runner.shutdownNow();
        this.heartbeat.shutdownNow();
    }

    @Override
    public MaintenanceRun start(MaintenanceRunPersist model) throws InvalidApplicationException {
        logger.debug("start maintenance run with tasks {}", model.getTasks());
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);
        List<MaintenanceTask> tasks = model.getTasks().stream().distinct().sorted().toList();
        if (tasks.contains(MaintenanceTask.ElasticIndex)) this.authorizationService.authorizeForce(Permission.ManageElastic);

        MyPrincipal principal = this.currentPrincipalResolverFactory.getInstance().currentPrincipal();
        UUID userId = this.userScopeFactory.getInstance().getUserIdSafe();
        TenantScope tenantScope = this.tenantScopeFactory.getInstance();
        UUID tenant = tenantScope.isSet() ? tenantScope.getTenant() : null;
        String tenantCode = tenantScope.isSet() ? tenantScope.getTenantCode() : null;

        UUID runId = UUID.randomUUID();
        this.inQueryTransaction(entityManager -> {
            this.interruptStaleRuns(entityManager);
            long running = this.queryFactory.query(MaintenanceRunQuery.class).statuses(MaintenanceRunStatus.Running).count();
            if (running > 0) throw new MyValidationException(this.errors.getMaintenanceRunInProgress().getCode(), this.errors.getMaintenanceRunInProgress().getMessage());

            Instant now = Instant.now();
            MaintenanceRunEntity run = new MaintenanceRunEntity();
            run.setId(runId);
            run.setPreset(model.getPreset());
            run.setStatus(MaintenanceRunStatus.Running);
            run.setStopOnFailure(model.getStopOnFailure());
            run.setStopRequested(false);
            run.setCreatedBy(userId);
            run.setCreatedAt(now);
            run.setHeartbeatAt(now);
            entityManager.persist(run);

            for (int i = 0; i < tasks.size(); i++) {
                MaintenanceTaskRunEntity taskRun = new MaintenanceTaskRunEntity();
                taskRun.setId(UUID.randomUUID());
                taskRun.setRun(runId);
                taskRun.setTask(tasks.get(i).getValue());
                taskRun.setOrdinal(i);
                taskRun.setStatus(MaintenanceTaskRunStatus.Queued);
                entityManager.persist(taskRun);
            }
            return null;
        });

        this.runner.submit(() -> this.execute(runId, principal, userId, tenant, tenantCode));
        return this.inQueryTransaction(entityManager -> this.toModel(entityManager.find(MaintenanceRunEntity.class, runId)));
    }

    @Override
    public MaintenanceRun latest() {
        logger.debug("latest maintenance run");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        return this.inQueryTransaction(entityManager -> {
            this.interruptStaleRuns(entityManager);
            MaintenanceRunQuery query = this.queryFactory.query(MaintenanceRunQuery.class);
            query.setOrder(new Ordering().addDescending(MaintenanceRunEntity._createdAt));
            MaintenanceRunEntity run = query.first();
            return run == null ? null : this.toModel(run);
        });
    }

    @Override
    public MaintenanceRun stop(UUID id) {
        logger.debug("stop maintenance run {}", id);
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        return this.inQueryTransaction(entityManager -> {
            MaintenanceRunEntity run = entityManager.find(MaintenanceRunEntity.class, id);
            if (run == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, MaintenanceRun.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (run.getStatus() == MaintenanceRunStatus.Running) run.setStopRequested(true);
            return this.toModel(run);
        });
    }

    @Override
    public List<MaintenanceTaskRun> lastTaskRuns() {
        logger.debug("last run of each maintenance task");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        return this.inQueryTransaction(entityManager -> {
            MaintenanceTaskRunQuery query = this.queryFactory.query(MaintenanceTaskRunQuery.class).disableTracking().statuses(MaintenanceTaskRunStatus.Succeeded, MaintenanceTaskRunStatus.Failed);
            query.setOrder(new Ordering().addAscending(MaintenanceTaskRunEntity._task).addDescending(MaintenanceTaskRunEntity._finishedAt));
            Set<String> seen = new HashSet<>();
            List<MaintenanceTaskRun> models = new ArrayList<>();
            for (MaintenanceTaskRunEntity taskRun : query.collect()) {
                // ordered newest first per task, so the first finished row of each task is its last run
                if (taskRun.getFinishedAt() == null || !seen.add(taskRun.getTask())) continue;
                MaintenanceTask task = MaintenanceTask.of(taskRun.getTask());
                // a task that no longer exists
                if (task == null) continue;
                MaintenanceTaskRun model = new MaintenanceTaskRun();
                model.setTask(task);
                model.setStatus(taskRun.getStatus());
                model.setStartedAt(taskRun.getStartedAt());
                model.setFinishedAt(taskRun.getFinishedAt());
                model.setError(taskRun.getError());
                models.add(model);
            }
            return models;
        });
    }

    private void execute(UUID runId, MyPrincipal principal, UUID userId, UUID tenant, String tenantCode) {
        ScheduledFuture<?> beat = this.heartbeat.scheduleAtFixedRate(() -> this.beat(runId), HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS);
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            // the task code checks permissions and reads the user and tenant as it does inside the request
            CurrentPrincipalResolver principalResolver = this.currentPrincipalResolverFactory.getInstance();
            principalResolver.push(principal);
            try {
                if (userId != null) this.userScopeFactory.getInstance().setUserId(userId);
                if (tenantCode != null) this.tenantScopeFactory.getInstance().setTenant(tenant, tenantCode);
                this.runTasks(runId);
            } finally {
                principalResolver.pop();
            }
        } catch (Exception ex) {
            logger.error("Maintenance run {} stopped unexpectedly", runId, ex);
            this.finishRun(runId, MaintenanceRunStatus.Failed);
        } finally {
            beat.cancel(false);
        }
    }

    private void runTasks(UUID runId) {
        List<MaintenanceTaskRunEntity> taskRuns = this.inQueryTransaction(entityManager -> this.taskRunsOf(runId));
        boolean stopOnFailure = this.inTransaction(entityManager -> entityManager.find(MaintenanceRunEntity.class, runId).getStopOnFailure());

        boolean anyFailed = false;
        for (int i = 0; i < taskRuns.size(); i++) {
            MaintenanceTaskRunEntity taskRun = taskRuns.get(i);
            if (this.inTransaction(entityManager -> entityManager.find(MaintenanceRunEntity.class, runId).getStopRequested())) {
                this.skip(taskRuns.subList(i, taskRuns.size()));
                this.finishRun(runId, MaintenanceRunStatus.Stopped);
                return;
            }

            this.updateTask(taskRun.getId(), t -> {
                t.setStatus(MaintenanceTaskRunStatus.Running);
                t.setStartedAt(Instant.now());
            });
            try {
                this.runTask(MaintenanceTask.of(taskRun.getTask()));
                this.updateTask(taskRun.getId(), t -> {
                    t.setStatus(MaintenanceTaskRunStatus.Succeeded);
                    t.setFinishedAt(Instant.now());
                });
                logger.info("Maintenance run {}: task {} succeeded", runId, taskRun.getTask());
            } catch (Exception ex) {
                logger.error("Maintenance run {}: task {} failed", runId, taskRun.getTask(), ex);
                anyFailed = true;
                String error = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                this.updateTask(taskRun.getId(), t -> {
                    t.setStatus(MaintenanceTaskRunStatus.Failed);
                    t.setFinishedAt(Instant.now());
                    t.setError(error.length() > ERROR_MAX_LENGTH ? error.substring(0, ERROR_MAX_LENGTH) : error);
                });
                if (stopOnFailure) {
                    this.skip(taskRuns.subList(i + 1, taskRuns.size()));
                    this.finishRun(runId, MaintenanceRunStatus.Failed);
                    return;
                }
            }
        }
        this.finishRun(runId, anyFailed ? MaintenanceRunStatus.Failed : MaintenanceRunStatus.Succeeded);
    }

    // Each task gets its own transaction, as each maintenance endpoint does through @Transactional.
    private void runTask(MaintenanceTask task) throws Exception {
        TenantEntityManagerFactory tenantEntityManagerFactory = this.applicationContext.getBean(TenantEntityManagerFactory.class);
        EntityManager entityManager = null;
        EntityTransaction transaction = null;
        try {
            entityManager = this.entityManagerFactory.createEntityManager();
            tenantEntityManagerFactory.getInstance().setEntityManager(entityManager);
            tenantEntityManagerFactory.getInstance().reloadTenantFilters();

            transaction = entityManager.getTransaction();
            transaction.begin();
            this.dispatch(task);
            entityManager.flush();
            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw ex;
        } finally {
            if (entityManager != null) entityManager.close();
        }
    }

    private void dispatch(MaintenanceTask task) throws Exception {
        MaintenanceService maintenanceService = this.applicationContext.getBean(MaintenanceService.class);
        switch (task) {
            case ElasticIndex -> {
                ElasticService elasticService = this.applicationContext.getBean(ElasticService.class);
                elasticService.resetPlanIndex();
                elasticService.resetDescriptionIndex();
            }
            case TenantTouch -> maintenanceService.sendTenantTouchEvents();
            case UserTouch -> maintenanceService.sendUserTouchEvents();
            case DescriptionTouch -> maintenanceService.sendDescriptionTouchEvents();
            case PlanTouch -> maintenanceService.sendPlanTouchEvents();
            case PlanAccounting -> maintenanceService.sendPlanAccountingEntriesEvents();
            case DescriptionAccounting -> maintenanceService.sendDescriptionAccountingEntriesEvents();
            case PlanBlueprintAccounting -> maintenanceService.sendBlueprintAccountingEntriesEvents();
            case PlanBlueprintTypeAccounting -> maintenanceService.sendBlueprintTypeAccountingEntriesEvents();
            case DescriptionTemplateAccounting -> maintenanceService.sendDescriptionTemplateAccountingEntriesEvents();
            case DescriptionTemplateTypeAccounting -> maintenanceService.sendDescriptionTemplateTypeAccountingEntriesEvents();
            case PrefillingSourceAccounting -> maintenanceService.sendPrefillingSourceAccountingEntriesEvents();
            case ReferenceAccounting -> maintenanceService.sendReferenceAccountingEntriesEvents();
            case ReferenceTypeAccounting -> maintenanceService.sendReferenceTypeAccountingEntriesEvents();
            case PlanStatusAccounting -> maintenanceService.sendPlanStatusAccountingEntriesEvents();
            case DescriptionStatusAccounting -> maintenanceService.sendDescriptionStatusAccountingEntriesEvents();
            case LanguageAccounting -> maintenanceService.sendLanguageAccountingEntriesEvents();
            case UserAccounting -> maintenanceService.sendUserAccountingEntriesEvents();
            case PlanEvaluationAccounting -> maintenanceService.sendEvaluationPlanAccountingEntriesEvents();
            case DescriptionEvaluationAccounting -> maintenanceService.sendEvaluationDescriptionAccountingEntriesEvents();
            case IndicatorCreate -> maintenanceService.sendIndicatorCreateEntryEvents();
            case IndicatorReset -> maintenanceService.sendIndicatorResetEntryEvents();
            case IndicatorAccess -> maintenanceService.sendIndicatorAccessEntryEvents();
            case DashboardTouch -> maintenanceService.sendDashboardTouchedEntryEvents();
            case IndicatorPointPlan -> maintenanceService.sendIndicatorPointPlanEntryEvents();
            case IndicatorPointDescription -> maintenanceService.sendIndicatorPointDescriptionEntryEvents();
            case IndicatorPointReference -> maintenanceService.sendIndicatorPointReferenceEntryEvents();
            case IndicatorPointUser -> maintenanceService.sendIndicatorPointUserEntryEvents();
            case IndicatorPointPlanBlueprint -> maintenanceService.sendIndicatorPointPlanBlueprintEntryEvents();
            case IndicatorPointDescriptionTemplate -> maintenanceService.sendIndicatorPointDescriptionTemplateEntryEvents();
            case IndicatorPointTenant -> maintenanceService.sendIndicatorPointTenantEntryEvents();
            case IndicatorPointPlanBlueprintType -> maintenanceService.sendIndicatorPointPlanBlueprintTypeEntryEvents();
            case IndicatorPointDescriptionTemplateType -> maintenanceService.sendIndicatorPointDescriptionTemplateTypeEntryEvents();
        }
    }

    private void beat(UUID runId) {
        try {
            // a plain find: the heartbeat thread has no request scope, so the queries can't run here
            this.inTransaction(entityManager -> {
                MaintenanceRunEntity run = entityManager.find(MaintenanceRunEntity.class, runId);
                if (run != null && run.getStatus() == MaintenanceRunStatus.Running) run.setHeartbeatAt(Instant.now());
                return null;
            });
        } catch (Exception ex) {
            logger.warn("Could not record the heartbeat of maintenance run {}", runId, ex);
        }
    }

    private void skip(List<MaintenanceTaskRunEntity> taskRuns) {
        for (MaintenanceTaskRunEntity taskRun : taskRuns) this.updateTask(taskRun.getId(), t -> t.setStatus(MaintenanceTaskRunStatus.Skipped));
    }

    private void finishRun(UUID runId, MaintenanceRunStatus status) {
        try {
            this.inTransaction(entityManager -> {
                MaintenanceRunEntity run = entityManager.find(MaintenanceRunEntity.class, runId);
                if (run != null && run.getStatus() == MaintenanceRunStatus.Running) {
                    run.setStatus(status);
                    run.setFinishedAt(Instant.now());
                }
                return null;
            });
        } catch (Exception ex) {
            logger.error("Could not record the end of maintenance run {}", runId, ex);
        }
    }

    private void interruptStaleRuns(EntityManager entityManager) {
        List<MaintenanceRunEntity> stale = this.queryFactory.query(MaintenanceRunQuery.class).statuses(MaintenanceRunStatus.Running).heartbeatBefore(Instant.now().minus(STALE_AFTER)).collect();
        if (stale.isEmpty()) return;

        Instant now = Instant.now();
        for (MaintenanceRunEntity run : stale) {
            logger.warn("Maintenance run {} stopped sending heartbeats; marking it interrupted", run.getId());
            run.setStatus(MaintenanceRunStatus.Interrupted);
            run.setFinishedAt(now);
        }
        List<MaintenanceTaskRunEntity> unfinished = this.queryFactory.query(MaintenanceTaskRunQuery.class)
                .runs(stale.stream().map(MaintenanceRunEntity::getId).toList())
                .statuses(MaintenanceTaskRunStatus.Running, MaintenanceTaskRunStatus.Queued)
                .collect();
        for (MaintenanceTaskRunEntity taskRun : unfinished) {
            if (taskRun.getStatus() == MaintenanceTaskRunStatus.Running) {
                taskRun.setStatus(MaintenanceTaskRunStatus.Failed);
                taskRun.setFinishedAt(now);
            } else {
                taskRun.setStatus(MaintenanceTaskRunStatus.Skipped);
            }
        }
    }

    private List<MaintenanceTaskRunEntity> taskRunsOf(UUID runId) {
        MaintenanceTaskRunQuery query = this.queryFactory.query(MaintenanceTaskRunQuery.class).runs(runId);
        query.setOrder(new Ordering().addAscending(MaintenanceTaskRunEntity._ordinal));
        return query.collect();
    }

    private void updateTask(UUID taskRunId, Consumer<MaintenanceTaskRunEntity> change) {
        this.inTransaction(entityManager -> {
            change.accept(entityManager.find(MaintenanceTaskRunEntity.class, taskRunId));
            return null;
        });
    }

    // Progress is written in its own short transactions, so it is visible while a task's transaction is still open.
    private <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            T result = work.apply(entityManager);
            entityManager.flush();
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            if (transaction.isActive()) transaction.rollback();
            throw ex;
        } finally {
            entityManager.close();
        }
    }

    // MaintenanceRunQuery reads through the request's TenantEntityManager, so point it at this transaction to see its own writes.
    private <T> T inQueryTransaction(Function<EntityManager, T> work) {
        TenantEntityManager tenantEntityManager = this.tenantEntityManagerFactory.getInstance();
        EntityManager requestEntityManager = tenantEntityManager.getEntityManager();
        return this.inTransaction(entityManager -> {
            tenantEntityManager.setEntityManager(entityManager);
            try {
                return work.apply(entityManager);
            } finally {
                tenantEntityManager.setEntityManager(requestEntityManager);
            }
        });
    }

    private MaintenanceRun toModel(MaintenanceRunEntity entity) {
        MaintenanceRun model = new MaintenanceRun();
        model.setId(entity.getId());
        model.setPreset(entity.getPreset());
        model.setStatus(entity.getStatus());
        model.setStopOnFailure(entity.getStopOnFailure());
        model.setStopRequested(entity.getStopRequested());
        model.setCreatedAt(entity.getCreatedAt());
        model.setFinishedAt(entity.getFinishedAt());

        List<MaintenanceTaskRun> tasks = new ArrayList<>();
        for (MaintenanceTaskRunEntity taskRun : this.taskRunsOf(entity.getId())) {
            MaintenanceTask task = MaintenanceTask.of(taskRun.getTask());
            if (task == null) continue;
            MaintenanceTaskRun taskModel = new MaintenanceTaskRun();
            taskModel.setTask(task);
            taskModel.setStatus(taskRun.getStatus());
            taskModel.setStartedAt(taskRun.getStartedAt());
            taskModel.setFinishedAt(taskRun.getFinishedAt());
            taskModel.setError(taskRun.getError());
            tasks.add(taskModel);
        }
        model.setTasks(tasks);
        return model;
    }
}
