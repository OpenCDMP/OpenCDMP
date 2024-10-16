package org.opencdmp.integrationevent.inbox.annotationentitycreated;

import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorProperties;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.annotation.AnnotationEntityType;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.inbox.EventProcessingStatus;
import org.opencdmp.integrationevent.inbox.InboxPrincipal;
import org.opencdmp.integrationevent.inbox.IntegrationEventProperties;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.*;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnnotationEntityCreatedIntegrationEventHandlerImpl implements AnnotationEntityCreatedIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(AnnotationEntityCreatedIntegrationEventHandlerImpl.class));

    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final NotificationProperties notificationProperties;
    private final TenantScope tenantScope;
    private final NotifyIntegrationEventHandler notifyIntegrationEventHandler;
    private final CurrentPrincipalResolver currentPrincipalResolver;
    private final ClaimExtractorProperties claimExtractorProperties;
    private final MessageSource messageSource;
    private final UserScope userScope;
    private final ErrorThesaurusProperties errors;
    private final TenantEntityManager tenantEntityManager;
    private final ValidatorFactory validatorFactory;
    private final AuditService auditService;

    public AnnotationEntityCreatedIntegrationEventHandlerImpl(QueryFactory queryFactory, JsonHandlingService jsonHandlingService, NotificationProperties notificationProperties, TenantScope tenantScope, NotifyIntegrationEventHandler notifyIntegrationEventHandler, CurrentPrincipalResolver currentPrincipalResolver, ClaimExtractorProperties claimExtractorProperties, MessageSource messageSource, UserScope userScope, ErrorThesaurusProperties errors, TenantEntityManager tenantEntityManager, ValidatorFactory validatorFactory, AuditService auditService) {
	    this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.notificationProperties = notificationProperties;
        this.tenantScope = tenantScope;
        this.notifyIntegrationEventHandler = notifyIntegrationEventHandler;
        this.currentPrincipalResolver = currentPrincipalResolver;
        this.claimExtractorProperties = claimExtractorProperties;
        this.messageSource = messageSource;
        this.userScope = userScope;
        this.errors = errors;
        this.tenantEntityManager = tenantEntityManager;
        this.validatorFactory = validatorFactory;
        this.auditService = auditService;
    }

    @Override
    public EventProcessingStatus handle(IntegrationEventProperties properties, String message) throws InvalidApplicationException {
        AnnotationEntityCreatedIntegrationEvent event = this.jsonHandlingService.fromJsonSafe(AnnotationEntityCreatedIntegrationEvent.class, message);
        if (event == null)
            return EventProcessingStatus.Error;

        logger.debug("Handling {}", AnnotationEntityCreatedIntegrationEvent.class.getSimpleName());
        this.validatorFactory.validator(AnnotationEntityCreatedIntegrationEvent.AnnotationEntityCreatedIntegrationEventValidator.class).validateForce(event);

        EventProcessingStatus status = EventProcessingStatus.Success;
        try {

            if (this.tenantScope.isMultitenant()) {
                if (properties.getTenantId() != null) {
                    TenantEntity tenant = this.queryFactory.query(TenantQuery.class).disableTracking().ids(properties.getTenantId()).firstAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
                    if (tenant == null) {
                        logger.error("missing tenant from event message");
                        return EventProcessingStatus.Error;
                    }
                    this.tenantScope.setTempTenant(this.tenantEntityManager, properties.getTenantId(), tenant.getCode());
                } else if (this.tenantScope.supportExpansionTenant()) {
                    this.tenantScope.setTempTenant(this.tenantEntityManager, null, this.tenantScope.getDefaultTenantCode());
                } else {
                    logger.error("missing tenant from event message");
                    return EventProcessingStatus.Error;
                }
            }

	        this.currentPrincipalResolver.push(InboxPrincipal.build(properties, this.claimExtractorProperties));
            this.sendNotification(event);
	        this.auditService.track(AuditableAction.Annotation_Created_Notify, Map.ofEntries(
                    new AbstractMap.SimpleEntry<String, Object>("model", event)
            ));

        } catch (Exception ex) {
            status = EventProcessingStatus.Error;
            logger.error("Problem getting list of queue outbox. Skipping: {}", ex.getMessage(), ex);
        } finally {
	        this.currentPrincipalResolver.pop();
            try {
	            this.tenantScope.removeTempTenant(this.tenantEntityManager);
                this.tenantEntityManager.reloadTenantFilters();
            } catch (InvalidApplicationException e) {
            }
        }

        return status;
    }

    private void sendNotification(AnnotationEntityCreatedIntegrationEvent event) throws InvalidApplicationException {

        List<PlanUserEntity> existingUsers = new ArrayList<>();
        DescriptionEntity descriptionEntity = null;
        PlanEntity planEntity = null;
        if (event.getEntityType().equals(AnnotationEntityType.Description)){
            descriptionEntity = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(event.getEntityId()).first();

            if (descriptionEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{event.getEntityId(), Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            existingUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking()
                    .planIds(descriptionEntity.getPlanId())
                    .isActives(IsActive.Active)
                    .collect();
        } else {
            planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(event.getEntityId()).first();

            if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{event.getEntityId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            existingUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking()
                    .planIds(planEntity.getId())
                    .isActives(IsActive.Active)
                    .collect();
        }

        if (existingUsers == null || existingUsers.size() <= 1){
            return;
        }

        UserEntity sender = this.queryFactory.query(UserQuery.class).disableTracking().ids(event.getSubjectId()).first();

        if (sender == null) throw new MyApplicationException("Sender user not found");

        List<UUID> existingUSerIDs = existingUsers.stream()
                .map(PlanUserEntity::getUserId)
                .filter(planUserId -> !planUserId.equals(event.getSubjectId()))
                .distinct().toList();
        for (UUID planUserId : existingUSerIDs) {
            UserEntity user = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUserId).first();
            if (user == null || user.getIsActive().equals(IsActive.Inactive))
                throw new MyValidationException(this.errors.getPlanInactiveUser().getCode(), this.errors.getPlanInactiveUser().getMessage());
            this.createAnnotationNotificationEvent(user, descriptionEntity, planEntity, sender.getName(), event);
        }

    }

    private void createAnnotationNotificationEvent(UserEntity user, DescriptionEntity description, PlanEntity plan, String reasonName, AnnotationEntityCreatedIntegrationEvent event) throws InvalidApplicationException, InvalidApplicationException {
        NotifyIntegrationEvent notifyIntegrationEvent = new NotifyIntegrationEvent();
        notifyIntegrationEvent.setUserId(user.getId());

        if (plan != null && description == null) notifyIntegrationEvent.setNotificationType(this.notificationProperties.getPlanAnnotationCreatedType());
        else notifyIntegrationEvent.setNotificationType(this.notificationProperties.getDescriptionAnnotationCreatedType());

        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, reasonName));

        if (plan != null && description == null) {
            fieldInfoList.add(new FieldInfo("{name}", DataType.String, plan.getLabel()));
            fieldInfoList.add(new FieldInfo("{id}", DataType.String, plan.getId().toString()));
        } else {
            fieldInfoList.add(new FieldInfo("{name}", DataType.String, description.getLabel()));
            fieldInfoList.add(new FieldInfo("{id}", DataType.String, description.getId().toString()));
        }

        String anchorUrl = "f/"+event.getAnchor()+"/annotation";
        fieldInfoList.add(new FieldInfo("{anchor}", DataType.String, anchorUrl));
        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        notifyIntegrationEvent.setData(this.jsonHandlingService.toJsonSafe(data));

        this.notifyIntegrationEventHandler.handle(notifyIntegrationEvent);
    }
}
