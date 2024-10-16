package org.opencdmp.integrationevent.outbox;

import gr.cite.queueoutbox.entity.QueueOutbox;
import gr.cite.queueoutbox.entity.QueueOutboxNotifyStatus;
import gr.cite.queueoutbox.repository.CandidateInfo;
import gr.cite.queueoutbox.repository.OutboxRepository;
import gr.cite.queueoutbox.task.MessageOptions;
import gr.cite.rabbitmq.IntegrationEvent;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.*;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.QueueOutboxEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.query.QueueOutboxQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OutboxRepositoryImpl implements OutboxRepository {

    private static final Logger log = LoggerFactory.getLogger(OutboxRepositoryImpl.class);
    protected final ApplicationContext applicationContext;

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(OutboxRepositoryImpl.class));

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    private final OutboxProperties outboxProperties; 
    private final JsonHandlingService jsonHandlingService;

    public OutboxRepositoryImpl(
		    ApplicationContext applicationContext, OutboxProperties outboxProperties
    ) {
        this.applicationContext = applicationContext;
	    this.jsonHandlingService = this.applicationContext.getBean(JsonHandlingService.class);
        this.outboxProperties = outboxProperties;
    }

    @Override
    public CandidateInfo candidate(Instant lastCandidateCreationTimestamp, MessageOptions messageOptions, Function<QueueOutbox, Boolean> onConfirmTimeout) {
        EntityTransaction transaction = null;
        CandidateInfo candidate = null;
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try{
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();
                
                tenantEntityManager.setEntityManager(entityManager);
                tenantEntityManager.disableTenantFilters();

                transaction = entityManager.getTransaction();
                transaction.begin();

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
                QueueOutboxEntity item = queryFactory.query(QueueOutboxQuery.class)
                        .isActives(IsActive.Active)
                        .notifyStatus(QueueOutboxNotifyStatus.PENDING, QueueOutboxNotifyStatus.WAITING_CONFIRMATION, QueueOutboxNotifyStatus.ERROR)
                        .retryThreshold(messageOptions.getRetryThreashold())
                        .confirmTimeout(messageOptions.getConfirmTimeoutSeconds())
                        .createdAfter(lastCandidateCreationTimestamp)
                        .ordering(new Ordering().addAscending(QueueOutboxEntity._createdAt))
                        .first();

                if (item != null) {
                    boolean confirmTimeout = onConfirmTimeout.apply(item);

                    QueueOutboxNotifyStatus prevState = item.getNotifyStatus();
                    item.setNotifyStatus(QueueOutboxNotifyStatus.PROCESSING);

                    entityManager.merge(item);
                    entityManager.flush();

                    candidate = new CandidateInfo();
                    candidate.setId(item.getId());
                    candidate.setCreatedAt(item.getCreatedAt());
                    candidate.setPreviousState(prevState);
                }

                transaction.commit();
            } catch (OptimisticLockException ex) {
                // we get this if/when someone else already modified the notifications. We want to essentially ignore this, and keep working
                logger.debug("Concurrency exception getting queue outbox. Skipping: {} ", ex.getMessage());
                if (transaction != null)
                    transaction.rollback();
                candidate = null;
            } catch (Exception ex) {
                logger.error("Problem getting list of queue outbox. Skipping: {}", ex.getMessage(), ex);
                if (transaction != null)
                    transaction.rollback();
                candidate = null;
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem getting list of queue outbox. Skipping: {}", ex.getMessage(), ex);
        }

        return candidate;
    }

    @Override
    public Boolean shouldOmit(CandidateInfo candidate, Function<QueueOutbox, Boolean> shouldOmit) {
        EntityTransaction transaction = null;
        boolean success = false;

        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try{
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();
                tenantEntityManager.setEntityManager(entityManager);
                tenantEntityManager.disableTenantFilters();

                transaction = entityManager.getTransaction();

                transaction.begin();

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
                QueueOutboxEntity item = queryFactory.query(QueueOutboxQuery.class).ids(candidate.getId()).first();

                if (item == null) {
                    logger.warn("Could not lookup queue outbox {} to process. Continuing...", candidate.getId());
                } else {
                    if (shouldOmit.apply(item)) {
                        item.setNotifyStatus(QueueOutboxNotifyStatus.OMITTED);

                        entityManager.merge(item);
                        entityManager.flush();
                        success = true;
                    }
                }

                transaction.commit();
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null)
                    transaction.rollback();
                success = false;
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
        }
        return success;
    }

    @Override
    public Boolean shouldWait(CandidateInfo candidate, Function<QueueOutbox, Boolean> itIsTimeFunc) {
        EntityTransaction transaction = null;
        boolean success = false;

        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try{
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();
                tenantEntityManager.setEntityManager(entityManager);
                tenantEntityManager.disableTenantFilters();

                transaction = entityManager.getTransaction();

                transaction.begin();

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
                QueueOutboxEntity item = queryFactory.query(QueueOutboxQuery.class).ids(candidate.getId()).first();

                if (item.getRetryCount() != null && item.getRetryCount() >= 1) {
                    Boolean itIsTime = itIsTimeFunc.apply(item);

                    if (!itIsTime) {
                        item.setNotifyStatus(candidate.getPreviousState());

                        entityManager.merge(item);
                        entityManager.flush();
                        success = true;
                    }

                    success = !itIsTime;
                }
                transaction.commit();
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null)
                    transaction.rollback();
                success = false;
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
        }
        return success;
    }

    @Override
    public Boolean process(CandidateInfo candidateInfo, Boolean isAutoconfirmOnPublish, Function<QueueOutbox, Boolean> publish) {
        EntityTransaction transaction = null;
        Boolean success = false;

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

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
                QueueOutboxEntity item = queryFactory.query(QueueOutboxQuery.class).ids(candidateInfo.getId()).first();

                if (item == null) {
                    logger.warn("Could not lookup queue outbox {} to process. Continuing...", candidateInfo.getId());
                } else {

                    success = publish.apply(item);
                    if (success) {
                        if (isAutoconfirmOnPublish) {
                            item.setNotifyStatus(QueueOutboxNotifyStatus.CONFIRMED);
                            item.setConfirmedAt(Instant.now());
                        } else {
                            item.setNotifyStatus(QueueOutboxNotifyStatus.WAITING_CONFIRMATION);
                        }
                        item.setPublishedAt(Instant.now());
                    } else {
                        item.setNotifyStatus(QueueOutboxNotifyStatus.ERROR);
                        item.setRetryCount(item.getRetryCount() != null ? item.getRetryCount() + 1 : 0);
                        item.setPublishedAt(null);
                    }

                    entityManager.merge(item);
                    entityManager.flush();
                }

                transaction.commit();
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null)
                    transaction.rollback();
                success = false;
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
        }
        return success;
    }

    @Override
    public void handleConfirm(List<UUID> confirmedMessages) {
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try  {
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();
                tenantEntityManager.setEntityManager(entityManager);
                tenantEntityManager.disableTenantFilters();

                this.handleConfirmWithRetries(entityManager, confirmedMessages);
                
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
        }
    }

    private void handleConfirmWithRetries(EntityManager entityManager, List<UUID> confirmedMessages) throws InterruptedException {
        EntityTransaction transaction = null;
        for (int i = 0; i < this.outboxProperties.getHandleAckRetries() + 1; i++) {
            try {
                transaction = entityManager.getTransaction();
                transaction.begin();

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
                List<QueueOutboxEntity> queueOutboxMessages = queryFactory.query(QueueOutboxQuery.class).ids(confirmedMessages).collect();

                if (queueOutboxMessages == null) {
                    logger.warn("Could not lookup messages {} to process. Continuing...", confirmedMessages.stream().map(UUID::toString).collect(Collectors.joining(",")));
                } else {

                    for (QueueOutboxEntity queueOutboxMessage : queueOutboxMessages) {
                        queueOutboxMessage = queryFactory.query(QueueOutboxQuery.class).ids(queueOutboxMessage.getId()).first();
                        queueOutboxMessage.setNotifyStatus(QueueOutboxNotifyStatus.CONFIRMED);
                        queueOutboxMessage.setConfirmedAt(Instant.now());
                        entityManager.merge(queueOutboxMessage);
                        entityManager.flush();
                    }
                }

                transaction.commit();
                return;
            } catch (OptimisticLockException ex) {
                logger.warn("Problem handle ack {}. Rolling back any message emit db changes and marking error. Retrying...", confirmedMessages.stream().map(UUID::toString).collect(Collectors.joining(",")));
                if (transaction != null) transaction.rollback();
                Thread.sleep(this.outboxProperties.getHandleAckWaitInMilliSeconds());
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null) transaction.rollback();
                return;
            }
        }
    }

    @Override
    public void handleNack(List<UUID> nackedMessages) {
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try  {
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();
                tenantEntityManager.setEntityManager(entityManager);
                tenantEntityManager.disableTenantFilters();

                this.handleNackWithRetries(entityManager, nackedMessages);
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
        }
    }
    
    private void handleNackWithRetries(EntityManager entityManager, List<UUID> nackedMessages) throws InterruptedException {
        EntityTransaction transaction = null;
        for (int i = 0; i < this.outboxProperties.getHandleNackRetries() + 1; i++) {
            try {
                transaction = entityManager.getTransaction();
                transaction.begin();

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
                List<QueueOutboxEntity> queueOutboxMessages = queryFactory.query(QueueOutboxQuery.class).ids(nackedMessages).collect();

                if (queueOutboxMessages == null) {
                    logger.warn("Could not lookup messages {} to process. Continuing...", nackedMessages.stream().map(UUID::toString).collect(Collectors.joining(",")));
                } else {

                    for (QueueOutboxEntity queueOutboxMessage : queueOutboxMessages) {
                        queueOutboxMessage.setNotifyStatus(QueueOutboxNotifyStatus.ERROR);
                        queueOutboxMessage.setRetryCount(queueOutboxMessage.getRetryCount() != null ? queueOutboxMessage.getRetryCount() + 1 : 0);
                        entityManager.merge(queueOutboxMessage);
                    }

                    entityManager.flush();
                }

                transaction.commit();
                return;
            } catch (OptimisticLockException ex) {
                logger.warn("Problem handle nack {}. Rolling back any message emit db changes and marking error. Retrying...", nackedMessages.stream().map(UUID::toString).collect(Collectors.joining(",")));
                if (transaction != null) transaction.rollback();
                Thread.sleep(this.outboxProperties.getHandleNackWaitInMilliSeconds());
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null) transaction.rollback();
                return;
            }
        }
    }

    @Override
    public QueueOutbox create(IntegrationEvent item) {
        EntityTransaction transaction = null;
        QueueOutboxEntity queueMessage = null;
        boolean success;
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try  {
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();
                tenantEntityManager.setEntityManager(entityManager);
                tenantEntityManager.disableTenantFilters();

                queueMessage = this.mapEvent((OutboxIntegrationEvent) item);
                transaction = entityManager.getTransaction();

                transaction.begin();

                entityManager.persist(queueMessage);
                entityManager.flush();

                transaction.commit();
                success = true;
            } catch (Exception ex) {
                success = false;
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null)
                    transaction.rollback();
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            success = false;
            logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
        }
        return success ? queueMessage : null;
    }

    private QueueOutboxEntity mapEvent(OutboxIntegrationEvent event) {
        String routingKey;
        switch (event.getType()) {
            case OutboxIntegrationEvent.TENANT_REACTIVATE: {
                routingKey = this.outboxProperties.getTenantReactivationTopic();
                break;
            }
            case OutboxIntegrationEvent.TENANT_REMOVE: {
                routingKey = this.outboxProperties.getTenantRemovalTopic();
                break;
            }
            case OutboxIntegrationEvent.TENANT_TOUCH: {
                routingKey = this.outboxProperties.getTenantTouchTopic();
                break;
            }
            case OutboxIntegrationEvent.INDICATOR_POINT_ENTRY: {
                routingKey = this.outboxProperties.getIndicatorPointTopic();
                break;
            }
            case OutboxIntegrationEvent.INDICATOR_ENTRY: {
                routingKey = this.outboxProperties.getIndicatorTopic();
                break;
            }
            case OutboxIntegrationEvent.INDICATOR_RESET_ENTRY: {
                routingKey = this.outboxProperties.getIndicatorResetTopic();
                break;
            }
            case OutboxIntegrationEvent.INDICATOR_ACCESS_ENTRY: {
                routingKey = this.outboxProperties.getIndicatorAccessTopic();
                break;
            }
            case OutboxIntegrationEvent.TENANT_USER_INVITE: {
                routingKey = this.outboxProperties.getTenantUserInviteTopic();
                break;
            }
            case OutboxIntegrationEvent.USER_REMOVE: {
                routingKey = this.outboxProperties.getUserRemovalTopic();
                break;
            }
            case OutboxIntegrationEvent.USER_TOUCH: {
                routingKey = this.outboxProperties.getUserTouchTopic();
                break;
            }
            case OutboxIntegrationEvent.ANNOTATION_ENTITY_TOUCH: {
                routingKey = this.outboxProperties.getAnnotationEntitiesTouchTopic();
                break;
            }
            case OutboxIntegrationEvent.ANNOTATION_ENTITY_REMOVE: {
                routingKey = this.outboxProperties.getAnnotationEntitiesRemovalTopic();
                break;
            }
            case OutboxIntegrationEvent.FORGET_ME_COMPLETED: {
                routingKey = this.outboxProperties.getForgetMeCompletedTopic();
                break;
            }
            case OutboxIntegrationEvent.NOTIFY: {
                routingKey = this.outboxProperties.getNotifyTopic();
                break;
            }
            case OutboxIntegrationEvent.TENANT_DEFAULT_LOCALE_REMOVAL: {
                routingKey = this.outboxProperties.getTenantDefaultLocaleRemovalTopic();
                break;
            }
            case OutboxIntegrationEvent.TENANT_DEFAULT_LOCALE_TOUCHED: {
                routingKey = this.outboxProperties.getTenantDefaultLocaleTouchedTopic();
                break;
            }
            case OutboxIntegrationEvent.WHAT_YOU_KNOW_ABOUT_ME_COMPLETED: {
                routingKey = this.outboxProperties.getWhatYouKnowAboutMeCompletedTopic();
                break;
            }
            case OutboxIntegrationEvent.ACCOUNTING_ENTRY_CREATED: {
                routingKey = this.outboxProperties.getAccountingEntryCreatedTopic();
                break;
            }
            case OutboxIntegrationEvent.GENERATE_FILE: {
                routingKey = this.outboxProperties.getGenerateFileTopic();
                break;
            }
            default: {
                logger.error("unrecognized outgoing integration event {}. Skipping...", event.getType());
                return null;
            }
        }

        UUID correlationId = UUID.randomUUID();
        if (event.getEvent() != null)
            event.getEvent().setTrackingContextTag(correlationId.toString());
        event.setMessage(this.jsonHandlingService.toJsonSafe(event.getEvent()));
        //this._logTrackingService.Trace(correlationId.ToString(), $"Correlating current tracking context with new correlationId {correlationId}");

        QueueOutboxEntity queueMessage = new QueueOutboxEntity();
        queueMessage.setId(UUID.randomUUID());
        queueMessage.setTenantId(event.getTenantId());
        queueMessage.setExchange(this.outboxProperties.getExchange());
        queueMessage.setRoute(routingKey);
        queueMessage.setMessageId(event.getMessageId());
        queueMessage.setMessage(this.jsonHandlingService.toJsonSafe(event));
        queueMessage.setIsActive(IsActive.Active);
        queueMessage.setNotifyStatus(QueueOutboxNotifyStatus.PENDING);
        queueMessage.setRetryCount(0);
        queueMessage.setCreatedAt(Instant.now());
        queueMessage.setUpdatedAt(Instant.now());

        return queueMessage;
    }
}
