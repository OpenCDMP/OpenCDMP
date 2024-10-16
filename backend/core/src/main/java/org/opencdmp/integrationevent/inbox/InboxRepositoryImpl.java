package org.opencdmp.integrationevent.inbox;

import gr.cite.queueinbox.entity.QueueInbox;
import gr.cite.queueinbox.entity.QueueInboxStatus;
import gr.cite.queueinbox.repository.CandidateInfo;
import gr.cite.queueinbox.repository.InboxRepository;
import gr.cite.queueinbox.task.MessageOptions;
import gr.cite.rabbitmq.IntegrationEventMessageConstants;
import gr.cite.rabbitmq.consumer.InboxCreatorParams;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.*;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.fake.FakeRequestScope;
import org.opencdmp.data.QueueInboxEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.integrationevent.inbox.annotationentitycreated.AnnotationEntityCreatedIntegrationEventHandler;
import org.opencdmp.integrationevent.inbox.annotationstatusentitychanged.AnnotationStatusEntityChangedIntegrationEventHandler;
import org.opencdmp.query.QueueInboxQuery;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class InboxRepositoryImpl implements InboxRepository {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(InboxRepositoryImpl.class));

    protected final ApplicationContext applicationContext;
    private final InboxProperties inboxProperties;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    public InboxRepositoryImpl(
            ApplicationContext applicationContext,
            InboxProperties inboxProperties
    ) {
        this.applicationContext = applicationContext;
        this.inboxProperties = inboxProperties;
    }

    @Override
    public CandidateInfo candidate(Instant lastCandidateCreationTimestamp, MessageOptions options) {
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

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);

                transaction = entityManager.getTransaction();
                transaction.begin();

                QueueInboxEntity item = queryFactory.query(QueueInboxQuery.class)
                        .isActives(IsActive.Active)
                        .status(QueueInboxStatus.PENDING, QueueInboxStatus.ERROR)
                        .retryThreshold(options.getRetryThreashold())
                        .createdAfter(lastCandidateCreationTimestamp)
                        .ordering(new Ordering().addAscending(QueueInboxEntity._createdAt))
                        .first();

                if (item != null) {
                    QueueInboxStatus prevState = item.getStatus();
                    item.setStatus(QueueInboxStatus.PROCESSING);

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
                logger.debug("Concurrency exception getting queue inbox. Skipping: {} ", ex.getMessage());
                if (transaction != null)
                    transaction.rollback();
                candidate = null;
            } catch (Exception ex) {
                logger.error("Problem getting list of queue inbox. Skipping: {}", ex.getMessage(), ex);
                if (transaction != null)
                    transaction.rollback();
                candidate = null;
            } finally {
                if (entityManager != null) entityManager.close();
                if (tenantEntityManager != null) tenantEntityManager.reloadTenantFilters();
            }
        } catch (Exception ex) {
            logger.error("Problem getting list of queue inbox. Skipping: {}", ex.getMessage(), ex);
        }

        return candidate;
    }

    @Override
    public Boolean shouldOmit(CandidateInfo candidate, Function<QueueInbox, Boolean> shouldOmit) {
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
                QueueInboxEntity item = queryFactory.query(QueueInboxQuery.class).ids(candidate.getId()).first();

                if (item == null) {
                    logger.warn("Could not lookup queue inbox {} to process. Continuing...", candidate.getId());
                } else {
                    if (shouldOmit.apply(item)) {
                        item.setStatus(QueueInboxStatus.OMITTED);

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
    public boolean shouldWait(CandidateInfo candidate, Function<QueueInbox, Boolean> itIsTimeFunc) {
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
                QueueInboxEntity item = queryFactory.query(QueueInboxQuery.class).ids(candidate.getId()).first();

                if (item.getRetryCount() != null && item.getRetryCount() >= 1) {
                    Boolean itIsTime = itIsTimeFunc.apply(item);

                    if (!itIsTime) {
                        item.setStatus(candidate.getPreviousState());

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
    public QueueInbox create(InboxCreatorParams inboxCreatorParams) {
        EntityTransaction transaction = null;
        boolean success;
        QueueInboxEntity queueMessage = null;
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try{
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();

                tenantEntityManager.setEntityManager(entityManager);
                tenantEntityManager.disableTenantFilters();

                QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);

                Long messageCount = queryFactory.query(QueueInboxQuery.class).messageIds(UUID.fromString(inboxCreatorParams.getMessageId())).count();
                if (messageCount == null || messageCount == 0) {
                    queueMessage = this.createQueueInboxEntity(inboxCreatorParams);
                    transaction = entityManager.getTransaction();

                    transaction.begin();
                    entityManager.persist(queueMessage);
                    entityManager.flush();

                    transaction.commit();
                }

                success = true;
            } catch (Exception ex) {
                success = false;
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null) transaction.rollback();
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

    private QueueInboxEntity createQueueInboxEntity(InboxCreatorParams inboxCreatorParams) {
        QueueInboxEntity queueMessage = new QueueInboxEntity();
        queueMessage.setId(UUID.randomUUID());
        Object tenantId = inboxCreatorParams.getHeaders() != null ? inboxCreatorParams.getHeaders().getOrDefault(IntegrationEventMessageConstants.TENANT, null) : null;
        if (tenantId instanceof UUID) queueMessage.setTenantId((UUID) tenantId);
        else if (tenantId instanceof String) {
            try {
                queueMessage.setTenantId(UUID.fromString((String) tenantId));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        queueMessage.setExchange(this.inboxProperties.getExchange());
        queueMessage.setRoute(inboxCreatorParams.getRoutingKey());
        queueMessage.setQueue(inboxCreatorParams.getQueueName());
        queueMessage.setApplicationId(inboxCreatorParams.getAppId());
        queueMessage.setMessageId(UUID.fromString(inboxCreatorParams.getMessageId()));
        queueMessage.setMessage(inboxCreatorParams.getMessageBody());
        queueMessage.setIsActive(IsActive.Active);
        queueMessage.setStatus(QueueInboxStatus.PENDING);
        queueMessage.setRetryCount(0);
        queueMessage.setCreatedAt(Instant.now());

        return queueMessage;
    }

    @Override
    public Boolean emit(CandidateInfo candidateInfo) {
        EntityTransaction transaction = null;
        boolean success = false;
        QueueInboxEntity queueInboxMessage;
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            QueryFactory queryFactory = this.applicationContext.getBean(QueryFactory.class);
            queueInboxMessage = queryFactory.query(QueueInboxQuery.class).ids(candidateInfo.getId()).first();
        }
        if (queueInboxMessage == null) {
            logger.warn("Could not lookup queue inbox {} to process. Continuing...", candidateInfo.getId());
        } else {
            EventProcessingStatus status = this.emitQueueInboxEntity(queueInboxMessage);
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

                    switch (status) {
                        case Success: {
                            queueInboxMessage.setStatus(QueueInboxStatus.SUCCESSFUL);
                            break;
                        }
                        case Postponed: {
                            queueInboxMessage.setStatus(QueueInboxStatus.PARKED);
                            break;
                        }
                        case Error: {
                            queueInboxMessage.setStatus(QueueInboxStatus.ERROR);
                            queueInboxMessage.setRetryCount(queueInboxMessage.getRetryCount() != null ? queueInboxMessage.getRetryCount() + 1 : 0);
                            break;
                        }
                        case Discard:
                        default: {
                            queueInboxMessage.setStatus(QueueInboxStatus.DISCARD);
                            break;
                        }
                    }
                    success = status == EventProcessingStatus.Success;

                    entityManager.merge(queueInboxMessage);
                    entityManager.flush();

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

        }
        return success;
    }

    public EventProcessingStatus emitQueueInboxEntity(QueueInboxEntity queueInboxMessage) {
        EntityTransaction transaction = null;
        EventProcessingStatus status = EventProcessingStatus.Discard;
        try (FakeRequestScope ignored = new FakeRequestScope()) {
            TenantEntityManager tenantEntityManager = null;
            EntityManager entityManager = null;
            try{
                tenantEntityManager = this.applicationContext.getBean(TenantEntityManager.class);
                entityManager = this.entityManagerFactory.createEntityManager();

                tenantEntityManager.setEntityManager(entityManager);

                transaction = entityManager.getTransaction();

                transaction.begin();

                status = this.processMessage(queueInboxMessage);

                entityManager.flush();

                switch (status) {
                    case Error:  transaction.rollback();  break;
                    case Success:
                    case Postponed:
                    case Discard:
                    default: transaction.commit();  break;
                }
            } catch (Exception ex) {
                logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
                if (transaction != null)
                    transaction.rollback();
            } finally {
                if (entityManager != null) entityManager.close();
            }
        } catch (Exception ex) {
            logger.error("Problem executing purge. rolling back any db changes and marking error. Continuing...", ex);
        }
        return status;
    }

    private EventProcessingStatus processMessage(QueueInboxEntity queueInboxMessage) {
        IntegrationEventHandler handler = null;
        logger.debug("Processing message with routing key '{}'", queueInboxMessage.getRoute());
        if (this.routingKeyMatched(queueInboxMessage.getRoute(), this.inboxProperties.getAnnotationCreatedTopic()))
            handler = this.applicationContext.getBean(AnnotationEntityCreatedIntegrationEventHandler.class);
        else if (this.routingKeyMatched(queueInboxMessage.getRoute(), this.inboxProperties.getAnnotationStatusChangedTopic()))
            handler = this.applicationContext.getBean(AnnotationStatusEntityChangedIntegrationEventHandler.class);
        else {
            logger.error("No handler found for message routing key '{}'. Discarding.", queueInboxMessage.getRoute());
            handler = null;
        }

        if (handler == null)
            return EventProcessingStatus.Discard;

        IntegrationEventProperties properties = new IntegrationEventProperties();
        properties.setAppId(queueInboxMessage.getApplicationId());
        properties.setMessageId(queueInboxMessage.getMessageId().toString());
        properties.setTenantId(queueInboxMessage.getTenantId());

//        TrackedEvent event = this.jsonHandlingService.fromJsonSafe(TrackedEvent.class, queueInboxMessage.getMessage());
//		using (LogContext.PushProperty(this._logTrackingConfig.LogTrackingContextName, @event.TrackingContextTag))
//		{
        try {
            return handler.handle(properties, queueInboxMessage.getMessage());
        } catch (Exception ex) {
            logger.error("problem handling event from routing key {}. Setting nack and continuing...", queueInboxMessage.getRoute(), ex);
            return EventProcessingStatus.Error;
        }
//		}
    }

    private Boolean routingKeyMatched(String routingKey, List<String> topics) {
        if (topics == null || topics.isEmpty())
            return false;
        return topics.stream().anyMatch(x -> x.equals(routingKey));
    }
}
