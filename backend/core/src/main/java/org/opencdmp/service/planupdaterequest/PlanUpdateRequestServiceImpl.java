package org.opencdmp.service.planupdaterequest;

import tools.jackson.core.JacksonException;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanUpdateRequestActionType;
import org.opencdmp.commons.enums.PlanUpdateRequestStatus;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.builder.commonmodels.dto.plan.PlanCommonModelBuilder;
import org.opencdmp.model.builder.planupdaterequest.PlanUpdateRequestBuilder;
import org.opencdmp.model.deleter.PlanUpdateRequestDeleter;
import org.opencdmp.model.persist.PlanSuggestion;
import org.opencdmp.model.persist.PlanUpdateRequestPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planupdaterequest.PlanUpdateRequest;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.PlanUpdateRequestQuery;
import org.opencdmp.query.PlanUserQuery;
import org.opencdmp.query.UserQuery;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.plan.PlanServiceProperties;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

import static org.opencdmp.authorization.AuthorizationFlags.AllExceptPublic;

@Service
public class PlanUpdateRequestServiceImpl implements PlanUpdateRequestService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanUpdateRequestServiceImpl.class));

    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;

    private final AuthorizationService authorizationService;
    private final ConventionService conventionService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final JsonHandlingService jsonHandlingService;
    private final FileTransformerService fileTransformerService;
    private final PlanServiceProperties planServiceProperties;
    private final QueryFactory queryFactory;
    private final NotificationProperties notificationProperties;
    private final TenantScopeFactory tenantScopeFactory;
    private final NotifyIntegrationEventHandler notifyIntegrationEventHandler;

    public PlanUpdateRequestServiceImpl(BuilderFactory builderFactory, DeleterFactory deleterFactory, AuthorizationService authorizationService, ConventionService conventionService, TenantEntityManagerFactory tenantEntityManagerFactory, MessageSource messageSource, ErrorThesaurusProperties errors, JsonHandlingService jsonHandlingService, FileTransformerService fileTransformerService, PlanServiceProperties planServiceProperties, QueryFactory queryFactory, NotificationProperties notificationProperties, TenantScopeFactory tenantScopeFactory, NotifyIntegrationEventHandler notifyIntegrationEventHandler) {
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;
        this.authorizationService = authorizationService;
        this.conventionService = conventionService;
        this.jsonHandlingService = jsonHandlingService;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.messageSource = messageSource;
        this.errors = errors;
        this.fileTransformerService = fileTransformerService;
        this.planServiceProperties = planServiceProperties;
        this.queryFactory = queryFactory;
        this.notificationProperties = notificationProperties;
        this.tenantScopeFactory = tenantScopeFactory;
        this.notifyIntegrationEventHandler = notifyIntegrationEventHandler;
    }

    @Override
    public PlanUpdateRequest persist(PlanUpdateRequestPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, JacksonException {
        logger.debug(new MapLogEntry("persisting plan update request").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPlanUpdateRequest, Permission.DeferredAffiliation);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanUpdateRequestEntity data;
        if (isUpdate) {
            data = this.tenantEntityManagerFactory.getInstance().find(PlanUpdateRequestEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanUpdateRequest.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
            if (!data.getActionType().equals(model.getActionType())) throw new MyValidationException("action type can not be update");
            if (!data.getSubmitterId().equals(model.getSubmitterId())) throw new MyValidationException("submitter can not be update");
            if (!data.getData().equals(model.getData())) throw new MyValidationException("data can not be update");
        } else {

            data = new PlanUpdateRequestEntity();
            data.setId(UUID.randomUUID());
            data.setRequestAt(Instant.now());
            data.setPlanId(model.getPlanId());
            data.setSubmitterId(model.getSubmitterId());
            data.setActionType(model.getActionType());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
            data.setSourceCode(model.getSourceCode());
            data.setData(this.jsonHandlingService.toJson(model.getData()));
        }

        data.setUpdatedAt(Instant.now());
        data.setStatus(model.getStatus());
        if (data.getStatus().equals(PlanUpdateRequestStatus.Accepted)) data.setApprovedAt(Instant.now());

        if (isUpdate) this.tenantEntityManagerFactory.getInstance().merge(data);
        else this.tenantEntityManagerFactory.getInstance().persist(data);

        this.tenantEntityManagerFactory.getInstance().flush();

        if (!isUpdate) this.sendNotification(data);

        return this.builderFactory.builder(PlanUpdateRequestBuilder.class).build(BaseFieldSet.build(fields, PlanUpdateRequest._id), data);
    }

    private void sendNotification(PlanUpdateRequestEntity data) throws InvalidApplicationException {
        if (data.getActionType().equals(PlanUpdateRequestActionType.CreatePlan)) {
            UserEntity user = this.queryFactory.query(UserQuery.class).disableTracking().ids(data.getSubmitterId()).isActive(IsActive.Active).first();
            if (user == null) return;
            this.createNotificationEvent(data, user.getName(), null);

        } else if (data.getActionType().equals(PlanUpdateRequestActionType.UpdatePlan)) {
            PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(data.getPlanId()).isActive(IsActive.Active).first();
            if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            List<PlanUserEntity> planUserEntities = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(data.getPlanId()).userRoles(PlanUserRole.Owner).isActives(IsActive.Active).collect();
            if (this.conventionService.isListNullOrEmpty(planUserEntities)) return;

            List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUserEntities.stream().map(PlanUserEntity::getUserId).distinct().toList()).isActive(IsActive.Active).collect();
            if (users != null) {
                for (UserEntity user: users) {
                    this.createNotificationEvent(data, user.getName(), planEntity.getLabel());
                }
            }

        }
    }

    private void createNotificationEvent(PlanUpdateRequestEntity entity, String recipient, String planLabel) throws InvalidApplicationException {
        NotifyIntegrationEvent notifyIntegrationEvent = new NotifyIntegrationEvent();
        notifyIntegrationEvent.setUserId(entity.getSubmitterId());

        if (entity.getActionType().equals(PlanUpdateRequestActionType.None)) return;
        else if (entity.getActionType().equals(PlanUpdateRequestActionType.CreatePlan)) notifyIntegrationEvent.setNotificationType(this.notificationProperties.getPlanCreatedSuggestionType());
        else notifyIntegrationEvent.setNotificationType(this.notificationProperties.getPlanUpdatedSuggestionType());

        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, recipient));
        fieldInfoList.add(new FieldInfo("{sourceName}", DataType.String, entity.getSourceCode()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, entity.getId().toString()));

        if (notifyIntegrationEvent.getNotificationType().equals(this.notificationProperties.getPlanUpdatedSuggestionType())) {
            fieldInfoList.add(new FieldInfo("{planId}", DataType.String, entity.getPlanId().toString()));
            fieldInfoList.add(new FieldInfo("{name}", DataType.String, planLabel));
        }

        if(this.tenantScopeFactory.getInstance().getTenantCode() != null && !this.tenantScopeFactory.getInstance().getTenantCode().equals(this.tenantScopeFactory.getInstance().getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScopeFactory.getInstance().getTenantCode())));
        }
        data.setFields(fieldInfoList);
        notifyIntegrationEvent.setData(this.jsonHandlingService.toJsonSafe(data));

        this.notifyIntegrationEventHandler.handle(notifyIntegrationEvent);
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting plan update request: {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePlanUpdateRequest, Permission.DeferredAffiliation);

        this.deleterFactory.deleter(PlanUpdateRequestDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    @Override
    public PreprocessingPlanModel preprocessing(UUID id) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        PlanUpdateRequestEntity entity = this.queryFactory.query(PlanUpdateRequestQuery.class).authorize(AllExceptPublic).ids(id).isActive(IsActive.Active).first();

        if (entity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanUpdateRequestEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!entity.getStatus().equals(PlanUpdateRequestStatus.Pending)) throw new MyValidationException(this.errors.getPlanUpdateRequestIsCompleted().getCode(), this.errors.getPlanUpdateRequestIsCompleted().getMessage());
        if (!entity.getActionType().equals(PlanUpdateRequestActionType.CreatePlan)) throw new MyValidationException(this.errors.getPlanUpdateRequestInvalidActionType().getCode(), this.errors.getPlanUpdateRequestInvalidActionType().getMessage());

        return this.fileTransformerService.preprocessingPlan(entity.getData().getBytes(), this.planServiceProperties.getRdaTransformerId());
    }

    @Override
    public Plan buildPlan(PlanSuggestion suggestion) throws InvalidAlgorithmParameterException, JAXBException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeyException {

        PlanUpdateRequestEntity entity = this.queryFactory.query(PlanUpdateRequestQuery.class).authorize(AllExceptPublic).ids(suggestion.getPlanUpdateRequestId()).isActive(IsActive.Active).first();
        if (entity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{suggestion.getPlanUpdateRequestId(), PlanUpdateRequestEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanModel model = this.fileTransformerService.importPlan(entity.getData().getBytes(), suggestion, this.planServiceProperties.getRdaTransformerId());
        if (model == null) throw new MyNotFoundException("Plan Import Error");

        return this.builderFactory.builder(PlanCommonModelBuilder.class).build(model);
    }

}
