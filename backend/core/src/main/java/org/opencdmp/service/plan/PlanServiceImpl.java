package org.opencdmp.service.plan;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFailure;
import gr.cite.tools.validation.ValidatorFactory;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.plan.PlanBlueprintValueModel;
import org.opencdmp.commonmodels.models.plan.PlanContactModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.commonmodels.models.planblueprint.ExtraFieldModel;
import org.opencdmp.commonmodels.models.planblueprint.FieldModel;
import org.opencdmp.commonmodels.models.planblueprint.ReferenceTypeFieldModel;
import org.opencdmp.commonmodels.models.planblueprint.SectionModel;
import org.opencdmp.commonmodels.models.plandescriptiontemplate.PlanDescriptionTemplateModel;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.enums.notification.NotificationContactType;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.actionconfirmation.PlanInvitationEntity;
import org.opencdmp.commons.types.description.importexport.DescriptionImportExport;
import org.opencdmp.commons.types.notification.*;
import org.opencdmp.commons.types.plan.PlanBlueprintValueEntity;
import org.opencdmp.commons.types.plan.PlanContactEntity;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.commons.types.plan.importexport.*;
import org.opencdmp.commons.types.planblueprint.BlueprintDescriptionTemplateEntity;
import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.commons.types.planblueprint.ReferenceTypeFieldEntity;
import org.opencdmp.commons.types.planblueprint.SectionEntity;
import org.opencdmp.commons.types.planblueprint.importexport.BlueprintExtraFieldImportExport;
import org.opencdmp.commons.types.planblueprint.importexport.BlueprintReferenceTypeFieldImportExport;
import org.opencdmp.commons.types.planblueprint.importexport.BlueprintSectionImportExport;
import org.opencdmp.commons.types.planreference.PlanReferenceDataEntity;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.commons.types.reference.FieldEntity;
import org.opencdmp.commons.users.UsersProperties;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.EventBroker;
import org.opencdmp.event.PlanTouchedEvent;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.integrationevent.outbox.annotationentityremoval.AnnotationEntityRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.annotationentitytouch.AnnotationEntityTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.*;
import org.opencdmp.model.builder.PlanUserBuilder;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.deleter.*;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.file.FileEnvelope;
import org.opencdmp.model.persist.*;
import org.opencdmp.model.persist.actionconfirmation.PlanInvitationPersist;
import org.opencdmp.model.persist.planproperties.PlanBlueprintValuePersist;
import org.opencdmp.model.persist.planproperties.PlanContactPersist;
import org.opencdmp.model.persist.planproperties.PlanPropertiesPersist;
import org.opencdmp.model.persist.planreference.PlanReferenceDataPersist;
import org.opencdmp.model.persist.referencedefinition.DefinitionPersist;
import org.opencdmp.model.persist.referencedefinition.FieldPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planreference.PlanReferenceData;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.actionconfirmation.ActionConfirmationService;
import org.opencdmp.service.custompolicy.CustomPolicyService;
import org.opencdmp.service.description.DescriptionService;
import org.opencdmp.service.descriptiontemplate.DescriptionTemplateService;
import org.opencdmp.service.descriptionworkflow.DescriptionWorkflowService;
import org.opencdmp.service.elastic.ElasticService;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.lock.LockService;
import org.opencdmp.service.planblueprint.PlanBlueprintService;
import org.opencdmp.service.planworkflow.PlanWorkflowService;
import org.opencdmp.service.responseutils.ResponseUtilsService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@Service
public class PlanServiceImpl implements PlanService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanServiceImpl.class));

    private final TenantEntityManager entityManager;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private final ConventionService conventionService;

    private final ErrorThesaurusProperties errors;

    private final MessageSource messageSource;

    private final XmlHandlingService xmlHandlingService;

    private final JsonHandlingService jsonHandlingService;

    private final UserScope userScope;

    private final EventBroker eventBroker;

    private final DescriptionService descriptionService;
    private final FileTransformerService fileTransformerService;

    private final NotifyIntegrationEventHandler eventHandler;

    private final NotificationProperties notificationProperties;

    private final ActionConfirmationService actionConfirmationService;

    private final ValidatorFactory validatorFactory;
    
    private final ElasticService elasticService;
    private final DescriptionTemplateService descriptionTemplateService;


    private final AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler;
    private final AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final TenantScope tenantScope;
    private final ResponseUtilsService responseUtilsService;
    private final PlanBlueprintService planBlueprintService;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final DescriptionWorkflowService descriptionWorkflowService;
    private final PlanWorkflowService planWorkflowService;
    private final CustomPolicyService customPolicyService;
    private final UsersProperties usersProperties;
    private final LockService lockService;

    @Autowired
    public PlanServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            QueryFactory queryFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            XmlHandlingService xmlHandlingService,
            JsonHandlingService jsonHandlingService,
            UserScope userScope,
            EventBroker eventBroker,
            DescriptionService descriptionService,
            NotifyIntegrationEventHandler eventHandler,
            NotificationProperties notificationProperties,
            ActionConfirmationService actionConfirmationService,
            FileTransformerService fileTransformerService,
            ValidatorFactory validatorFactory,
            ElasticService elasticService, DescriptionTemplateService descriptionTemplateService,
            AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler, AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler, AuthorizationContentResolver authorizationContentResolver, TenantScope tenantScope, ResponseUtilsService responseUtilsService, PlanBlueprintService planBlueprintService, UsageLimitService usageLimitService, AccountingService accountingService, DescriptionWorkflowService descriptionWorkflowService, PlanWorkflowService planWorkflowService, CustomPolicyService customPolicyService, UsersProperties usersProperties, LockService lockService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.jsonHandlingService = jsonHandlingService;
        this.userScope = userScope;
        this.eventBroker = eventBroker;
        this.descriptionService = descriptionService;
        this.fileTransformerService = fileTransformerService;
        this.eventHandler = eventHandler;
        this.notificationProperties = notificationProperties;
        this.actionConfirmationService = actionConfirmationService;
        this.validatorFactory = validatorFactory;
	    this.elasticService = elasticService;
	    this.descriptionTemplateService = descriptionTemplateService;
	    this.annotationEntityTouchedIntegrationEventHandler = annotationEntityTouchedIntegrationEventHandler;
	    this.annotationEntityRemovalIntegrationEventHandler = annotationEntityRemovalIntegrationEventHandler;
	    this.authorizationContentResolver = authorizationContentResolver;
	    this.tenantScope = tenantScope;
	    this.responseUtilsService = responseUtilsService;
	    this.planBlueprintService = planBlueprintService;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.descriptionWorkflowService = descriptionWorkflowService;
        this.planWorkflowService = planWorkflowService;
        this.customPolicyService = customPolicyService;
        this.usersProperties = usersProperties;
        this.lockService = lockService;
    }

    public Plan persist(PlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, IOException {
        
        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());
        if (isUpdate) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(model.getId())), Permission.EditPlan);
        else {
            this.authorizationService.authorizeForce(Permission.NewPlan);
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.PLAN_COUNT);
        }
        
        PlanEntity data = this.patchAndSave(model);

        PlanBlueprintEntity blueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, data.getBlueprintId(), true);
        if (blueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprintEntity.getDefinition());
        if (definition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getBlueprintId(), org.opencdmp.commons.types.planblueprint.DefinitionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        this.patchAndSaveReferences(this.buildPlanReferencePersists(model.getProperties()), data.getId(), definition);

        if (isUpdate) this.checkIfDescriptionTemplateIsUse(model.getDescriptionTemplates(), model.getId());

        this.patchAndSaveTemplates(data.getId(), model.getDescriptionTemplates());

        if (!isUpdate && this.userScope.isSet()) {
            this.addOwner(data);
            if (model.getUsers() == null) model.setUsers(new ArrayList<>());
            if (model.getUsers().stream().noneMatch(x-> x.getUser() != null && x.getUser().equals(this.userScope.getUserIdSafe()) &&  PlanUserRole.Owner.equals(x.getRole()))) model.getUsers().add(this.createOwnerPersist());
        }

        this.eventBroker.emit(new PlanTouchedEvent(data.getId()));

        this.sendNotification(data);
        this.assignUsers(data.getId(), this.inviteUserOrAssignUsers(data.getId(), model.getUsers(), false), null, false);
        
        this.elasticService.persistPlan(data);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(data.getId());

        return this.builderFactory.builder(PlanBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Plan._id, Plan._hash), data);
    }

    private void checkIfDescriptionTemplateIsUse (List<PlanDescriptionTemplatePersist> descriptionTemplates, UUID id){
        List<PlanDescriptionTemplateEntity> existingPlanDescriptionTemplates = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().planIds(id).isActive(IsActive.Active).collect();

        List<PlanDescriptionTemplateEntity> removedDescriptionTemplates = existingPlanDescriptionTemplates.stream().filter(x -> descriptionTemplates.stream().noneMatch(y -> y.getDescriptionTemplateGroupId().equals(x.getDescriptionTemplateGroupId()))).toList();
        PlanDescriptionTemplateQuery planDescriptionTemplateQuery = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).planIds(id).descriptionTemplateGroupIds(removedDescriptionTemplates.stream().map(PlanDescriptionTemplateEntity::getDescriptionTemplateGroupId).collect(Collectors.toList()));
        DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).planDescriptionTemplateSubQuery(planDescriptionTemplateQuery).isActive(IsActive.Active);

        if (query != null && query.count() > 0) throw new MyValidationException(this.errors.getPlanDescriptionTemplateCanNotRemove().getCode(), this.errors.getPlanDescriptionTemplateCanNotRemove().getMessage());

    }

    private PlanUserPersist createOwnerPersist()  {
        PlanUserPersist persist = new PlanUserPersist();
        persist.setRole(PlanUserRole.Owner);
        persist.setUser(this.userScope.getUserIdSafe());
        return persist;
    }
    
    private void addOwner(PlanEntity planEntity) throws InvalidApplicationException {
        PlanUserEntity data = new PlanUserEntity();
        data.setId(UUID.randomUUID());
        data.setIsActive(IsActive.Active);
        data.setCreatedAt(Instant.now());
        data.setUpdatedAt(Instant.now());
        data.setRole(PlanUserRole.Owner);
        data.setUserId(this.userScope.getUserId());
        data.setPlanId(planEntity.getId());

        this.entityManager.persist(data);
    }

    private void sendNotification(PlanEntity plan) throws InvalidApplicationException {
        PlanStatusEntity planStatusEntity = this.entityManager.find(PlanStatusEntity.class, plan.getStatusId(), true);
        if (planStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{plan.getStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        List<PlanUserEntity> existingUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking()
                .planIds(plan.getId())
                .isActives(IsActive.Active)
                .collect();

        if (existingUsers == null || existingUsers.size() <= 1){
            return;
        }

        for (PlanUserEntity planUser : existingUsers) {
            if (!planUser.getUserId().equals(this.userScope.getUserIdSafe())){
                UserEntity user = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUser.getUserId()).first();
                if (user == null || user.getIsActive().equals(IsActive.Inactive)) throw new MyValidationException(this.errors.getPlanInactiveUser().getCode(), this.errors.getPlanInactiveUser().getMessage());
	            this.createPlanNotificationEvent(plan, planStatusEntity, user);
            }
        }
    }

    private void createPlanNotificationEvent(PlanEntity plan, PlanStatusEntity planStatus, UserEntity user) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        if (planStatus.getInternalStatus() == null) event.setNotificationType(this.notificationProperties.getPlanStatusChangedType());
        else this.applyNotificationType(planStatus.getInternalStatus(), event);

        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScope.getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, plan.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, plan.getId().toString()));
        if (planStatus.getInternalStatus() == null) fieldInfoList.add(new FieldInfo("{statusName}", DataType.String, planStatus.getName()));
        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

	    this.eventHandler.handle(event);
    }

    private void applyNotificationType(PlanStatus status, NotifyIntegrationEvent event) {
        switch (status) {
            case Draft:
                event.setNotificationType(this.notificationProperties.getPlanModifiedType());
                break;
            case Finalized:
                event.setNotificationType(this.notificationProperties.getPlanFinalisedType());
                break;
            default:
                throw new MyApplicationException("Unsupported Plan Status.");
        }
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException {
        logger.debug("deleting plan: {}", id);

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(id)), Permission.DeletePlan);
        
        PlanEntity data = this.entityManager.find(PlanEntity.class, id);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().types(EntityType.Plan).entityIds(data.getId());
        if (entityDoiQuery.count() > 0) throw new MyApplicationException("Plan is deposited can not deleted");

        PlanEntity previousPlan = null;
        if (!data.getVersionStatus().equals(PlanVersionStatus.Previous)){
            PlanQuery planQuery = this.queryFactory.query(PlanQuery.class)
                    .excludedIds(data.getId())
                    .isActive(IsActive.Active)
                    .groupIds(data.getGroupId());

            planQuery.setOrder(new Ordering().addDescending(Plan._version));
            previousPlan = planQuery.count() > 0 ? planQuery.collect().getFirst() : null;
            if (previousPlan != null){
                PlanStatusEntity previousPlanStatusEntity = this.entityManager.find(PlanStatusEntity.class, previousPlan.getStatusId(), true);
                if (previousPlanStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{previousPlan.getStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                if (previousPlanStatusEntity.getInternalStatus() != null && previousPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized)) previousPlan.setVersionStatus(PlanVersionStatus.Current);
                else previousPlan.setVersionStatus(PlanVersionStatus.NotFinalized);
                this.entityManager.merge(previousPlan);
            }
            data.setVersionStatus(PlanVersionStatus.NotFinalized);
            this.entityManager.merge(data);
            this.entityManager.flush();
        }
        
        this.deleterFactory.deleter(PlanDeleter.class).deleteAndSaveByIds(List.of(id), false);
        if (previousPlan != null) this.elasticService.persistPlan(previousPlan);

        this.annotationEntityRemovalIntegrationEventHandler.handlePlan(data.getId());
    }

    @Override
    public Plan createNewVersion(NewVersionPlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("persisting data bew version").And("model", model).And("fields", fields));
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation( model.getId())), Permission.CreateNewVersionPlan);

        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.PLAN_COUNT);
        PlanEntity oldPlanEntity = this.entityManager.find(PlanEntity.class, model.getId(), true);
        if (oldPlanEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(oldPlanEntity.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        
        List<PlanEntity> latestVersionPlans = this.queryFactory.query(PlanQuery.class).disableTracking()
                .groupIds(oldPlanEntity.getGroupId())
                .isActive(IsActive.Active)
                .versionStatuses(PlanVersionStatus.Current)
                .collect();
        if (latestVersionPlans.isEmpty()) throw new MyValidationException(this.errors.getPlanIsNotFinalized().getCode());
        if (latestVersionPlans.size() > 1) throw new MyValidationException(this.errors.getMultiplePlanVersionsNotSupported().getCode());
        if (!latestVersionPlans.getFirst().getVersion().equals(oldPlanEntity.getVersion())){
            throw new MyValidationException(this.errors.getPlanNewVersionConflict().getCode(), this.errors.getPlanNewVersionConflict().getMessage());
        }
        Long notFinalizedCount = this.queryFactory.query(PlanQuery.class).disableTracking()
                .versionStatuses(PlanVersionStatus.NotFinalized)
                .groupIds(oldPlanEntity.getGroupId())
                .isActive(IsActive.Active)
                .count();
        if (notFinalizedCount > 0) throw new MyValidationException(this.errors.getPlanNewVersionAlreadyCreatedDraft().getCode(), this.errors.getPlanNewVersionAlreadyCreatedDraft().getMessage());

        PlanStatusEntity startingPlanStatusEntity = this.entityManager.find(PlanStatusEntity.class, this.planWorkflowService.getActiveWorkFlowDefinition().getStartingStatusId(), true);
        if (startingPlanStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{this.planWorkflowService.getActiveWorkFlowDefinition().getStartingStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanEntity newPlan = new PlanEntity();
        newPlan.setId(UUID.randomUUID());
        newPlan.setIsActive(IsActive.Active);
        newPlan.setCreatedAt(Instant.now());
        newPlan.setUpdatedAt(Instant.now());
        newPlan.setGroupId(oldPlanEntity.getGroupId());
        newPlan.setVersionStatus(PlanVersionStatus.NotFinalized);
        newPlan.setVersion((short)(oldPlanEntity.getVersion() + 1));
        newPlan.setDescription(model.getDescription());
        newPlan.setLabel(model.getLabel());
        newPlan.setLanguage(oldPlanEntity.getLanguage());
        newPlan.setStatusId(startingPlanStatusEntity.getId());
        newPlan.setProperties(oldPlanEntity.getProperties());
        newPlan.setBlueprintId(model.getBlueprintId());
        newPlan.setAccessType(oldPlanEntity.getAccessType());
        newPlan.setCreatorId(this.userScope.getUserId());

        this.entityManager.persist(newPlan);

        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking()
                .planIds(model.getId())
                .isActives(IsActive.Active)
                .collect();
        List<PlanReferenceEntity> planReferences = this.queryFactory.query(PlanReferenceQuery.class).disableTracking()
                .planIds(model.getId())
                .isActives(IsActive.Active)
                .collect();
        List<PlanDescriptionTemplateEntity> oldPlanDescriptionTemplates = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking()
                .planIds(model.getId())
                .isActive(IsActive.Active)
                .collect();

        for (PlanUserEntity planUser : planUsers) {
            PlanUserEntity newUser = new PlanUserEntity();
            newUser.setId(UUID.randomUUID());
            newUser.setPlanId(newPlan.getId());
            newUser.setUserId(planUser.getUserId());
            newUser.setRole(planUser.getRole());
            newUser.setCreatedAt(Instant.now());
            newUser.setUpdatedAt(Instant.now());
            newUser.setIsActive(IsActive.Active);

            this.entityManager.persist(newUser);
        }

        for (PlanReferenceEntity planReference : planReferences) {
            PlanReferenceEntity newReference = new PlanReferenceEntity();
            newReference.setId(UUID.randomUUID());
            newReference.setPlanId(newPlan.getId());
            newReference.setReferenceId(planReference.getReferenceId());
            newReference.setData(planReference.getData());
            newReference.setCreatedAt(Instant.now());
            newReference.setUpdatedAt(Instant.now());
            newReference.setIsActive(IsActive.Active);

            this.entityManager.persist(newReference);
        }

        this.entityManager.flush();

        PlanBlueprintEntity blueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, model.getBlueprintId(), true);
        if (blueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprintEntity.getDefinition());

        List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(model.getDescriptions().stream().map(NewVersionPlanDescriptionPersist::getDescriptionId).distinct().collect(Collectors.toList())).collect();

        FieldSet fieldSet = new BaseFieldSet(Description._id, BaseFieldSet.asIndexer(Description._descriptionTemplate, DescriptionTemplate._groupId), Description._isActive, BaseFieldSet.asIndexer(Description._status, org.opencdmp.model.descriptionstatus.DescriptionStatus._id));
        List<Description> models = this.builderFactory.builder(DescriptionBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, descriptionEntities);

        DescriptionStatusEntity canceledStatusEntity = this.findDescriptionStatus(DescriptionStatus.Canceled);
        if (canceledStatusEntity == null) throw new MyApplicationException("canceled status not found");

        if (!this.conventionService.isListNullOrEmpty(models) && !this.conventionService.isListNullOrEmpty(model.getDescriptions())){
            for (NewVersionPlanDescriptionPersist newVersionPlanDescriptionPersist : model.getDescriptions()) {
                Description description = models.stream().filter(x -> x.getId().equals(newVersionPlanDescriptionPersist.getDescriptionId())).findFirst().orElse(null);
                if (description != null && (description.getIsActive().equals(IsActive.Active) || canceledStatusEntity.getId().equals(description.getStatus().getId()))){
                    PlanDescriptionTemplateEntity existingPlanDescriptionTemplateEntity = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().planIds(newPlan.getId()).isActive(IsActive.Active).sectionIds(newVersionPlanDescriptionPersist.getBlueprintSectionId()).descriptionTemplateGroupIds(description.getDescriptionTemplate().getGroupId()).first();
                    if (existingPlanDescriptionTemplateEntity == null){
                        PlanDescriptionTemplateEntity newTemplate = new PlanDescriptionTemplateEntity();
                        newTemplate.setId(UUID.randomUUID());
                        newTemplate.setPlanId(newPlan.getId());
                        newTemplate.setDescriptionTemplateGroupId(description.getDescriptionTemplate().getGroupId());
                        newTemplate.setSectionId(newVersionPlanDescriptionPersist.getBlueprintSectionId());
                        newTemplate.setCreatedAt(Instant.now());
                        newTemplate.setUpdatedAt(Instant.now());
                        newTemplate.setIsActive(IsActive.Active);
                        this.entityManager.persist(newTemplate);
                        this.entityManager.flush();
                        this.cloneDescription(newPlan.getId(), null, newVersionPlanDescriptionPersist.getDescriptionId(), newTemplate.getId(), description.getIsActive().equals(IsActive.Active));
                    } else{
                        this.cloneDescription(newPlan.getId(), null, newVersionPlanDescriptionPersist.getDescriptionId(), existingPlanDescriptionTemplateEntity.getId(), description.getIsActive().equals(IsActive.Active));
                    }
                }
            }
        }

        List<PlanDescriptionTemplateEntity> newPlanDescriptionTemplateEntitiesEntities = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().planIds(newPlan.getId()).isActive(IsActive.Active).collect();
        List<SectionEntity> sections = definition.getSections().stream().filter(SectionEntity::getHasTemplates).collect(Collectors.toList());
        if (!this.conventionService.isListNullOrEmpty(oldPlanDescriptionTemplates)) {
            for (PlanDescriptionTemplateEntity oldPlanDescriptionTemplate : oldPlanDescriptionTemplates) {
                for (SectionEntity section : sections) {
                    if (section.getId().equals(oldPlanDescriptionTemplate.getSectionId())) {
                        PlanDescriptionTemplateEntity oldDescriptionTemplateEntity = oldPlanDescriptionTemplates.stream().filter(x -> x.getSectionId().equals(section.getId()) && x.getDescriptionTemplateGroupId().equals(oldPlanDescriptionTemplate.getDescriptionTemplateGroupId())).findFirst().orElse(null);

                        boolean isDescriptionTemplateAlreadyInPlan = false;

                        if (!this.conventionService.isListNullOrEmpty(newPlanDescriptionTemplateEntitiesEntities)) {
                            PlanDescriptionTemplateEntity existingBlueprintDescriptionTemplateEntity = newPlanDescriptionTemplateEntitiesEntities.stream().filter(x -> x.getSectionId().equals(section.getId()) && x.getDescriptionTemplateGroupId().equals(oldPlanDescriptionTemplate.getDescriptionTemplateGroupId())).findFirst().orElse(null);
                            isDescriptionTemplateAlreadyInPlan = existingBlueprintDescriptionTemplateEntity != null;
                        }

                        if (oldDescriptionTemplateEntity != null && !isDescriptionTemplateAlreadyInPlan) {
                            PlanDescriptionTemplateEntity newTemplate = new PlanDescriptionTemplateEntity();
                            newTemplate.setId(UUID.randomUUID());
                            newTemplate.setPlanId(newPlan.getId());
                            newTemplate.setDescriptionTemplateGroupId(oldPlanDescriptionTemplate.getDescriptionTemplateGroupId());
                            newTemplate.setSectionId(section.getId());
                            newTemplate.setCreatedAt(Instant.now());
                            newTemplate.setUpdatedAt(Instant.now());
                            newTemplate.setIsActive(IsActive.Active);
                            this.entityManager.persist(newTemplate);
                            this.entityManager.flush();
                            newPlanDescriptionTemplateEntitiesEntities.add(newTemplate);
                        }
                    }
                }
            }
        }
        if (!oldPlanEntity.getBlueprintId().equals(blueprintEntity.getId())){
            // add description templates if exists in new blueprint
            if (!this.conventionService.isListNullOrEmpty(sections)){
                for (SectionEntity section: sections) {
                    if (!this.conventionService.isListNullOrEmpty(section.getDescriptionTemplates()) && section.getHasTemplates()){
                        // new blueprint templates
                        for (BlueprintDescriptionTemplateEntity blueprintDescriptionTemplate: section.getDescriptionTemplates()) {

                            boolean isDescriptionTemplateAlreadyInPlan = false;

                            if (!this.conventionService.isListNullOrEmpty(newPlanDescriptionTemplateEntitiesEntities)) {
                                PlanDescriptionTemplateEntity existingBlueprintDescriptionTemplateEntity = newPlanDescriptionTemplateEntitiesEntities.stream().filter(x -> x.getSectionId().equals(section.getId()) && x.getDescriptionTemplateGroupId().equals(blueprintDescriptionTemplate.getDescriptionTemplateGroupId())).findFirst().orElse(null);
                                isDescriptionTemplateAlreadyInPlan = existingBlueprintDescriptionTemplateEntity != null;
                            }

                            if (!isDescriptionTemplateAlreadyInPlan){
                                PlanDescriptionTemplateEntity newTemplate = new PlanDescriptionTemplateEntity();
                                newTemplate.setId(UUID.randomUUID());
                                newTemplate.setPlanId(newPlan.getId());
                                newTemplate.setDescriptionTemplateGroupId(blueprintDescriptionTemplate.getDescriptionTemplateGroupId());
                                newTemplate.setSectionId(section.getId());
                                newTemplate.setCreatedAt(Instant.now());
                                newTemplate.setUpdatedAt(Instant.now());
                                newTemplate.setIsActive(IsActive.Active);
                                this.entityManager.persist(newTemplate);
                                this.entityManager.flush();
                                newPlanDescriptionTemplateEntitiesEntities.add(newTemplate);
                            }
                        }
                    }
                }
            }
        }
        
        this.entityManager.flush();

        this.updateVersionStatusAndSave(newPlan, PlanStatus.Draft, startingPlanStatusEntity.getInternalStatus());

        this.entityManager.flush();

        this.elasticService.persistPlan(oldPlanEntity);
        this.elasticService.persistPlan(newPlan);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(newPlan.getId());
        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(oldPlanEntity.getId());

        this.accountingService.increase(UsageLimitTargetMetric.PLAN_COUNT.getValue());

        return this.builderFactory.builder(PlanBuilder.class).build(BaseFieldSet.build(fields, Plan._id), newPlan);
    }

    public void cloneDescription(UUID planId, Map<UUID, UUID> planDescriptionTemplateRemap, UUID descriptionId, UUID newPlanDescriptionTemplateId, boolean isActive) throws InvalidApplicationException, IOException {
        logger.debug("cloning description: {} with description: {}", descriptionId, planId);

        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.DESCRIPTION_COUNT);
        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).isActive(IsActive.Active).first();

        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (planEntity.getAccessType() != null && !planEntity.getAccessType().equals(PlanAccessType.Public)) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(descriptionId)), Permission.CloneDescription);
        else this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(descriptionId)), Permission.PublicCloneDescription);

        DescriptionEntity existing = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionId).isActive(isActive ?  IsActive.Active : IsActive.Inactive).first();

        if (existing == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DescriptionEntity newDescription = new DescriptionEntity();
        newDescription.setId(UUID.randomUUID());
        newDescription.setLabel(existing.getLabel());
        newDescription.setDescription(existing.getDescription());
        newDescription.setStatusId(this.descriptionWorkflowService.getActiveWorkFlowDefinition().getStartingStatusId());
        newDescription.setProperties(existing.getProperties());
        newDescription.setPlanId(planId);
        if (newPlanDescriptionTemplateId == null && planDescriptionTemplateRemap != null) newDescription.setPlanDescriptionTemplateId(planDescriptionTemplateRemap.get(existing.getPlanDescriptionTemplateId()));
        else newDescription.setPlanDescriptionTemplateId(newPlanDescriptionTemplateId);
        newDescription.setDescriptionTemplateId(existing.getDescriptionTemplateId());
        newDescription.setCreatedById(this.userScope.getUserId());
        newDescription.setCreatedAt(Instant.now());
        newDescription.setUpdatedAt(Instant.now());
        newDescription.setIsActive(IsActive.Active);

        this.entityManager.persist(newDescription);

        List<DescriptionReferenceEntity> descriptionReferences = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking()
                .descriptionIds(existing.getId())
                .isActive(isActive ?  IsActive.Active : IsActive.Inactive)
                .collect();

        List<DescriptionTagEntity> descriptionTags = this.queryFactory.query(DescriptionTagQuery.class).disableTracking()
                .descriptionIds(existing.getId())
                .isActive(isActive ?  IsActive.Active : IsActive.Inactive)
                .collect();

        for (DescriptionReferenceEntity descriptionReference : descriptionReferences) {
            DescriptionReferenceEntity newReference = new DescriptionReferenceEntity();
            newReference.setId(UUID.randomUUID());
            newReference.setDescriptionId(newDescription.getId());
            newReference.setReferenceId(descriptionReference.getReferenceId());
            newReference.setData(descriptionReference.getData());
            newReference.setCreatedAt(Instant.now());
            newReference.setUpdatedAt(Instant.now());
            newReference.setIsActive(IsActive.Active);

            this.entityManager.persist(newReference);
        }

        for(DescriptionTagEntity descriptionTag : descriptionTags) {
            DescriptionTagEntity newTag = new DescriptionTagEntity();
            newTag.setId(UUID.randomUUID());
            newTag.setDescriptionId(newDescription.getId());
            newTag.setTagId(descriptionTag.getTagId());
            newTag.setCreatedAt(Instant.now());
            newTag.setUpdatedAt(Instant.now());
            newTag.setIsActive(IsActive.Active);

            this.entityManager.persist(newTag);
        }

        this.entityManager.flush();

        this.elasticService.persistDescription(newDescription);

        this.annotationEntityTouchedIntegrationEventHandler.handleDescription(newDescription.getId());
        this.annotationEntityTouchedIntegrationEventHandler.handleDescription(existing.getId());

        this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_COUNT.getValue());
    }

    private void clonePublicDescription(UUID planId, Map<UUID, UUID> planDescriptionTemplateRemap, UUID descriptionId, org.opencdmp.commons.types.planblueprint.DefinitionEntity blueprintDefinition) throws InvalidApplicationException, IOException {
        logger.debug("cloning public description: {} with description: {}", descriptionId, planId);

        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.DESCRIPTION_COUNT);

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(descriptionId)), Permission.PublicCloneDescription);

        DescriptionEntity existing = null;
        List<DescriptionReferenceEntity> descriptionReferences = new ArrayList<>();
        List<ReferenceEntity> referenceEntities = new ArrayList<>();

        List<DescriptionTagEntity> descriptionTags = new ArrayList<>();
        List<TagEntity> tags = new ArrayList<>();
        try {
            this.entityManager.disableTenantFilters();
            PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
            existing = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(descriptionId).planSubQuery(this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public)).first();

            if (existing == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            PlanDescriptionTemplateEntity planDescriptionTemplate = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().ids(existing.getPlanDescriptionTemplateId()).first();
            if (planDescriptionTemplate == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{existing.getPlanDescriptionTemplateId(), PlanDescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            if (blueprintDefinition != null && planDescriptionTemplate.getSectionId() != null && !this.conventionService.isListNullOrEmpty(blueprintDefinition.getSections())) {
                if (blueprintDefinition.getSections().stream().filter(x -> x.getId().equals(planDescriptionTemplate.getSectionId()) && x.getHasTemplates()).findFirst().orElse(null) == null) {
                    // ignore this description because don't exist templates in this section
                    return;
                }
            }

            this.entityManager.disableTenantFilters();
            descriptionReferences = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking()
                    .descriptionIds(existing.getId())
                    .isActive(IsActive.Active)
                    .collect();

            if (!this.conventionService.isListNullOrEmpty(descriptionReferences)) {
                this.entityManager.disableTenantFilters();
                referenceEntities = this.queryFactory.query(ReferenceQuery.class).disableTracking()
                        .ids(descriptionReferences.stream().map(DescriptionReferenceEntity::getReferenceId).distinct().toList())
                        .collect();
            }

            this.entityManager.disableTenantFilters();
            descriptionTags = this.queryFactory.query(DescriptionTagQuery.class).disableTracking()
                    .descriptionIds(existing.getId())
                    .isActive(IsActive.Active)
                    .collect();

            if (!this.conventionService.isListNullOrEmpty(descriptionTags)) {
                this.entityManager.disableTenantFilters();
                tags = this.queryFactory.query(TagQuery.class).disableTracking()
                        .ids(descriptionTags.stream().map(DescriptionTagEntity::getTagId).distinct().toList())
                        .collect();
            }

            this.entityManager.reloadTenantFilters();
        } finally {
            this.entityManager.reloadTenantFilters();
            DescriptionEntity newDescription = new DescriptionEntity();
            newDescription.setId(UUID.randomUUID());
            newDescription.setLabel(existing.getLabel());
            newDescription.setDescription(existing.getDescription());
            newDescription.setStatusId(this.descriptionWorkflowService.getActiveWorkFlowDefinition().getStartingStatusId());
            newDescription.setProperties(existing.getProperties());
            newDescription.setPlanId(planId);
            if (planDescriptionTemplateRemap != null) newDescription.setPlanDescriptionTemplateId(planDescriptionTemplateRemap.get(existing.getPlanDescriptionTemplateId()));
            newDescription.setDescriptionTemplateId(existing.getDescriptionTemplateId());
            newDescription.setCreatedById(this.userScope.getUserId());
            newDescription.setCreatedAt(Instant.now());
            newDescription.setUpdatedAt(Instant.now());
            newDescription.setIsActive(IsActive.Active);

            this.entityManager.persist(newDescription);
            if (newDescription.getId() != null){

                for (DescriptionReferenceEntity descriptionReference : descriptionReferences) {
                    if (!this.conventionService.isListNullOrEmpty(referenceEntities)){
                        ReferenceEntity existingReference = referenceEntities.stream().filter(x -> x.getId().equals(descriptionReference.getReferenceId())).findFirst().orElse(null);
                        if (existingReference != null) {
                            // persist reference for selected tenant
                            ReferenceEntity existingTenantReference = this.queryFactory.query(ReferenceQuery.class).ids(existingReference.getId()).first(); //TODO: optimize
                            if (existingTenantReference == null) existingTenantReference = this.queryFactory.query(ReferenceQuery.class).references(existingReference.getReference()).typeIds(existingReference.getTypeId()).sources(existingReference.getSource()).first();
                            UUID referenceId;
                            if (existingTenantReference == null) {
                                ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(existingReference.getTypeId()).firstAs(new BaseFieldSet().ensure(ReferenceType._id));//TODO: optimize
                                if (referenceTypeEntity == null) continue;

                                ReferenceEntity newReferenceEntity = new ReferenceEntity();
                                newReferenceEntity.setId(UUID.randomUUID());
                                newReferenceEntity.setLabel(existingReference.getLabel());
                                newReferenceEntity.setReference(existingReference.getReference());
                                newReferenceEntity.setTypeId(referenceTypeEntity.getId());
                                newReferenceEntity.setSource(existingReference.getSource());
                                newReferenceEntity.setSourceType(existingReference.getSourceType());
                                newReferenceEntity.setAbbreviation(existingReference.getAbbreviation());
                                newReferenceEntity.setDescription(existingReference.getDescription());
                                newReferenceEntity.setDefinition(existingReference.getDefinition());
                                this.entityManager.persist(newReferenceEntity);

                                referenceId = newReferenceEntity.getId();
                            } else {
                                referenceId = existingTenantReference.getId();
                            }
                            DescriptionReferenceEntity newDescriptionReference = new DescriptionReferenceEntity();
                            newDescriptionReference.setId(UUID.randomUUID());
                            newDescriptionReference.setDescriptionId(newDescription.getId());
                            newDescriptionReference.setReferenceId(referenceId);
                            newDescriptionReference.setData(descriptionReference.getData());
                            newDescriptionReference.setCreatedAt(Instant.now());
                            newDescriptionReference.setUpdatedAt(Instant.now());
                            newDescriptionReference.setIsActive(IsActive.Active);

                            this.entityManager.persist(newDescriptionReference);
                        }

                    }
                }

                for (DescriptionTagEntity descriptionTag : descriptionTags) {
                    if (!this.conventionService.isListNullOrEmpty(tags)) {
                        TagEntity existingTag = tags.stream().filter(x -> x.getId().equals(descriptionTag.getTagId())).findFirst().orElse(null);
                        if (existingTag != null) {
                            UUID tagId;
                            // persist tag for selected tenant
                            TagEntity existingTenantTag = this.queryFactory.query(TagQuery.class).ids(existingTag.getId()).first(); //TODO: optimize
                            if (existingTenantTag == null) {
                                TagEntity tagEntity = new TagEntity();
                                tagEntity.setId(UUID.randomUUID());
                                tagEntity.setId(UUID.randomUUID());
                                tagEntity.setLabel(existingTag.getLabel());
                                tagEntity.setIsActive(IsActive.Active);
                                tagEntity.setCreatedAt(Instant.now());
                                tagEntity.setUpdatedAt(Instant.now());
                                this.entityManager.persist(tagEntity);

                                tagId = tagEntity.getId();
                            } else {
                                tagId = existingTenantTag.getId();
                            }
                            DescriptionTagEntity newTag = new DescriptionTagEntity();
                            newTag.setId(UUID.randomUUID());
                            newTag.setDescriptionId(newDescription.getId());
                            newTag.setTagId(tagId);
                            newTag.setCreatedAt(Instant.now());
                            newTag.setUpdatedAt(Instant.now());
                            newTag.setIsActive(IsActive.Active);

                            this.entityManager.persist(newTag);
                        }
                    }

                }

                this.entityManager.flush();

                this.elasticService.persistDescription(newDescription);

                this.annotationEntityTouchedIntegrationEventHandler.handleDescription(newDescription.getId());
                this.annotationEntityTouchedIntegrationEventHandler.handleDescription(existing.getId());

                this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_COUNT.getValue());
                this.entityManager.reloadTenantFilters();
            }
        }

    }

    private void updateVersionStatusAndSave(PlanEntity data, PlanStatus previousStatus, PlanStatus newStatus) throws InvalidApplicationException {
        if (previousStatus == null && newStatus == null)
            return;
        if (previousStatus != null && previousStatus.equals(newStatus))
            return;
        if (previousStatus != null && previousStatus.equals(PlanStatus.Finalized) && (newStatus == null || newStatus.equals(PlanStatus.Draft))){
            boolean alreadyCreatedNewVersion = this.queryFactory.query(PlanQuery.class).disableTracking()
                    .versionStatuses(PlanVersionStatus.NotFinalized, PlanVersionStatus.Current)
                    .excludedIds(data.getId())
                    .isActive(IsActive.Active)
                    .groupIds(data.getGroupId())
                    .count() > 0;
            if (alreadyCreatedNewVersion) throw new MyValidationException(this.errors.getPlanNewVersionAlreadyCreatedDraft().getCode(), this.errors.getPlanNewVersionAlreadyCreatedDraft().getMessage());;

            data.setVersionStatus(PlanVersionStatus.NotFinalized);
            this.entityManager.merge(data);
        }

        if (newStatus != null && newStatus.equals(PlanStatus.Finalized)) {
            List<PlanEntity> latestVersionPlans = this.queryFactory.query(PlanQuery.class)
                    .versionStatuses(PlanVersionStatus.Current).excludedIds(data.getId())
                    .isActive(IsActive.Active).groupIds(data.getGroupId()).collect();
            if (latestVersionPlans.size() > 1)
                throw new MyValidationException("Multiple previous template found");
            PlanEntity oldPlanEntity = latestVersionPlans.stream().findFirst().orElse(null);

            data.setVersionStatus(PlanVersionStatus.Current);

            if (oldPlanEntity != null) {
                data.setVersion((short) (oldPlanEntity.getVersion() + 1));

                oldPlanEntity.setVersionStatus(PlanVersionStatus.Previous);
                this.entityManager.merge(oldPlanEntity);
            } else {
                data.setVersion((short) 1);
            }
        }
    }

    @Override
    public Plan buildClone(ClonePlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, IOException, InvalidApplicationException {

        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.PLAN_COUNT);

        PlanEntity existingPlanEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(model.getId()).firstAs(fields.ensure(Plan._isActive));

        if (existingPlanEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        boolean isActive =  existingPlanEntity.getIsActive() != null && existingPlanEntity.getIsActive().equals(IsActive.Active);

        if (existingPlanEntity.getAccessType() != null && !existingPlanEntity.getAccessType().equals(PlanAccessType.Public)) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation( model.getId())), Permission.ClonePlan);
        else this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation( model.getId())), Permission.PublicClonePlan);

        PlanEntity newPlan = new PlanEntity();
        newPlan.setId(UUID.randomUUID());
        newPlan.setIsActive(IsActive.Active);
        newPlan.setCreatedAt(Instant.now());
        newPlan.setUpdatedAt(Instant.now());
        newPlan.setGroupId(UUID.randomUUID());
        newPlan.setVersion((short) 1);
        newPlan.setVersionStatus(PlanVersionStatus.NotFinalized);
        newPlan.setDescription(model.getDescription());
        newPlan.setLabel(model.getLabel());
        newPlan.setLanguage(existingPlanEntity.getLanguage());
        newPlan.setStatusId(this.planWorkflowService.getActiveWorkFlowDefinition().getStartingStatusId());
        newPlan.setProperties(existingPlanEntity.getProperties());
        newPlan.setBlueprintId(existingPlanEntity.getBlueprintId());
        newPlan.setAccessType(existingPlanEntity.getAccessType());
        newPlan.setCreatorId(this.userScope.getUserId());

        this.entityManager.persist(newPlan);

        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking()
                .planIds(model.getId())
                .isActives(isActive ?  IsActive.Active : IsActive.Inactive )
                .collect();
        List<PlanReferenceEntity> planReferences = this.queryFactory.query(PlanReferenceQuery.class).disableTracking()
                .planIds(model.getId())
                .isActives(isActive ?  IsActive.Active : IsActive.Inactive )
                .collect();
        List<PlanDescriptionTemplateEntity> planDescriptionTemplates = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking()
                .planIds(model.getId())
                .isActive(isActive ?  IsActive.Active : IsActive.Inactive )
                .collect();

        UUID currentUserId = this.userScope.getUserId();

        boolean isCurrentUserInPlan = planUsers.stream().anyMatch(u ->  u.getUserId().equals(currentUserId));

        if (!isCurrentUserInPlan) {
            this.addOwner(newPlan);
        } else {
            PlanUserEntity currentPlanUser = planUsers.stream().filter(u ->  u.getUserId().equals(currentUserId)).toList().getFirst();

            boolean isCurrentUserOwner = PlanUserRole.Owner.equals(currentPlanUser.getRole());
            if (!isCurrentUserOwner) {
                planUsers.remove(currentPlanUser);
                this.addOwner(newPlan);
            }
        }

        for (PlanUserEntity planUser : planUsers) {
            PlanUserEntity newUser = new PlanUserEntity();
            newUser.setId(UUID.randomUUID());
            newUser.setPlanId(newPlan.getId());
            newUser.setUserId(planUser.getUserId());
            newUser.setRole(planUser.getRole());
            newUser.setCreatedAt(Instant.now());
            newUser.setUpdatedAt(Instant.now());
            newUser.setIsActive(IsActive.Active);

            this.entityManager.persist(newUser);
        }

        for (PlanReferenceEntity planReference : planReferences) {
            PlanReferenceEntity newReference = new PlanReferenceEntity();
            newReference.setId(UUID.randomUUID());
            newReference.setPlanId(newPlan.getId());
            newReference.setReferenceId(planReference.getReferenceId());
            newReference.setData(planReference.getData());
            newReference.setCreatedAt(Instant.now());
            newReference.setUpdatedAt(Instant.now());
            newReference.setIsActive(IsActive.Active);

            this.entityManager.persist(newReference);
        }

        Map<UUID, UUID> planDescriptionTemplateRemap = new HashMap<>();
        for (PlanDescriptionTemplateEntity planDescriptionTemplate : planDescriptionTemplates) {
            PlanDescriptionTemplateEntity newTemplate = new PlanDescriptionTemplateEntity();
            newTemplate.setId(UUID.randomUUID());
            newTemplate.setPlanId(newPlan.getId());
            newTemplate.setDescriptionTemplateGroupId(planDescriptionTemplate.getDescriptionTemplateGroupId());
            newTemplate.setSectionId(planDescriptionTemplate.getSectionId());
            newTemplate.setCreatedAt(Instant.now());
            newTemplate.setUpdatedAt(Instant.now());
            newTemplate.setIsActive(IsActive.Active);
            planDescriptionTemplateRemap.put(planDescriptionTemplate.getId(), newTemplate.getId());

            this.entityManager.persist(newTemplate);
        }

        this.entityManager.flush();

        this.elasticService.persistPlan(newPlan);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(newPlan.getId());

        this.accountingService.increase(UsageLimitTargetMetric.PLAN_COUNT.getValue());

        PlanEntity resultingPlanEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(newPlan.getId()).firstAs(fields);
        if (!this.conventionService.isListNullOrEmpty(model.getDescriptions())){
            for (UUID description: model.getDescriptions()) {
	            this.cloneDescription(newPlan.getId(), planDescriptionTemplateRemap, description, null, isActive);
            }
        }
        return this.builderFactory.builder(PlanBuilder.class).build(fields, resultingPlanEntity);
    }

    public Plan buildPublicClone(ClonePlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, IOException, InvalidApplicationException {

        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.PLAN_COUNT);

        PlanEntity existingPlanEntity = null;
        PlanEntity newPlan = new PlanEntity();
        PlanBlueprintEntity existingBlueprintEntity = null;
        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = null;
        List<PlanReferenceEntity> planReferences = new ArrayList<>();
        List<ReferenceEntity> referenceEntities = new ArrayList<>();
        List<PlanDescriptionTemplateEntity> planDescriptionTemplates = new ArrayList<>();
        List<org.opencdmp.data.DescriptionTemplateEntity> descriptionTemplates = new ArrayList<>();
        List<DescriptionTemplateTypeEntity> descriptionTemplateTypes = new ArrayList<>();

        try {
            this.entityManager.disableTenantFilters();
            // query for public plan
            PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
            existingPlanEntity = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(model.getId()).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public).firstAs(fields);

            if (existingPlanEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PublicPlan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation( model.getId())), Permission.PublicClonePlan);

            // find blueprint code to check if exist in current tenant
            existingBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking()
                    .ids(existingPlanEntity.getBlueprintId())
                    .first();

            if (existingBlueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{existingPlanEntity.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            this.entityManager.disableTenantFilters();
            planReferences = this.queryFactory.query(PlanReferenceQuery.class).disableTracking()
                    .planIds(model.getId())
                    .isActives(IsActive.Active)
                    .collect();
            this.entityManager.disableTenantFilters();
            planDescriptionTemplates = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking()
                    .planIds(model.getId())
                    .isActive(IsActive.Active)
                    .collect();

            if (!this.conventionService.isListNullOrEmpty(planReferences)) {
                this.entityManager.disableTenantFilters();
                referenceEntities = this.queryFactory.query(ReferenceQuery.class).disableTracking()
                        .ids(planReferences.stream().map(PlanReferenceEntity::getReferenceId).distinct().toList())
                        .collect();
            }

            if (!this.conventionService.isListNullOrEmpty(planDescriptionTemplates)) {
                this.entityManager.disableTenantFilters();
                descriptionTemplates = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                        .groupIds(planDescriptionTemplates.stream().map(PlanDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().toList())
                        .versionStatuses(DescriptionTemplateVersionStatus.Current)
                        .collect();
            }

            if (!this.conventionService.isListNullOrEmpty(descriptionTemplates)){
                this.entityManager.disableTenantFilters();
                descriptionTemplateTypes = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking()
                        .ids(descriptionTemplates.stream().map(org.opencdmp.data.DescriptionTemplateEntity::getTypeId).distinct().toList())
                        .collect();
            }

            this.entityManager.reloadTenantFilters();
        } finally {
            this.entityManager.reloadTenantFilters();
            if (existingPlanEntity != null && existingBlueprintEntity != null) {

                PlanBlueprintEntity blueprintEntityByTenant = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(existingBlueprintEntity.getId()).first();
                if (blueprintEntityByTenant == null){
                    blueprintEntityByTenant = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().groupIds(existingBlueprintEntity.getGroupId()).versionStatuses(PlanBlueprintVersionStatus.Current).isActive(IsActive.Active).statuses(PlanBlueprintStatus.Finalized).first();
                    if (blueprintEntityByTenant == null){
                        blueprintEntityByTenant = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().codes(existingBlueprintEntity.getCode()).versionStatuses(PlanBlueprintVersionStatus.Current).isActive(IsActive.Active).first();
                        if (blueprintEntityByTenant == null) {
                            throw new MyValidationException(this.errors.getPlanBlueprintImportNotFound().getCode(), existingBlueprintEntity.getCode());
                        }
                    }
                }
                if (!blueprintEntityByTenant.getStatus().equals(PlanBlueprintStatus.Finalized)) throw new MyValidationException(this.errors.getPlanBlueprintImportDraft().getCode(), existingBlueprintEntity.getCode());

                definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprintEntityByTenant.getDefinition());
                if (definition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{blueprintEntityByTenant.getId(), org.opencdmp.commons.types.planblueprint.DefinitionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                
                newPlan.setId(UUID.randomUUID());
                newPlan.setIsActive(IsActive.Active);
                newPlan.setCreatedAt(Instant.now());
                newPlan.setUpdatedAt(Instant.now());
                newPlan.setGroupId(UUID.randomUUID());
                newPlan.setVersion((short) 1);
                newPlan.setVersionStatus(PlanVersionStatus.NotFinalized);
                newPlan.setDescription(model.getDescription());
                newPlan.setLabel(model.getLabel());
                newPlan.setLanguage(existingPlanEntity.getLanguage());
                newPlan.setStatusId(this.planWorkflowService.getActiveWorkFlowDefinition().getStartingStatusId());
                newPlan.setProperties(existingPlanEntity.getProperties());
                newPlan.setBlueprintId(blueprintEntityByTenant.getId());
                newPlan.setAccessType(existingPlanEntity.getAccessType());
                newPlan.setCreatorId(this.userScope.getUserId());

                this.entityManager.persist(newPlan);
            }

            if (newPlan.getId() != null) {
                this.addOwner(newPlan);

                for (PlanReferenceEntity planReference : planReferences) {
                    if (!this.conventionService.isListNullOrEmpty(referenceEntities)){
                        ReferenceEntity existingReference = referenceEntities.stream().filter(x -> x.getId().equals(planReference.getReferenceId())).findFirst().orElse(null);
                        if (existingReference != null) {
                            // persist reference for selected tenant
                            ReferenceEntity existingTenantReference = this.queryFactory.query(ReferenceQuery.class).ids(existingReference.getId()).first(); //TODO: optimize
                            if (existingTenantReference == null) existingTenantReference = this.queryFactory.query(ReferenceQuery.class).references(existingReference.getReference()).typeIds(existingReference.getTypeId()).sources(existingReference.getSource()).first();
                            UUID referenceId;
                            if (existingTenantReference == null) {
                                ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(existingReference.getTypeId()).firstAs(new BaseFieldSet().ensure(ReferenceType._id));//TODO: optimize
                                if (referenceTypeEntity == null) continue;

                                ReferenceEntity newReferenceEntity = new ReferenceEntity();
                                newReferenceEntity.setId(UUID.randomUUID());
                                newReferenceEntity.setLabel(existingReference.getLabel());
                                newReferenceEntity.setReference(existingReference.getReference());
                                newReferenceEntity.setTypeId(referenceTypeEntity.getId());
                                newReferenceEntity.setSource(existingReference.getSource());
                                newReferenceEntity.setSourceType(existingReference.getSourceType());
                                newReferenceEntity.setAbbreviation(existingReference.getAbbreviation());
                                newReferenceEntity.setDescription(existingReference.getDescription());
                                newReferenceEntity.setDefinition(existingReference.getDefinition());
                                this.entityManager.persist(newReferenceEntity);

                                referenceId = newReferenceEntity.getId();
                            } else {
                                referenceId = existingTenantReference.getId();
                            }
                            PlanReferenceEntity newPlanReference = new PlanReferenceEntity();
                            newPlanReference.setId(UUID.randomUUID());
                            newPlanReference.setPlanId(newPlan.getId());
                            newPlanReference.setReferenceId(referenceId);
                            newPlanReference.setData(planReference.getData());
                            newPlanReference.setCreatedAt(Instant.now());
                            newPlanReference.setUpdatedAt(Instant.now());
                            newPlanReference.setIsActive(IsActive.Active);

                            this.entityManager.persist(newPlanReference);
                        }

                    }
                }

                Map<UUID, UUID> planDescriptionTemplateRemap = new HashMap<>();
                for (PlanDescriptionTemplateEntity planDescriptionTemplate : planDescriptionTemplates) {
                    if (!this.conventionService.isListNullOrEmpty(descriptionTemplates)) {
                        org.opencdmp.data.DescriptionTemplateEntity existingDescriptionTemplate = descriptionTemplates.stream().filter(x -> x.getGroupId().equals(planDescriptionTemplate.getDescriptionTemplateGroupId())).findFirst().orElse(null);
                        if (existingDescriptionTemplate != null) {
                            DescriptionTemplateTypeEntity existingDescriptionTemplateType = descriptionTemplateTypes.stream().filter(x -> x.getId().equals(existingDescriptionTemplate.getTypeId())).findFirst().orElse(null);
                            if (existingDescriptionTemplateType != null) {
                                DescriptionTemplateTypeEntity existingTenantTemplateType = this.queryFactory.query(DescriptionTemplateTypeQuery.class).ids(existingDescriptionTemplateType.getId()).first();
                                if (existingTenantTemplateType == null) existingTenantTemplateType = this.queryFactory.query(DescriptionTemplateTypeQuery.class).codes(existingDescriptionTemplateType.getCode()).isActive(IsActive.Active).first();
                                if (existingTenantTemplateType == null) throw new MyValidationException(this.errors.getDescriptionTemplateTypeImportNotFound().getCode(), existingTenantTemplateType.getCode());
                                if (!existingTenantTemplateType.getStatus().equals(DescriptionTemplateTypeStatus.Finalized)) throw new MyValidationException(this.errors.getDescriptionTemplateTypeImportDraft().getCode(), existingTenantTemplateType.getCode());
                            }

                            org.opencdmp.data.DescriptionTemplateEntity existingTenantTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).ids(existingDescriptionTemplate.getId()).first();
                            if (existingTenantTemplate == null) existingTenantTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).groupIds(existingDescriptionTemplate.getId()).first();
                            if (existingTenantTemplate == null) existingTenantTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).codes(existingDescriptionTemplate.getCode()).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).first();
                            if (existingTenantTemplate == null) throw new MyValidationException(this.errors.getDescriptionTemplateImportNotFound().getCode(), existingTenantTemplate.getCode());
                            if (!existingTenantTemplate.getStatus().equals(DescriptionTemplateStatus.Finalized)) throw new MyValidationException(this.errors.getPlanDescriptionTemplateImportDraft().getCode(), existingTenantTemplate.getCode());

                            PlanDescriptionTemplateEntity newTemplate = new PlanDescriptionTemplateEntity();
                            newTemplate.setId(UUID.randomUUID());
                            newTemplate.setPlanId(newPlan.getId());
                            newTemplate.setDescriptionTemplateGroupId(planDescriptionTemplate.getDescriptionTemplateGroupId());
                            newTemplate.setSectionId(planDescriptionTemplate.getSectionId());
                            newTemplate.setCreatedAt(Instant.now());
                            newTemplate.setUpdatedAt(Instant.now());
                            newTemplate.setIsActive(IsActive.Active);
                            planDescriptionTemplateRemap.put(planDescriptionTemplate.getId(), newTemplate.getId());

                            this.entityManager.persist(newTemplate);
                        }
                    }
                }

                this.entityManager.flush();

                this.elasticService.persistPlan(newPlan);

                this.annotationEntityTouchedIntegrationEventHandler.handlePlan(newPlan.getId());

                this.accountingService.increase(UsageLimitTargetMetric.PLAN_COUNT.getValue());

                if (!this.conventionService.isListNullOrEmpty(model.getDescriptions())){
                    for (UUID description: model.getDescriptions()) {
                        this.clonePublicDescription(newPlan.getId(), planDescriptionTemplateRemap, description, definition);
                    }
                }
            }
        }

        return this.builderFactory.builder(PlanBuilder.class).build(fields, existingPlanEntity);
    }

    @Override
    public List<PlanUser> assignUsers(UUID planId, List<PlanUserPersist> model, FieldSet fieldSet, boolean disableDelete) throws InvalidApplicationException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(planId)), Permission.AssignPlanUsers);
        
        if (!disableDelete && (model == null || model.stream().noneMatch(x-> x.getUser() != null && PlanUserRole.Owner.equals(x.getRole())))) throw new MyApplicationException("At least one owner required");

        this.checkDuplicatePlanUser(model);

        PlanEntity planEntity = this.entityManager.find(PlanEntity.class, planId, true);
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        List<PlanUserEntity> existingUsers = this.queryFactory.query(PlanUserQuery.class)
                .planIds(planId)
                .isActives(IsActive.Active)
                .collect();

        List<UUID> updatedCreatedIds = new ArrayList<>();
        for (PlanUserPersist planUser : model) {
            PlanUserEntity planUserEntity = existingUsers.stream().filter(x-> x.getPlanId().equals(planId) && x.getUserId().equals(planUser.getUser()) && x.getRole().equals(planUser.getRole()) && Objects.equals(planUser.getSectionId(), x.getSectionId())).findFirst().orElse(null);
            if (planUserEntity == null){
                planUserEntity = new PlanUserEntity();
                planUserEntity.setId(UUID.randomUUID());
                planUserEntity.setPlanId(planId);
                planUserEntity.setUserId(planUser.getUser());
                planUserEntity.setRole(planUser.getRole());
                planUserEntity.setSectionId(planUser.getSectionId());
                planUserEntity.setCreatedAt(Instant.now());
                planUserEntity.setUpdatedAt(Instant.now());
                planUserEntity.setIsActive(IsActive.Active);
                this.entityManager.persist(planUserEntity);
            }                 
            updatedCreatedIds.add(planUserEntity.getId());
        }

        List<PlanUserEntity> toDelete = existingUsers.stream().filter(x-> updatedCreatedIds.stream().noneMatch(y-> y.equals(x.getId()))).collect(Collectors.toList());
        if (!toDelete.isEmpty() && !disableDelete)  this.deleterFactory.deleter(PlanUserDeleter.class).delete(toDelete);

        this.entityManager.flush();

        List<PlanUserEntity> persisted = this.queryFactory.query(PlanUserQuery.class)
                .planIds(planId)
                .isActives(IsActive.Active)
                .collect();

        this.elasticService.persistPlan(planEntity);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(planEntity.getId());
        
        return this.builderFactory.builder(PlanUserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fieldSet, PlanUser._id, PlanUser._hash), persisted);
    }

    private void checkDuplicatePlanUser(List<PlanUserPersist> model){
        for (PlanUserPersist user: model) {
            List<PlanUserPersist> duplicateUser;
            if (user.getUser() != null){
                duplicateUser = model.stream().filter(x -> x.getUser().equals(user.getUser()) && x.getRole().equals(user.getRole()) && Objects.equals(user.getSectionId(), x.getSectionId())).collect(Collectors.toList());
            } else {
                duplicateUser = model.stream().filter(x -> x.getEmail().equals(user.getEmail()) && x.getRole().equals(user.getRole()) && Objects.equals(user.getSectionId(), x.getSectionId())).collect(Collectors.toList());
            }
            if (duplicateUser.size() > 1) {
                throw new MyValidationException(this.errors.getDuplicatePlanUser().getCode(), this.errors.getDuplicatePlanUser().getMessage());
            }
        }
    }

    @Override
    public Plan removeUser(PlanUserRemovePersist model, FieldSet fields) throws InvalidApplicationException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(model.getPlanId())), Permission.AssignPlanUsers);
        PlanEntity data = this.entityManager.find(PlanEntity.class, model.getPlanId(), true);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (data.getIsActive().equals(IsActive.Inactive)) throw new MyApplicationException("Plan is not Active");
        List<PlanUserEntity> existingUsers = this.queryFactory.query(PlanUserQuery.class)
            .planIds(model.getPlanId()).ids(model.getId()).userRoles(model.getRole())
            .collect();

        if (!existingUsers.isEmpty()) this.deleterFactory.deleter(PlanUserDeleter.class).delete(existingUsers);

        this.entityManager.flush();

        PlanEntity planEntity = this.entityManager.find(PlanEntity.class, model.getPlanId());
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getPlanId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        this.elasticService.persistPlan(planEntity);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(planEntity.getId());

        return this.builderFactory.builder(PlanBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Plan._id, Plan._hash), data);
    }

    @Override
    public ResponseEntity<byte[]> export(UUID id, String transformerId, String exportType, boolean isPublic) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportPlan(id, transformerId, exportType, isPublic);
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    private PlanEntity patchAndSave(PlanPersist model) throws JsonProcessingException, InvalidApplicationException {
        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanEntity data;
        if (isUpdate) {
            data = this.entityManager.find(PlanEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (this.lockService.isLocked(data.getId(), null).getStatus()) throw new MyApplicationException(this.errors.getLockedPlan().getCode(), this.errors.getLockedPlan().getMessage());
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
            PlanStatusEntity oldPlanStatusEntity = this.entityManager.find(PlanStatusEntity.class, data.getStatusId(), true);
            if (oldPlanStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            PlanStatusEntity newPlanStatusEntity = this.entityManager.find(PlanStatusEntity.class, model.getStatusId(), true);
            if (newPlanStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (newPlanStatusEntity.getInternalStatus() != null && newPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized) && oldPlanStatusEntity.getInternalStatus() != null && oldPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized)) {
                this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(model.getId())), Permission.FinalizePlan);
                data.setStatusId(model.getStatusId());
                data.setFinalizedAt(Instant.now());
            }
        } else {
            data = new PlanEntity();
            data.setId(UUID.randomUUID());
            data.setGroupId(UUID.randomUUID());
            data.setVersion((short) 1);
            data.setStatusId(this.planWorkflowService.getActiveWorkFlowDefinition().getStartingStatusId());
            data.setVersionStatus(PlanVersionStatus.NotFinalized);
            data.setCreatorId(this.userScope.getUserId());
            data.setBlueprintId(model.getBlueprint());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }
//        PlanStatus previousStatus = data.getStatus();

        PlanBlueprintEntity planBlueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, model.getBlueprint(), true);
        if (planBlueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getBlueprint(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());

        data.setLabel(model.getLabel());
        data.setLanguage(model.getLanguage());
        data.setProperties(this.jsonHandlingService.toJson(this.buildPlanPropertiesEntity(model.getProperties(), definition)));
        data.setDescription(model.getDescription());
        data.setAccessType(model.getAccessType());
        data.setUpdatedAt(Instant.now());
        if (isUpdate)
            this.entityManager.merge(data);
        else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.PLAN_COUNT.getValue());
        }

        this.entityManager.flush();

//        this.updateVersionStatusAndSave(data, previousStatus, data.getStatus());

        this.entityManager.flush();

        return data;
    }

    private @NotNull PlanPropertiesEntity buildPlanPropertiesEntity(PlanPropertiesPersist persist, org.opencdmp.commons.types.planblueprint.DefinitionEntity definition){
        PlanPropertiesEntity data = new PlanPropertiesEntity();
        if (persist == null) return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getContacts())){
            data.setContacts(new ArrayList<>());
            for (PlanContactPersist contactPersist: persist.getContacts()) {
                data.getContacts().add(this.buildPlanContactEntity(contactPersist));
            }
        }
        if (persist.getPlanBlueprintValues() != null && !persist.getPlanBlueprintValues().isEmpty()){
            data.setPlanBlueprintValues(new ArrayList<>());
            for (PlanBlueprintValuePersist fieldValuePersist: persist.getPlanBlueprintValues().values()) {
                if (!this.conventionService.isNullOrEmpty(fieldValuePersist.getFieldValue()) || fieldValuePersist.getDateValue() != null || fieldValuePersist.getNumberValue() != null) data.getPlanBlueprintValues().add(this.buildPlanBlueprintValueEntity(fieldValuePersist, definition));
            }
        }
        return data;
    }

    private @NotNull PlanContactEntity buildPlanContactEntity(PlanContactPersist persist){
        PlanContactEntity data = new PlanContactEntity();
        if (persist == null) return data;

        data.setEmail(persist.getEmail());
        data.setLastName(persist.getLastName());
        data.setFirstName(persist.getFirstName());
        return data;
    }

    private @NotNull PlanBlueprintValueEntity buildPlanBlueprintValueEntity(PlanBlueprintValuePersist persist, org.opencdmp.commons.types.planblueprint.DefinitionEntity definition){
        PlanBlueprintValueEntity data = new PlanBlueprintValueEntity();
        if (persist == null || definition == null) return data;

        org.opencdmp.commons.types.planblueprint.FieldEntity fieldEntity = definition.getFieldById(persist.getFieldId()).stream().findFirst().orElse(null);
        if (fieldEntity == null) return data;

        if (fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.Extra)) {
            ExtraFieldEntity extraFieldEntity = (ExtraFieldEntity) fieldEntity;
	        if (PlanBlueprintExtraFieldDataType.isDateType(extraFieldEntity.getType())){
                data.setDateValue(persist.getDateValue());
            } else if (PlanBlueprintExtraFieldDataType.isNumberType(extraFieldEntity.getType())){
                data.setNumberValue(persist.getNumberValue());
            } else {
                data.setValue(persist.getFieldValue());
            }
            data.setFieldId(persist.getFieldId());
        }

        return data;
    }

    private @NotNull  List<PlanReferencePersist> buildPlanReferencePersists(PlanPropertiesPersist persist){
        List<PlanReferencePersist> planReferencePersists = new ArrayList<>();
        if (persist.getPlanBlueprintValues() != null && !persist.getPlanBlueprintValues().isEmpty()){
            for (PlanBlueprintValuePersist fieldValuePersist: persist.getPlanBlueprintValues().values()) {
                if (fieldValuePersist.getReference() != null) {
                    if (fieldValuePersist.getReferences() == null) fieldValuePersist.setReferences(new ArrayList<>());
                    fieldValuePersist.getReferences().add(fieldValuePersist.getReference());
                }
                if (this.conventionService.isNullOrEmpty(fieldValuePersist.getFieldValue()) && fieldValuePersist.getDateValue() == null && fieldValuePersist.getNumberValue() == null &&  !this.conventionService.isListNullOrEmpty( fieldValuePersist.getReferences())) {
                    for (ReferencePersist referencePersist : fieldValuePersist.getReferences()) {
                        PlanReferencePersist planReferencePersist = new PlanReferencePersist();
                        planReferencePersist.setData(new PlanReferenceDataPersist());
                        planReferencePersist.getData().setBlueprintFieldId(fieldValuePersist.getFieldId());
                        planReferencePersist.setReference(referencePersist);
                        planReferencePersists.add(planReferencePersist);
                    }
                }
            }
        }
        return planReferencePersists;
    }

    private void patchAndSaveReferences(List<PlanReferencePersist> models, UUID planId, org.opencdmp.commons.types.planblueprint.DefinitionEntity blueprintDefinition) throws InvalidApplicationException {
        if (models == null) models = new ArrayList<>();

        List<PlanReferenceEntity> planReferences = this.queryFactory.query(PlanReferenceQuery.class).planIds(planId).isActives(IsActive.Active).collect();
        Map<UUID, List<PlanReferenceEntity>> planReferenceEntityByReferenceId = new HashMap<>();
        for (PlanReferenceEntity planReferenceEntity : planReferences){
            List<PlanReferenceEntity> planReferenceEntities = planReferenceEntityByReferenceId.getOrDefault(planReferenceEntity.getReferenceId(), null);
            if (planReferenceEntities == null) {
                planReferenceEntities = new ArrayList<>();
                planReferenceEntityByReferenceId.put(planReferenceEntity.getReferenceId(), planReferenceEntities);
            }
            planReferenceEntities.add(planReferenceEntity);
        }
        
        Map<UUID, PlanReferenceDataEntity> planReferenceDataEntityMap = new HashMap<>();
        for (PlanReferenceEntity planReferenceEntity : planReferences){
            planReferenceDataEntityMap.put(planReferenceEntity.getId(), this.jsonHandlingService.fromJsonSafe(PlanReferenceDataEntity.class, planReferenceEntity.getData()));
        }
        
        List<UUID> updatedCreatedIds = new ArrayList<>();
        for (PlanReferencePersist model : models) {
            ReferencePersist referencePersist = model.getReference();
            ReferenceEntity referenceEntity;
            if (this.conventionService.isValidGuid(referencePersist.getId())){
                referenceEntity = this.entityManager.find(ReferenceEntity.class, referencePersist.getId());
                if (referenceEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{referencePersist.getId(), Reference.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            } else {
                ReferenceTypeFieldEntity fieldEntity = blueprintDefinition.getFieldById(model.getData().getBlueprintFieldId()).stream().filter(x-> x.getCategory().equals(PlanBlueprintFieldCategory.ReferenceType)).map(x-> (ReferenceTypeFieldEntity)x).findFirst().orElse(null);
                if (fieldEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getData().getBlueprintFieldId(), ReferenceTypeFieldEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                
                referenceEntity = this.queryFactory.query(ReferenceQuery.class).sourceTypes(referencePersist.getSourceType()).typeIds(fieldEntity.getReferenceTypeId()).sources(referencePersist.getSource()).isActive(IsActive.Active).references(referencePersist.getReference()).first();
                if (referenceEntity == null){
                    referenceEntity = new ReferenceEntity();
                    referenceEntity.setId(UUID.randomUUID());
                    referenceEntity.setLabel(referencePersist.getLabel());
                    referenceEntity.setDescription(referencePersist.getDescription());
                    referenceEntity.setIsActive(IsActive.Active);
                    referenceEntity.setCreatedAt(Instant.now());
                    referenceEntity.setTypeId(fieldEntity.getReferenceTypeId());

                    referenceEntity.setDefinition(this.xmlHandlingService.toXmlSafe(this.buildDefinitionEntity(referencePersist.getDefinition())));
                    referenceEntity.setUpdatedAt(Instant.now());
                    referenceEntity.setReference(referencePersist.getReference());
                    referenceEntity.setAbbreviation(referencePersist.getAbbreviation());
                    referenceEntity.setSource(referencePersist.getSource());
                    referenceEntity.setSourceType(referencePersist.getSourceType());
                    try {
                        ReferenceTypeEntity referenceType = this.queryFactory.query(ReferenceTypeQuery.class).ids(fieldEntity.getReferenceTypeId()).firstAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceTypeEntity._tenantId));
                        if (referenceType == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{fieldEntity.getReferenceTypeId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                        if (referenceEntity.getSourceType().equals(ReferenceSourceType.External) && !this.tenantScope.isDefaultTenant() && referenceType.getTenantId() == null){
                            this.tenantScope.setTempTenant(this.entityManager, null, this.tenantScope.getDefaultTenantCode());
                        }
                        this.entityManager.persist(referenceEntity);
                    } finally {
	                    this.tenantScope.removeTempTenant(this.entityManager);
                    }
                }
            }

            PlanReferenceEntity data = null;
            List<PlanReferenceEntity> planReferenceEntities = planReferenceEntityByReferenceId.getOrDefault(referenceEntity.getId(), new ArrayList<>());
            for (PlanReferenceEntity planReferenceEntity : planReferenceEntities){
                PlanReferenceDataEntity planReferenceDataEntity = planReferenceDataEntityMap.getOrDefault(planReferenceEntity.getId(), new PlanReferenceDataEntity());
                if (Objects.equals(planReferenceDataEntity.getBlueprintFieldId(), model.getData().getBlueprintFieldId())){
                    data = planReferenceEntity;
                    break;
                }
            }
            boolean isUpdate = data != null;

            if (!isUpdate) {
                data = new PlanReferenceEntity();
                data.setId(UUID.randomUUID());
                data.setReferenceId(referenceEntity.getId());
                data.setPlanId(planId);
                data.setCreatedAt(Instant.now());
                data.setIsActive(IsActive.Active);
                data.setData(this.jsonHandlingService.toJsonSafe(this.buildPlanReferenceDataEntity(model.getData())));
            }
            updatedCreatedIds.add(data.getId());

            data.setUpdatedAt(Instant.now());

            if (isUpdate) this.entityManager.merge(data);
            else this.entityManager.persist(data);
        }
        List<PlanReferenceEntity> toDelete = planReferences.stream().filter(x-> updatedCreatedIds.stream().noneMatch(y-> y.equals(x.getId()))).collect(Collectors.toList());
        this.deleterFactory.deleter(PlanReferenceDeleter.class).delete(toDelete);
        this.entityManager.flush();
    }

    private void patchAndSaveTemplates(UUID id, List<PlanDescriptionTemplatePersist> models) throws InvalidApplicationException {
        if (models == null) models = new ArrayList<>();
        List<PlanDescriptionTemplateEntity> items = this.queryFactory.query(PlanDescriptionTemplateQuery.class).isActive(IsActive.Active).planIds(id).collect();
        List<UUID> updatedCreatedIds = new ArrayList<>();
        for (PlanDescriptionTemplatePersist model : models) {
            PlanDescriptionTemplateEntity data = items.stream().filter(x -> x.getDescriptionTemplateGroupId().equals(model.getDescriptionTemplateGroupId()) && x.getSectionId().equals(model.getSectionId())).findFirst().orElse(null);
            if (data == null){
                data = new PlanDescriptionTemplateEntity();
                data.setId(UUID.randomUUID());
                data.setIsActive(IsActive.Active);
                data.setCreatedAt(Instant.now());
                data.setUpdatedAt(Instant.now());
                data.setPlanId(id);
                data.setSectionId(model.getSectionId());
                data.setDescriptionTemplateGroupId(model.getDescriptionTemplateGroupId());
                this.entityManager.persist(data);
            }
            updatedCreatedIds.add(data.getId());
        }
        List<PlanDescriptionTemplateEntity> toDelete = items.stream().filter(x-> updatedCreatedIds.stream().noneMatch(y-> y.equals(x.getId()))).collect(Collectors.toList());

        this.deleterFactory.deleter(PlanDescriptionTemplateDeleter.class).delete(toDelete);
    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist){
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null) return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getFields())){
            data.setFields(new ArrayList<>());
            for (FieldPersist fieldPersist: persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }

        return data;
    }

    private @NotNull PlanReferenceDataEntity buildPlanReferenceDataEntity(PlanReferenceDataPersist persist){
        PlanReferenceDataEntity data = new PlanReferenceDataEntity();
        if (persist == null) return data;
        data.setBlueprintFieldId(persist.getBlueprintFieldId());
        return data;
    }

    private @NotNull FieldEntity buildFieldEntity(FieldPersist persist){
        FieldEntity data = new FieldEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setDataType(persist.getDataType());
        data.setValue(persist.getValue());

        return data;
    }

    public void setStatus(UUID id, UUID newStatusId, List<UUID> descriptionIds) throws InvalidApplicationException, IOException {
        PlanEntity plan = this.queryFactory.query(PlanQuery.class).authorize(AuthorizationFlags.AllExceptPublic).ids(id).isActive(IsActive.Active).first();
        if (plan == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(id)), Permission.EditPlan, this.customPolicyService.getPlanStatusCanEditStatusPermission(newStatusId));

        if (this.lockService.isLocked(plan.getId(), null).getStatus()) throw new MyApplicationException(this.errors.getLockedPlan().getCode(), this.errors.getLockedPlan().getMessage());

        List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class).authorize(AuthorizationFlags.AllExceptPublic).planIds(plan.getId()).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._label));
        if (descriptions == null) throw new MyApplicationException("Descriptions not found ");
        for (DescriptionEntity description: descriptions)
            if (this.lockService.isLocked(description.getId(), null).getStatus()) throw new MyApplicationException(this.errors.getLockedDescription().getCode(), description.getLabel());

        EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).types(EntityType.Plan).entityIds(plan.getId()).isActive(IsActive.Active);
        if (entityDoiQuery.count() > 0) throw new MyApplicationException("Plan is deposited");

        if (plan.getStatusId().equals(newStatusId)) throw new MyApplicationException("Old status equals with new");

        PlanStatusEntity oldPlanStatusEntity = this.entityManager.find(PlanStatusEntity.class, plan.getStatusId(), true);
        if (oldPlanStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{plan.getStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanStatusEntity newPlanStatusEntity = this.entityManager.find(PlanStatusEntity.class, newStatusId, true);
        if (newPlanStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{newStatusId, PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (oldPlanStatusEntity.getInternalStatus() != null && oldPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized)) {
            this.undoFinalize(plan, oldPlanStatusEntity, newPlanStatusEntity);
        } else if (newPlanStatusEntity.getInternalStatus() != null && newPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized)) {
            this.finalize(plan, descriptionIds ,oldPlanStatusEntity, newPlanStatusEntity);
        } else {
            plan.setStatusId(newPlanStatusEntity.getId());
            plan.setUpdatedAt(Instant.now());

            this.entityManager.merge(plan);
            this.entityManager.flush();
            this.sendNotification(plan);
        }
    }

    private void finalize(PlanEntity plan, List<UUID> descriptionIds, PlanStatusEntity oldPlanStatusEntity, PlanStatusEntity newPlanStatusEntity) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(plan.getId())), Permission.FinalizePlan, this.customPolicyService.getPlanStatusCanEditStatusPermission(newPlanStatusEntity.getId()));

        if (oldPlanStatusEntity.getInternalStatus() != null && oldPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized)){
            throw new MyApplicationException("Plan is already finalized");
        }

        if (this.validate(plan.getId()).getResult().equals(PlanValidationOutput.Invalid)){
            throw new MyApplicationException("Plan is invalid");
        }

        List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class)
                .authorize(AuthorizationFlags.AllExceptPublic).planIds(plan.getId()).isActive(IsActive.Active).collect();

        if (!this.conventionService.isListNullOrEmpty(descriptions)) {
            List<DescriptionStatusEntity> statusEntities = this.queryFactory.query(DescriptionStatusQuery.class).authorize(AuthorizationFlags.AllExceptPublic).ids(descriptions.stream().map(DescriptionEntity::getStatusId).distinct().toList()).isActive(IsActive.Active).collect();

            if (this.conventionService.isListNullOrEmpty(statusEntities)) throw new MyApplicationException("Not found description statuses");
            DescriptionStatusEntity descriptionFinalizedStatusEntity = this.findDescriptionStatus(DescriptionStatus.Finalized);
            if (descriptionFinalizedStatusEntity == null) throw new MyApplicationException("finalized status not found");

            DescriptionStatusEntity canceledStatusEntity = this.findDescriptionStatus(DescriptionStatus.Canceled);
            if (canceledStatusEntity == null) throw new MyApplicationException("canceled status not found");

            for (DescriptionEntity description: descriptions) {
                DescriptionStatusEntity currentStatusEntity = statusEntities.stream().filter(x -> x.getId().equals(description.getStatusId())).findFirst().orElse(null);
                if (descriptionIds.contains(description.getId())){
                    // description to be finalized
                    if (currentStatusEntity != null && currentStatusEntity.getInternalStatus()!= null && currentStatusEntity.getInternalStatus().equals(DescriptionStatus.Finalized)){
                        throw new MyApplicationException("Description is already finalized");
                     }
                    if (this.descriptionService.validate(List.of(description.getId())).getFirst().getResult().equals(DescriptionValidationOutput.Invalid)){
                        throw new MyApplicationException("Description is invalid");
                    }
                    description.setStatusId(descriptionFinalizedStatusEntity.getId());
                    description.setUpdatedAt(Instant.now());
                    description.setFinalizedAt(Instant.now());
                    this.entityManager.merge(description);
                } else if (currentStatusEntity != null && currentStatusEntity.getInternalStatus()!= null && !currentStatusEntity.getInternalStatus().equals(DescriptionStatus.Finalized)) {
                    // description to be canceled
                    description.setStatusId(canceledStatusEntity.getId());
                    this.deleterFactory.deleter(DescriptionDeleter.class).delete(List.of(description), false);
                }
            }
        }

        plan.setStatusId(newPlanStatusEntity.getId());
        plan.setUpdatedAt(Instant.now());
        plan.setFinalizedAt(Instant.now());

        this.updateVersionStatusAndSave(plan, oldPlanStatusEntity.getInternalStatus(), newPlanStatusEntity.getInternalStatus());
        plan.setVersionStatus(PlanVersionStatus.Current);

        this.entityManager.merge(plan);
        this.entityManager.flush();
        
        this.elasticService.persistPlan(plan);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(plan.getId());
        this.sendNotification(plan);
    }

    private DescriptionStatusEntity findDescriptionStatus(DescriptionStatus internalStatus){
        DescriptionStatusEntity status;

        DescriptionStatusQuery descriptionStatusQuery = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().internalStatuses(internalStatus).isActive(IsActive.Active);
        descriptionStatusQuery.setOrder(new Ordering().addAscending(org.opencdmp.model.descriptionstatus.DescriptionStatus._ordinal));
        List<DescriptionStatusEntity> descriptionStatusEntities = descriptionStatusQuery.collectAs(new BaseFieldSet().ensure(org.opencdmp.model.descriptionstatus.DescriptionStatus._id).ensure(org.opencdmp.model.descriptionstatus.DescriptionStatus._belongsToCurrentTenant).ensure(org.opencdmp.model.descriptionstatus.DescriptionStatus._ordinal));
        if (this.conventionService.isListNullOrEmpty(descriptionStatusEntities)) throw new MyApplicationException("status not found");
        if (!this.tenantScope.isDefaultTenant()) {
            status = descriptionStatusEntities.stream().filter(x -> {
                try {
                    return this.tenantScope.getTenant().equals(x.getTenantId());
                } catch (InvalidApplicationException e) {
                    throw new RuntimeException(e);
                }
            }).findFirst().orElse(null);
            if (status == null) status = descriptionStatusEntities.getFirst();
        } else {
            status = descriptionStatusEntities.getFirst();
        }
        return status;
    }

    private void undoFinalize(PlanEntity plan, PlanStatusEntity oldPlanStatusEntity, PlanStatusEntity newPlanStatusEntity) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(plan.getId())), Permission.UndoFinalizePlan, this.customPolicyService.getPlanStatusCanEditStatusPermission(newPlanStatusEntity.getId()));

        if (oldPlanStatusEntity.getInternalStatus() == null && !oldPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized)) throw new MyApplicationException("Plan is already non finalized");

        plan.setStatusId(newPlanStatusEntity.getId());
        plan.setUpdatedAt(Instant.now());

        this.entityManager.merge(plan);
        this.entityManager.flush();

        this.updateVersionStatusAndSave(plan, PlanStatus.Finalized, newPlanStatusEntity.getInternalStatus());
        this.entityManager.flush();

        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking()
                .versionStatuses(PlanVersionStatus.Previous)
                .excludedIds(plan.getId())
                .isActive(IsActive.Active)
                .groupIds(plan.getGroupId());

        planQuery.setOrder(new Ordering().addDescending(Plan._version));
        PlanEntity previousPlan = planQuery.count() > 0 ? planQuery.collect().getFirst() : null;
        if (previousPlan != null){
            PlanStatusEntity previousPlanStatusEntity = this.entityManager.find(PlanStatusEntity.class, previousPlan.getStatusId(), true);
            if (previousPlanStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{previousPlan.getStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            if (previousPlanStatusEntity.getInternalStatus() != null && previousPlanStatusEntity.getInternalStatus().equals(PlanStatus.Finalized)) previousPlan.setVersionStatus(PlanVersionStatus.Current);
            else previousPlan.setVersionStatus(PlanVersionStatus.NotFinalized);
            this.entityManager.merge(previousPlan);
        }
        this.entityManager.flush();

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(plan.getId());
        this.sendNotification(plan);
    }

    public PlanValidationResult validate(UUID id) throws InvalidApplicationException {

        PlanEntity plan = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id).isActive(IsActive.Active).first();

        if (plan == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanValidationResult planValidationResult = new PlanValidationResult(plan.getId(), PlanValidationOutput.Invalid);

        PlanPersist.PlanPersistValidator validator = this.validatorFactory.validator(PlanPersist.PlanPersistValidator.class);
        validator.validate(this.buildPlanPersist(plan));
        if (validator.result().isValid()) planValidationResult.setResult(PlanValidationOutput.Valid);
        else planValidationResult.setErrors(validator.result().getErrors().stream().map(ValidationFailure::getErrorMessage).collect(Collectors.toList()));

        return planValidationResult;
    }

    // build persist

    private @NotNull PlanPersist buildPlanPersist(PlanEntity data) throws InvalidApplicationException {
        PlanPersist persist = new PlanPersist();
        if (data == null) return persist;

        PlanBlueprintEntity planBlueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, data.getBlueprintId(), true);
        if (planBlueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        persist.setId(data.getId());
        persist.setHash(data.getId().toString());
        persist.setLabel(data.getLabel());
        PlanStatusQuery planStatusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
        planStatusQuery.setOrder(new Ordering().addAscending(org.opencdmp.model.planstatus.PlanStatus._ordinal));
        PlanStatusEntity statusEntity = planStatusQuery.firstAs(new BaseFieldSet().ensure(org.opencdmp.model.planstatus.PlanStatus._id));
        if (statusEntity != null) persist.setStatusId(statusEntity.getId());
        persist.setDescription(data.getDescription());
        persist.setBlueprint(data.getBlueprintId());
        persist.setAccessType(data.getAccessType());
        persist.setLanguage(data.getLanguage());

        List<PlanUserEntity> planUserEntities = this.queryFactory.query(PlanUserQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).planIds(data.getId()).isActives(IsActive.Active).collect();

        if (!this.conventionService.isListNullOrEmpty(planUserEntities)){
            persist.setUsers(new ArrayList<>());
            for (PlanUserEntity user: planUserEntities) {
                persist.getUsers().add(this.buildPlanUserPersist(user));
            }
        }

	    List<PlanReferenceEntity> planReferenceEntities = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).planIds(data.getId()).isActives(IsActive.Active).collect();

        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());

        List<PlanDescriptionTemplateEntity> planDescriptionTemplateEntities = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).planIds(data.getId()).isActive(IsActive.Active).collect();
        if (!this.conventionService.isListNullOrEmpty(planDescriptionTemplateEntities)){
            persist.setDescriptionTemplates(new ArrayList<>());
            for (PlanDescriptionTemplateEntity descriptionTemplateEntity: planDescriptionTemplateEntities) {
                persist.getDescriptionTemplates().add(this.buildPlanDescriptionTemplatePersists(descriptionTemplateEntity));
            }
        }
	    persist.setProperties(this.buildPlanPropertyDefinitionPersist( this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, data.getProperties()), planReferenceEntities, definition.getSections()));

        return persist;
    }

    private @NotNull PlanPropertiesPersist buildPlanPropertyDefinitionPersist(PlanPropertiesEntity data, List<PlanReferenceEntity> planReferenceEntities, List<SectionEntity> sectionEntities){
        PlanPropertiesPersist persist = new PlanPropertiesPersist();
        if (data == null) return persist;
        if (!this.conventionService.isListNullOrEmpty(data.getContacts())){
            persist.setContacts(new ArrayList<>());
            for (PlanContactEntity contact: data.getContacts()) {
                persist.getContacts().add(this.buildPlanContactPersist(contact));
            }
        }

        List<ReferenceEntity> referencesFromAllFields = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(planReferenceEntities)) {
            referencesFromAllFields = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(planReferenceEntities.stream().map(PlanReferenceEntity::getReferenceId).collect(Collectors.toList())).isActive(IsActive.Active).collect();
        }

        Map<UUID, PlanBlueprintValuePersist> planBlueprintValues = new HashMap<>();
        if (!this.conventionService.isListNullOrEmpty(sectionEntities)){
            for (SectionEntity sectionEntity: sectionEntities) {
                if (!this.conventionService.isListNullOrEmpty(sectionEntity.getFields())){
                    for (org.opencdmp.commons.types.planblueprint.FieldEntity fieldEntity: sectionEntity.getFields()) {

                        if (!this.conventionService.isListNullOrEmpty(planReferenceEntities) && fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.ReferenceType)) {
                            List<ReferencePersist> referencePersists = new ArrayList<>();
                            for (PlanReferenceEntity planReferenceEntity : planReferenceEntities) {
                                PlanReferenceData referenceData = this.jsonHandlingService.fromJsonSafe(PlanReferenceData.class, planReferenceEntity.getData());

                                ReferenceEntity reference = referencesFromAllFields.stream().filter(x -> x.getId().equals(planReferenceEntity.getReferenceId())).toList().getFirst();
                                if (referenceData.getBlueprintFieldId().equals(fieldEntity.getId()) && reference != null) {
                                    referencePersists.add(this.buildReferencePersist(reference));
                                }
                                // put references
                                planBlueprintValues.put(fieldEntity.getId(), this.buildPlanBlueprintValuePersist(fieldEntity.getId(), null, null, null, referencePersists, ((ReferenceTypeFieldEntity)fieldEntity).getMultipleSelect()));
                            }
                        } else if (!this.conventionService.isListNullOrEmpty(data.getPlanBlueprintValues())) {
                            for (PlanBlueprintValueEntity value : data.getPlanBlueprintValues()) {
                                if (value.getFieldId().equals(fieldEntity.getId())) {
                                    if (value.getDateValue() != null) {
                                        planBlueprintValues.put(fieldEntity.getId(), this.buildPlanBlueprintValuePersist(fieldEntity.getId(), null, value.getDateValue(), null, null,null));
                                    } else if (value.getNumberValue() != null) {
                                        planBlueprintValues.put(fieldEntity.getId(), this.buildPlanBlueprintValuePersist(fieldEntity.getId(), null, null, value.getNumberValue(), null,null));
                                    } else if (!this.conventionService.isNullOrEmpty(value.getValue())) {
                                        planBlueprintValues.put(fieldEntity.getId(), this.buildPlanBlueprintValuePersist(fieldEntity.getId(), value.getValue(), null,null,null,null));
                                    } else {
                                        planBlueprintValues.put(fieldEntity.getId(), null);
                                    }
                                }
                            }
                        }

                        // fill fields with no values
                        if (planBlueprintValues.get(fieldEntity.getId()) == null){
                            planBlueprintValues.put(fieldEntity.getId(), this.buildPlanBlueprintValuePersist(fieldEntity.getId(), null, null, null,null,null));
                        }

                    }
                }

            }
            persist.setPlanBlueprintValues(planBlueprintValues);
        }

        return persist;
    }

    private @NotNull PlanContactPersist buildPlanContactPersist(PlanContactEntity data){
        PlanContactPersist persist = new PlanContactPersist();
        if (data == null) return persist;

        persist.setEmail(data.getEmail());
        persist.setLastName(data.getLastName());
        persist.setFirstName(data.getFirstName());
        return persist;
    }

    private @NotNull PlanUserPersist buildPlanUserPersist(PlanUserEntity data){
        PlanUserPersist persist = new PlanUserPersist();
        if (data == null) return persist;

        persist.setUser(data.getUserId());
        persist.setSectionId(data.getSectionId());
        persist.setRole(data.getRole());

        return persist;
    }

    private @NotNull PlanDescriptionTemplatePersist buildPlanDescriptionTemplatePersists(PlanDescriptionTemplateEntity data){
        PlanDescriptionTemplatePersist persist = new PlanDescriptionTemplatePersist();
        if (data == null) return persist;

        persist.setSectionId(data.getSectionId());
        persist.setDescriptionTemplateGroupId(data.getDescriptionTemplateGroupId());

        return persist;
    }

    private @NotNull ReferencePersist buildReferencePersist(ReferenceEntity data){
        ReferencePersist persist = new ReferencePersist();
        if (data == null) return persist;

        persist.setLabel(data.getLabel());
        persist.setReference(data.getReference());
        persist.setAbbreviation(data.getAbbreviation());
        persist.setDescription(data.getDescription());
        persist.setSource(data.getSource());
        persist.setSourceType(data.getSourceType());
        persist.setReference(data.getReference());
        persist.setTypeId(data.getTypeId());

        return persist;
    }

    private @NotNull PlanBlueprintValuePersist buildPlanBlueprintValuePersist(UUID fieldId, String fieldValue, Instant dateValue, Double numberValue, List<ReferencePersist> referencePersists, Boolean multipleSelect){
        PlanBlueprintValuePersist persist = new PlanBlueprintValuePersist();

        persist.setFieldId(fieldId);

        if (!this.conventionService.isListNullOrEmpty(referencePersists) && multipleSelect){
            persist.setReferences(referencePersists);
        }else if (!this.conventionService.isListNullOrEmpty(referencePersists)){
            persist.setReference(referencePersists.getFirst());
        }else if (!this.conventionService.isNullOrEmpty(fieldValue)){
            persist.setFieldValue(fieldValue);
        } else if (numberValue != null){
            persist.setNumberValue(numberValue);
        } else if (dateValue != null){
            persist.setDateValue(dateValue);
        }

        return persist;
    }

    //  invites
    public void inviteUserOrAssignUsers(UUID id, List<PlanUserPersist> users) throws InvalidApplicationException, JAXBException, IOException {
       this.inviteUserOrAssignUsers(id, users, true);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(id);
    }



    private List<PlanUserPersist>  inviteUserOrAssignUsers(UUID id, List<PlanUserPersist> users, boolean persistUsers) throws InvalidApplicationException, JAXBException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(id)), Permission.InvitePlanUsers);

        PlanEntity plan = this.queryFactory.query(PlanQuery.class).disableTracking().ids(id).first();
        if (plan == null){
            throw new InvalidApplicationException("Plan does not exist!");
        }

        List<PlanUserEntity> existingUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking()
                .planIds(plan.getId())
                .isActives(IsActive.Active)
                .collect();

        if (this.conventionService.isListNullOrEmpty(existingUsers)){
            throw new InvalidApplicationException("Plan does not have users!");
        }

        List<PlanUserPersist> usersToAssign = new ArrayList<>();
        for (PlanUserPersist user :users) {
            UUID userId = null;
            if (user.getUser() != null){
                userId = user.getUser();
            } else if (user.getEmail() != null) {
                UserContactInfoEntity contactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().values(user.getEmail()).types(ContactInfoType.Email).first();
                if (contactInfoEntity != null){
                    userId = contactInfoEntity.getUserId();
                }
            }
            if (userId != null){
                user.setUser(userId);
                usersToAssign.add(user);
                if (this.userScope.getUserId() != userId && !existingUsers.stream().map(PlanUserEntity::getUserId).toList().contains(userId)){
                    this.sendPlanInvitationExistingUser(user.getUser(), plan, user.getRole());
                }
            }else if (user.getEmail() != null) {
                this.sendPlanInvitationExternalUser(user.getEmail(),plan, user.getRole(), user.getSectionId());
            }

        }
        if(!usersToAssign.isEmpty() && persistUsers) this.assignUsers(id, usersToAssign, null, true);
        return usersToAssign;
    }

    private void sendPlanInvitationExistingUser(UUID userId, PlanEntity plan, PlanUserRole role) throws InvalidApplicationException {
        UserEntity recipient = this.queryFactory.query(UserQuery.class).disableTracking().ids(userId).isActive(IsActive.Active).first();
        if (recipient == null) throw new MyValidationException(this.errors.getPlanInactiveUser().getCode(), this.errors.getPlanInactiveUser().getMessage());
        String email = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().userIds(recipient.getId()).first().getValue();
        this.createPlanInvitationExistingUserEvent(recipient, plan, role, email);
    }

    private void createPlanInvitationExistingUserEvent(UserEntity recipient, PlanEntity plan, PlanUserRole role, String email) throws InvalidApplicationException {

        if (email == null) throw new MyValidationException(this.errors.getPlanMissingUserContactInfo().getCode(), this.errors.getPlanMissingUserContactInfo().getMessage());

        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(recipient.getId());

        event.setNotificationType(this.notificationProperties.getPlanInvitationExistingUserType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, recipient.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScope.getUserIdSafe()).first().getName()));
        fieldInfoList.add(new FieldInfo("{planname}", DataType.String, plan.getLabel()));
        fieldInfoList.add(new FieldInfo("{planrole}", DataType.String, role.toString()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, plan.getId().toString()));
        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));
	    this.eventHandler.handle(event);
    }

    private void sendPlanInvitationExternalUser(String email, PlanEntity plan, PlanUserRole role, UUID sectionId) throws JAXBException, InvalidApplicationException {
        String token = this.createActionConfirmation(email, plan, role, sectionId);

        NotifyIntegrationEvent event = new NotifyIntegrationEvent();

        List<ContactPair> contactPairs = new ArrayList<>();
        contactPairs.add(new ContactPair(ContactInfoType.Email, email));
        NotificationContactData contactData = new NotificationContactData(contactPairs, null, null);
        event.setContactHint(this.jsonHandlingService.toJsonSafe(contactData));
        event.setContactTypeHint(NotificationContactType.EMAIL);
        event.setNotificationType(this.notificationProperties.getPlanInvitationExternalUserType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, email));
        fieldInfoList.add(new FieldInfo("{confirmationToken}", DataType.String, token));
        fieldInfoList.add(new FieldInfo("{planname}", DataType.String, plan.getLabel()));
        fieldInfoList.add(new FieldInfo("{planrole}", DataType.String, role.toString()));
        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));
	    this.eventHandler.handle(event);
    }

    private String createActionConfirmation(String email, PlanEntity plan, PlanUserRole role, UUID sectionId) throws JAXBException, InvalidApplicationException {
        ActionConfirmationPersist persist = new ActionConfirmationPersist();
        persist.setType(ActionConfirmationType.PlanInvitation);
        persist.setStatus(ActionConfirmationStatus.Requested);
        persist.setToken(UUID.randomUUID().toString());
        persist.setPlanInvitation(new PlanInvitationPersist(email, plan.getId(), sectionId, role));
        persist.setExpiresAt(Instant.now().plusSeconds(this.usersProperties.getEmailExpirationTimeSeconds().getPlanInvitationExternalUserExpiration()));
	    this.validatorFactory.validator(ActionConfirmationPersist.ActionConfirmationPersistValidator.class).validateForce(persist);
        this.actionConfirmationService.persist(persist, null);

        return persist.getToken();
    }

    public PlanInvitationResult planInvitationAccept(String token) throws InvalidApplicationException {
        PlanInvitationResult planInvitationResult = new PlanInvitationResult();
        ActionConfirmationEntity action = this.queryFactory.query(ActionConfirmationQuery.class).tokens(token).types(ActionConfirmationType.PlanInvitation).isActive(IsActive.Active).first();

        if (action == null){
            throw new MyValidationException(this.errors.getTokenNotExist().getCode(), this.errors.getTokenNotExist().getMessage());
        }
        PlanInvitationEntity planInvitation = this.xmlHandlingService.fromXmlSafe(PlanInvitationEntity.class, action.getData());
        if (planInvitation == null) {
            throw new MyApplicationException("plan invitation don't exist");
        }
        if (action.getStatus().equals(ActionConfirmationStatus.Accepted)){
            UserContactInfoEntity contactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().userIds(this.userScope.getUserId()).values(planInvitation.getEmail()).types(ContactInfoType.Email).first();
            if (contactInfoEntity == null){
                throw new MyValidationException(this.errors.getAnotherUserToken().getCode(), this.errors.getAnotherUserToken().getMessage());
            }
            planInvitationResult.setPlanId(planInvitation.getPlanId());
            planInvitationResult.setIsAlreadyAccepted(true);
            return planInvitationResult;
        }
        if (action.getExpiresAt().compareTo(Instant.now()) < 0){
            throw new MyValidationException(this.errors.getRequestHasExpired().getCode(), this.errors.getRequestHasExpired().getMessage());
        }

        UserContactInfoEntity contactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().userIds(this.userScope.getUserId()).values(planInvitation.getEmail()).types(ContactInfoType.Email).first();
        if (contactInfoEntity == null){
            throw new MyValidationException(this.errors.getAnotherUserToken().getCode(), this.errors.getAnotherUserToken().getMessage());
        }
        PlanUserEntity data = new PlanUserEntity();
        data.setId(UUID.randomUUID());
        data.setIsActive(IsActive.Active);
        data.setCreatedAt(Instant.now());
        data.setUpdatedAt(Instant.now());
        data.setRole(planInvitation.getRole());
        data.setUserId(this.userScope.getUserIdSafe());
        data.setPlanId(planInvitation.getPlanId());
        data.setSectionId(planInvitation.getSectionId());
        this.entityManager.persist(data);

        action.setStatus(ActionConfirmationStatus.Accepted);
        this.entityManager.merge(action);

        this.annotationEntityTouchedIntegrationEventHandler.handlePlan(planInvitation.getPlanId());
        planInvitationResult.setPlanId(planInvitation.getPlanId());
        planInvitationResult.setIsAlreadyAccepted(false);
        return planInvitationResult;
    }

    //region Export

    @Override
    public PlanImportExport exportXmlEntity(UUID id, boolean ignoreAuthorize, boolean isPublic) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export xml").And("id", id));

        if (!ignoreAuthorize) this.authorizationService.authorizeForce(Permission.ExportPlan);
        PlanEntity data = null;
        if (!isPublic) {
            data = this.queryFactory.query(PlanQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.All).isActive(IsActive.Active).first();
        } else {
            try {
                this.entityManager.disableTenantFilters();
                PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
                data = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(id).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public).first();
                this.entityManager.reloadTenantFilters();
            } finally {
                this.entityManager.reloadTenantFilters();
            }
        }
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanPropertiesEntity definition = this.jsonHandlingService.fromJson(PlanPropertiesEntity.class, data.getProperties());
        return this.definitionXmlToExport(data, definition, isPublic);
    }

    @Override
    public ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export xml").And("id", id));

        this.authorizationService.authorizeForce(Permission.ExportPlan);
        PlanEntity data = this.queryFactory.query(PlanQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.All).isActive(IsActive.Active).first();
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String xml = this.xmlHandlingService.toXml(this.exportXmlEntity(data.getId(), false, false));
        this.accountingService.increase(UsageLimitTargetMetric.EXPORT_PLAN_XML_EXECUTION_COUNT.getValue());
        return this.responseUtilsService.buildResponseFileFromText(xml, data.getLabel() + ".xml");
    }

    @Override
    public ResponseEntity<byte[]> exportPublicXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export public xml").And("id", id));

        this.authorizationService.authorizeForce(Permission.ExportPlan);

        PlanEntity data = null;
        try {
            this.entityManager.disableTenantFilters();
            PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
            data = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(id).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public).first();
            this.entityManager.reloadTenantFilters();
        } finally {
            this.entityManager.reloadTenantFilters();
        }

        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PublicPlan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String xml = this.xmlHandlingService.toXml(this.exportXmlEntity(data.getId(), false, true));
        this.accountingService.increase(UsageLimitTargetMetric.EXPORT_PLAN_XML_EXECUTION_COUNT.getValue());
        return this.responseUtilsService.buildResponseFileFromText(xml, data.getLabel() + ".xml");
    }
    
    private PlanImportExport definitionXmlToExport(PlanEntity data, PlanPropertiesEntity propertiesEntity, Boolean isPublic) throws InvalidApplicationException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        PlanBlueprintEntity blueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(data.getBlueprintId()).authorize(AuthorizationFlags.All).first();
        if (blueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        PlanImportExport xml = new PlanImportExport();
        xml.setId(data.getId());
        xml.setDescription(data.getDescription());
        xml.setTitle(data.getLabel());
        xml.setLanguage(data.getLanguage());
        xml.setAccess(data.getAccessType());
        xml.setStatus(this.planStatusImportExportToExport(data.getStatusId()));
        xml.setFinalizedAt(data.getFinalizedAt());
        xml.setPublicAfter(data.getPublicAfter());
        xml.setVersion(data.getVersion());
        xml.setContacts(this.planContactsToExport(propertiesEntity));
        xml.setUsers(this.planUsersToExport(data));
        xml.setBlueprint(this.planBlueprintService.getExportXmlEntity(blueprintEntity.getId(), true));
        xml.setDescriptionTemplates(this.planDescriptionTemplatesToExport(data));
        xml.setBlueprintValues(this.planBlueprintValuesToExport(propertiesEntity, blueprintEntity));
        xml.setReferences(this.planReferencesToExport(data));
        xml.setDescriptions(this.descriptionsToExport(data, isPublic));
        
        return xml;
    }

    private PlanStatusImportExport planStatusImportExportToExport(UUID statusId) throws InvalidApplicationException {
        PlanStatusImportExport xml = new PlanStatusImportExport();
        if (statusId == null) return xml;
        PlanStatusEntity planStatusEntity = this.entityManager.find(PlanStatusEntity.class, statusId, true);
        if (planStatusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{statusId, PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        xml.setId(planStatusEntity.getId());
        xml.setName(planStatusEntity.getName());
        return xml;
    }
    
    private List<DescriptionImportExport> descriptionsToExport(PlanEntity data, Boolean isPublic) throws JAXBException, InvalidApplicationException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        List<DescriptionEntity> descriptions;
        if (!isPublic) {
            descriptions = this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(data.getId()).authorize(AuthorizationFlags.All).planIds(data.getId()).isActive(IsActive.Active).collect();
        } else {
            try {
                this.entityManager.disableTenantFilters();
                descriptions = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(Public)).planIds(data.getId()).collect();
                this.entityManager.reloadTenantFilters();
            } finally {
                this.entityManager.reloadTenantFilters();
            }
        }
        if (!this.conventionService.isListNullOrEmpty(descriptions)) {
            List<DescriptionImportExport> descriptionImportExports = new LinkedList<>();
            for (DescriptionEntity description : descriptions) {
                descriptionImportExports.add(this.descriptionService.exportXmlEntity(description.getId(), true, isPublic));
            }
            return descriptionImportExports;
        }
        return null;
    }

    private List<PlanReferenceImportExport> planReferencesToExport(PlanEntity data){
        List<PlanReferenceEntity> planReferences = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().planIds(data.getId()).authorize(AuthorizationFlags.All).isActives(IsActive.Active).collect();
        if (!this.conventionService.isListNullOrEmpty(planReferences)) {
            List<ReferenceEntity> references = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(planReferences.stream().map(PlanReferenceEntity::getReferenceId).distinct().toList()).authorize(AuthorizationFlags.All).isActive(IsActive.Active).collect();
            Map<UUID, ReferenceEntity> referenceEntityMap = references == null ? new HashMap<>() : references.stream().collect(Collectors.toMap(ReferenceEntity::getId, x-> x));
            List<ReferenceTypeEntity> referenceTypes = references == null ? new ArrayList<>() : this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(references.stream().map(ReferenceEntity::getTypeId).distinct().toList()).authorize(AuthorizationFlags.AllExceptPublic).isActive(IsActive.Active).collect();
            Map<UUID, ReferenceTypeEntity> referenceTypeEntityMap = referenceTypes == null ? new HashMap<>() : referenceTypes.stream().collect(Collectors.toMap(ReferenceTypeEntity::getId, x-> x));
            List<PlanReferenceImportExport> planReferenceImportExports = new LinkedList<>();
            for (PlanReferenceEntity descriptionTemplateEntity : planReferences) {
                planReferenceImportExports.add(this.planReferenceToExport(descriptionTemplateEntity, referenceEntityMap, referenceTypeEntityMap));
            }
            return planReferenceImportExports;
        }
        return null;
    }

    private PlanReferenceImportExport planReferenceToExport(PlanReferenceEntity entity, Map<UUID, ReferenceEntity> referenceEntityMap, Map<UUID, ReferenceTypeEntity> referenceTypeEntityMap) {
        PlanReferenceImportExport xml = new PlanReferenceImportExport();
        if (entity == null) return xml;
        PlanReferenceDataEntity referenceData = this.jsonHandlingService.fromJsonSafe(PlanReferenceDataEntity.class, entity.getData());
        if (referenceData != null) xml.setFieldId(referenceData.getBlueprintFieldId());
        ReferenceEntity reference = referenceEntityMap.getOrDefault(entity.getReferenceId(), null);

        if (reference != null){
            xml.setId(reference.getId());
            xml.setLabel(reference.getLabel());
            xml.setSource(reference.getSource());
            xml.setSourceType(reference.getSourceType());
            xml.setReference(reference.getReference());
            ReferenceTypeEntity referenceType = referenceTypeEntityMap.getOrDefault(reference.getTypeId(), null);
            if (referenceType != null) xml.setType(this.planReferenceTypeToExport(referenceType));
        }
        
        return xml;
    }

    private PlanReferenceTypeImportExport planReferenceTypeToExport(ReferenceTypeEntity entity) {
        PlanReferenceTypeImportExport xml = new PlanReferenceTypeImportExport();
        if (entity == null) return xml;
        xml.setId(entity.getId());
        xml.setCode(entity.getCode());
        xml.setName(entity.getName());

        return xml;
    }

    private List<PlanBlueprintValueImportExport> planBlueprintValuesToExport(PlanPropertiesEntity propertiesEntity, PlanBlueprintEntity blueprintEntity){
        if (propertiesEntity != null && !this.conventionService.isListNullOrEmpty(propertiesEntity.getPlanBlueprintValues())) {
            List<PlanBlueprintValueImportExport> planBlueprintValueImportExports = new LinkedList<>();
            org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprintEntity.getDefinition());
            for (PlanBlueprintValueEntity planBlueprintValueEntity : propertiesEntity.getPlanBlueprintValues()) {
                planBlueprintValueImportExports.add(this.planBlueprintValueToExport(planBlueprintValueEntity, definition));
            }
            return planBlueprintValueImportExports;
        }
        return null;
    }

    private PlanBlueprintValueImportExport planBlueprintValueToExport(PlanBlueprintValueEntity entity, org.opencdmp.commons.types.planblueprint.DefinitionEntity definition) {
        PlanBlueprintValueImportExport xml = new PlanBlueprintValueImportExport();
        if (entity == null) return xml;

        org.opencdmp.commons.types.planblueprint.FieldEntity fieldEntity = definition.getFieldById(entity.getFieldId()).stream().findFirst().orElse(null);
        if (fieldEntity == null) return xml;

        if (fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.Extra)) {
            ExtraFieldEntity extraFieldEntity = (ExtraFieldEntity) fieldEntity;
            if (PlanBlueprintExtraFieldDataType.isDateType(extraFieldEntity.getType())){
                xml.setDateValue(entity.getDateValue());
            } else if (PlanBlueprintExtraFieldDataType.isNumberType(extraFieldEntity.getType())){
                xml.setNumberValue(entity.getNumberValue());
            } else {
                xml.setValue(entity.getValue());
            }
            xml.setFieldId(entity.getFieldId());
        }

        return xml;
    }


    private List<PlanDescriptionTemplateImportExport> planDescriptionTemplatesToExport(PlanEntity data){
        List<PlanDescriptionTemplateEntity> planDescriptionTemplateEntities = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().authorize(AuthorizationFlags.All).planIds(data.getId()).isActive(IsActive.Active).collect();
        if (!this.conventionService.isListNullOrEmpty(planDescriptionTemplateEntities)) {
            List<PlanDescriptionTemplateImportExport> planDescriptionTemplateImportExports = new LinkedList<>();
            List<org.opencdmp.data.DescriptionTemplateEntity> templatesWithCode = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(planDescriptionTemplateEntities.stream().map(PlanDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList())).disableTracking().collectAs(new BaseFieldSet().ensure(org.opencdmp.data.DescriptionTemplateEntity._code).ensure(org.opencdmp.data.DescriptionTemplateEntity._groupId));

            for (PlanDescriptionTemplateEntity descriptionTemplateEntity : planDescriptionTemplateEntities) {
                planDescriptionTemplateImportExports.add(this.planDescriptionTemplateToExport(descriptionTemplateEntity, templatesWithCode));
            }
            return planDescriptionTemplateImportExports;
        }
        return null;
    }

    private PlanDescriptionTemplateImportExport planDescriptionTemplateToExport(PlanDescriptionTemplateEntity entity, List<org.opencdmp.data.DescriptionTemplateEntity> templatesWithCode) {
        PlanDescriptionTemplateImportExport xml = new PlanDescriptionTemplateImportExport();
        if (entity == null) return xml;

        if (!this.conventionService.isListNullOrEmpty(templatesWithCode)) {
            org.opencdmp.data.DescriptionTemplateEntity code = templatesWithCode.stream().filter(x -> x.getGroupId().equals(entity.getDescriptionTemplateGroupId())).findFirst().orElse(null);
            if (code != null) xml.setDescriptionTemplateCode(code.getCode());
        }

        xml.setDescriptionTemplateGroupId(entity.getDescriptionTemplateGroupId());
        xml.setSectionId(entity.getSectionId());

        return xml;
    }

    private List<PlanUserImportExport> planUsersToExport(PlanEntity data){
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(data.getId()).authorize(AuthorizationFlags.All).isActives(IsActive.Active).collect();
        if (!this.conventionService.isListNullOrEmpty(planUsers)) {
            List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUsers.stream().map(PlanUserEntity::getUserId).distinct().toList()).authorize(AuthorizationFlags.All).isActive(IsActive.Active).collect();
            Map<UUID, UserEntity> usersMap = users == null ? new HashMap<>() : users.stream().collect(Collectors.toMap(UserEntity::getId, x -> x));
            List<PlanUserImportExport> planUserImportExports = new LinkedList<>();
            for (PlanUserEntity planUserEntity : planUsers) {
                planUserImportExports.add(this.planUserToExport(planUserEntity, usersMap));
            }
            return planUserImportExports;
        }
        return null;
    }

    private PlanUserImportExport planUserToExport(PlanUserEntity entity, Map<UUID, UserEntity> usersMap) {
        PlanUserImportExport xml = new PlanUserImportExport();
        if (entity == null) return xml;

        xml.setId(entity.getUserId());
        UserEntity userEntity = usersMap.getOrDefault(entity.getUserId(), null);
        if (userEntity != null){
            xml.setName(userEntity.getName());
        }
        xml.setRole(entity.getRole());
        xml.setSectionId(entity.getSectionId());

        return xml;
    }

    private List<PlanContactImportExport> planContactsToExport(PlanPropertiesEntity propertiesEntity){
        if (propertiesEntity != null && !this.conventionService.isListNullOrEmpty(propertiesEntity.getContacts())) {
            List<PlanContactImportExport> planContactImportExports = new LinkedList<>();
            for (PlanContactEntity contactEntity : propertiesEntity.getContacts()) {
                planContactImportExports.add(this.planContactToExport(contactEntity));
            }
            return planContactImportExports;
        }
        return null;
    }

    private PlanContactImportExport planContactToExport(PlanContactEntity entity) {
        PlanContactImportExport xml = new PlanContactImportExport();
        if (entity == null) return xml;

        xml.setEmail(entity.getEmail());
        xml.setFirstName(entity.getFirstName());
        xml.setLastName(entity.getLastName());

        return xml;
    }

    //endregion


    //region Import Xml
    
    public Plan importXml(byte[] bytes, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import data").And("bytes", bytes).And("label", label).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.NewPlan);

        PlanImportExport planXml;
        try {
            planXml = this.xmlHandlingService.fromXml(PlanImportExport.class, new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.warn("Plan import xml failed. Input: " + new String(bytes, StandardCharsets.UTF_8));
            throw new MyApplicationException(this.errors.getInvalidPlanImportXml().getCode(), this.errors.getInvalidPlanImportXml().getMessage());
        }

        if (planXml == null) {
            logger.warn("Plan import xml failed. Input: " + new String(bytes, StandardCharsets.UTF_8));
            throw new MyApplicationException(this.errors.getInvalidPlanImportXml().getCode(), this.errors.getInvalidPlanImportXml().getMessage());
        }
        
        PlanPersist persist = new PlanPersist();
        persist.setLabel(label);
        persist.setDescription(planXml.getDescription());
        persist.setAccessType(planXml.getAccess());
        persist.setLanguage(planXml.getLanguage());
        persist.setProperties(this.xmlToPlanPropertiesPersist(planXml));
        persist.setBlueprint(this.xmlPlanBlueprintToPersist(planXml));
        persist.setDescriptionTemplates(this.xmlPlanDescriptionTemplatesToPersist(planXml, persist.getBlueprint())); //TODO maybe we should create templates if not exists

        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = null;
        if (persist.getBlueprint() != null) {
            PlanBlueprintEntity planBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(persist.getBlueprint()).first();
            definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());
            if (definition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{persist.getBlueprint(), org.opencdmp.commons.types.planblueprint.DefinitionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

        this.validatorFactory.validator(PlanPersist.PlanPersistValidator.class).validateForce(persist);

        Plan plan = this.persist(persist, BaseFieldSet.build(fields, Plan._id, Plan._hash));
        if (plan == null) throw new MyApplicationException("Error creating plan");

        if (!this.conventionService.isListNullOrEmpty(planXml.getDescriptions())){
            for (DescriptionImportExport description: planXml.getDescriptions()){
                if (definition != null && description.getSectionId() != null && !this.conventionService.isListNullOrEmpty(definition.getSections())) {
                    if (definition.getSections().stream().filter(x -> x.getId().equals(description.getSectionId()) && x.getHasTemplates()).findFirst().orElse(null) != null){
                        this.descriptionService.importXml(description, plan.getId(), fields != null ? fields.extractPrefixed(this.conventionService.asPrefix(Plan._description)) : null);
                    }
                }
            }
        }

        this.accountingService.increase(UsageLimitTargetMetric.IMPORT_PLAN_XML_EXECUTION_COUNT.getValue());

        return plan;
    }

    private UUID xmlPlanBlueprintToPersist(PlanImportExport planXml) throws JAXBException, InvalidApplicationException, ParserConfigurationException, IOException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        if (planXml.getBlueprint() != null){
            PlanBlueprintEntity planBlueprintEntity = planXml.getBlueprint().getId() != null ? this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(planXml.getBlueprint().getId()).first() : null;
            if (planBlueprintEntity == null) planBlueprintEntity = planXml.getBlueprint().getGroupId() != null ? this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().groupIds(planXml.getBlueprint().getGroupId()).versionStatuses(PlanBlueprintVersionStatus.Current).isActive(IsActive.Active).statuses(PlanBlueprintStatus.Finalized).first() : null;
            if (planBlueprintEntity != null){
                if (!planBlueprintEntity.getStatus().equals(PlanBlueprintStatus.Finalized)) throw new MyValidationException(this.errors.getPlanBlueprintImportDraft().getCode(), planBlueprintEntity.getCode());
                return planBlueprintEntity.getId();
            } else {
                planBlueprintEntity = !this.conventionService.isNullOrEmpty(planXml.getBlueprint().getCode()) ? this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().codes(planXml.getBlueprint().getCode()).versionStatuses(PlanBlueprintVersionStatus.Current).isActive(IsActive.Active).first() : null;
                if (planBlueprintEntity == null) {
                    throw new MyValidationException(this.errors.getPlanBlueprintImportNotFound().getCode(), planXml.getBlueprint().getCode());
                } else {
                    if (!planBlueprintEntity.getStatus().equals(PlanBlueprintStatus.Finalized)) throw new MyValidationException(this.errors.getPlanBlueprintImportDraft().getCode(), planBlueprintEntity.getCode());
                    return planBlueprintEntity.getId();
                }
            }
        }
        return null;
    }

    private PlanPropertiesPersist xmlToPlanPropertiesPersist(PlanImportExport importXml) {
        if (importXml == null) return null;

        PlanPropertiesPersist persist = new PlanPropertiesPersist();
        persist.setContacts(this.xmlToPlanContactPersist(importXml));
        persist.setPlanBlueprintValues(this.xmlToPlanBlueprintValuePersist(importXml));

        return persist;
    }

    private Map<UUID, PlanBlueprintValuePersist> xmlToPlanBlueprintValuePersist(PlanImportExport importXml){
        if (importXml.getBlueprint() != null && importXml.getBlueprint().getPlanBlueprintDefinition() != null && !this.conventionService.isListNullOrEmpty(importXml.getBlueprint().getPlanBlueprintDefinition().getSections())) {
            Map<UUID, PlanBlueprintValuePersist> planBlueprintValues = new HashMap<>();
            List<BlueprintSectionImportExport> sections = importXml.getBlueprint().getPlanBlueprintDefinition().getSections();
            if (!this.conventionService.isListNullOrEmpty(sections)){
                for (BlueprintSectionImportExport section : importXml.getBlueprint().getPlanBlueprintDefinition().getSections()) {
                    this.xmlToPlanBlueprintExtraFieldValuePersist(importXml, section, planBlueprintValues);
                    this.xmlToPlanBlueprintReferenceFieldValuePersist(importXml, section, planBlueprintValues);
                }
            }
            return planBlueprintValues;
        }
        return null;
    }

    private void xmlToPlanBlueprintExtraFieldValuePersist(PlanImportExport importXml, BlueprintSectionImportExport section, Map<UUID, PlanBlueprintValuePersist> planBlueprintValues){
        if (!this.conventionService.isListNullOrEmpty(section.getExtraFields()) && !this.conventionService.isListNullOrEmpty(importXml.getBlueprintValues())){
            for (PlanBlueprintValueImportExport value : importXml.getBlueprintValues()) {
                if (value.getFieldId() != null ) {
	                section.getExtraFields().stream().filter(x -> x.getId().equals(value.getFieldId())).findFirst().ifPresent(extraFieldImportExport -> planBlueprintValues.put(value.getFieldId(), this.xmlPlanBlueprintValueToPersist(value, extraFieldImportExport)));
                }
            }
        }
    }
    
    private void xmlToPlanBlueprintReferenceFieldValuePersist(PlanImportExport importXml, BlueprintSectionImportExport section, Map<UUID, PlanBlueprintValuePersist> planBlueprintValues){
        if (!this.conventionService.isListNullOrEmpty(section.getReferenceFields()) && !this.conventionService.isListNullOrEmpty(importXml.getReferences())){
            for (BlueprintReferenceTypeFieldImportExport blueprintReferenceTypeField : section.getReferenceFields()) {
                List<PlanReferenceImportExport> planReferencesByField = importXml.getReferences().stream().filter(x -> x.getFieldId().equals(blueprintReferenceTypeField.getId())).collect(Collectors.toList());
                if (!this.conventionService.isListNullOrEmpty(planReferencesByField)){
                    planBlueprintValues.put(blueprintReferenceTypeField.getId(), this.xmlReferenceFieldToPlanBlueprintValuePersist(blueprintReferenceTypeField, planReferencesByField));
                }
            }
        }
    }

    private List<PlanContactPersist> xmlToPlanContactPersist(PlanImportExport importXml){
        if (!this.conventionService.isListNullOrEmpty(importXml.getContacts())) {
            List<PlanContactPersist> contacts = new ArrayList<>();
            for (PlanContactImportExport contact : importXml.getContacts()) {
                contacts.add(this.xmlPlanContactToPersist(contact));
            }
            return contacts;
        }
        return null;
    }

    private PlanBlueprintValuePersist xmlReferenceFieldToPlanBlueprintValuePersist(BlueprintReferenceTypeFieldImportExport blueprintReferenceTypeFieldImportXml, List<PlanReferenceImportExport> planReferencesImportXml) {
        if (blueprintReferenceTypeFieldImportXml == null || this.conventionService.isListNullOrEmpty(planReferencesImportXml))
            return null;

        ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(blueprintReferenceTypeFieldImportXml.getReferenceTypeId()).first();//TODO: optimize
        if (referenceTypeEntity == null) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).codes(blueprintReferenceTypeFieldImportXml.getReferenceTypeCode()).first();
        if (referenceTypeEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{blueprintReferenceTypeFieldImportXml.getReferenceTypeCode(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        PlanBlueprintValuePersist persist = new PlanBlueprintValuePersist();

        persist.setFieldId(blueprintReferenceTypeFieldImportXml.getId());
        if (blueprintReferenceTypeFieldImportXml.getMultipleSelect()){
            List<ReferencePersist> references = new ArrayList<>();
            for (PlanReferenceImportExport planReference : planReferencesImportXml) {
                references.add(this.xmlPlanReferenceToReferencePersist(planReference, referenceTypeEntity));
            }
            persist.setReferences(references);
        } else {
            persist.setReference(this.xmlPlanReferenceToReferencePersist(planReferencesImportXml.stream().findFirst().orElse(null), referenceTypeEntity));
        }

        return persist;
    }

    private ReferencePersist xmlPlanReferenceToReferencePersist(PlanReferenceImportExport importXml, ReferenceTypeEntity referenceTypeEntity) {
        if (importXml == null)
            return null;
        
        if (!referenceTypeEntity.getCode().equals(importXml.getType().getCode())) throw new MyApplicationException("Invalid reference for field " + importXml.getId());
        
        ReferenceEntity referenceEntity = this.queryFactory.query(ReferenceQuery.class).ids(importXml.getId()).first(); //TODO: optimize
        if (referenceEntity == null) referenceEntity = this.queryFactory.query(ReferenceQuery.class).references(importXml.getReference()).typeIds(referenceTypeEntity.getId()).sources(importXml.getSource()).first();
        
        ReferencePersist persist = new ReferencePersist();
        
        persist.setTypeId(referenceTypeEntity.getId());
        if (referenceEntity == null) {
            persist.setLabel(importXml.getLabel());
            persist.setReference(importXml.getReference());
            persist.setSource(importXml.getSource());
            persist.setSourceType(ReferenceSourceType.External);
        } else {
            persist.setId(referenceEntity.getId());
            persist.setLabel(referenceEntity.getLabel());
            persist.setReference(referenceEntity.getReference());
            persist.setSource(referenceEntity.getSource());
            persist.setSourceType(referenceEntity.getSourceType());
            persist.setAbbreviation(referenceEntity.getAbbreviation());
            persist.setDescription(referenceEntity.getDescription());
            persist.setHash(this.conventionService.hashValue(referenceEntity.getUpdatedAt()));
        }
        return persist;
    }

    private PlanBlueprintValuePersist xmlPlanBlueprintValueToPersist(PlanBlueprintValueImportExport importXml, BlueprintExtraFieldImportExport extraFieldImportExport) {
        if (importXml == null || extraFieldImportExport == null)
            return null;

        PlanBlueprintValuePersist persist = new PlanBlueprintValuePersist();

        persist.setFieldId(importXml.getFieldId());
        switch (extraFieldImportExport.getType()){
            case Date ->  persist.setDateValue(importXml.getDateValue());
            case Number ->  persist.setNumberValue(importXml.getNumberValue());
            case Text, RichTex ->  persist.setFieldValue(importXml.getValue());
            default -> throw new MyApplicationException("unrecognized type " + extraFieldImportExport.getType());
        }

        return persist;
    }

    private  List<PlanDescriptionTemplatePersist> xmlPlanDescriptionTemplatesToPersist(PlanImportExport planXml, UUID blueprintId) throws InvalidApplicationException {
        if (!this.conventionService.isListNullOrEmpty(planXml.getDescriptionTemplates())) {
            List<PlanDescriptionTemplatePersist> descriptionTemplates = new ArrayList<>();
            for (PlanDescriptionTemplateImportExport descriptionTemplate : planXml.getDescriptionTemplates()) {
                descriptionTemplates.add(this.xmlPlanDescriptionTemplateToPersist(descriptionTemplate));
            }
            // add description templates from blueprint
            if (blueprintId != null) {
                PlanBlueprintEntity blueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, blueprintId, true);
                if (blueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planXml.getBlueprint().getId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprintEntity.getDefinition());
                List<SectionEntity> sections = definition.getSections().stream().filter(SectionEntity::getHasTemplates).collect(Collectors.toList());
                for (SectionEntity section: sections) {
                    if (section.getHasTemplates() && !this.conventionService.isListNullOrEmpty(section.getDescriptionTemplates())){
                        for (BlueprintDescriptionTemplateEntity descriptionTemplateEntity: section.getDescriptionTemplates()){
                            if (descriptionTemplates.stream().filter(x -> x.getDescriptionTemplateGroupId().equals(descriptionTemplateEntity.getDescriptionTemplateGroupId()) && x.getSectionId().equals(section.getId())).findFirst().orElse(null) == null) {
                                PlanDescriptionTemplateImportExport model = new PlanDescriptionTemplateImportExport();
                                model.setDescriptionTemplateGroupId(descriptionTemplateEntity.getDescriptionTemplateGroupId());
                                model.setSectionId(section.getId());
                                descriptionTemplates.add(this.xmlPlanDescriptionTemplateToPersist(model));
                            }
                        }
                    }
                }

            }
            return descriptionTemplates.stream().filter(Objects::nonNull).toList();
        }
        return null;
    }

    private PlanDescriptionTemplatePersist xmlPlanDescriptionTemplateToPersist(PlanDescriptionTemplateImportExport importXml) {
        if (importXml == null)
            return null;

        PlanDescriptionTemplatePersist persist = new PlanDescriptionTemplatePersist();

        org.opencdmp.data.DescriptionTemplateEntity data = importXml.getDescriptionTemplateGroupId() != null ? this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().versionStatuses(DescriptionTemplateVersionStatus.Current).groupIds(importXml.getDescriptionTemplateGroupId()).isActive(IsActive.Active).disableTracking().firstAs(new BaseFieldSet().ensure(DescriptionTemplate._groupId).ensure(DescriptionTemplate._status).ensure(DescriptionTemplate._code)) : null;
        if (data != null ) {
            if (!data.getStatus().equals(DescriptionTemplateStatus.Finalized)) throw new MyValidationException(this.errors.getPlanDescriptionTemplateImportDraft().getCode(), data.getCode());
            persist.setDescriptionTemplateGroupId(importXml.getDescriptionTemplateGroupId());
        } else {
            if (!this.conventionService.isNullOrEmpty(importXml.getDescriptionTemplateCode())) {
                data = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().codes(importXml.getDescriptionTemplateCode()).isActive(IsActive.Active).versionStatuses(DescriptionTemplateVersionStatus.Current).disableTracking().firstAs(new BaseFieldSet().ensure(DescriptionTemplate._code).ensure(DescriptionTemplate._groupId).ensure(DescriptionTemplate._status));
                if (data != null) {
                    if (!data.getStatus().equals(DescriptionTemplateStatus.Finalized)) throw new MyValidationException(this.errors.getPlanDescriptionTemplateImportDraft().getCode(), data.getCode());
                    persist.setDescriptionTemplateGroupId(data.getGroupId());
                }
            }
        }

        if (data == null) throw new MyValidationException(this.errors.getDescriptionTemplateImportNotFound().getCode(), importXml.getDescriptionTemplateCode());

        persist.setSectionId(importXml.getSectionId());

        return persist;
    }

    private PlanContactPersist xmlPlanContactToPersist(PlanContactImportExport importXml) {
        if (importXml == null)
            return null;

        PlanContactPersist persist = new PlanContactPersist();

        persist.setEmail(importXml.getEmail());
        persist.setFirstName(importXml.getFirstName());
        persist.setLastName(importXml.getLastName());

        return persist;
    }

    //endregion


    //region Import RDA JSON
    public PreprocessingPlanModel preprocessingPlan(UUID fileId, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        return this.fileTransformerService.preprocessingPlan(fileId, repositoryId);
    }

    public Plan importJson(PlanCommonModelConfig planCommonModelConfig, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ParserConfigurationException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import data").And("file id", planCommonModelConfig.getFileId()).And("label", planCommonModelConfig.getLabel()).And("fields", fields));

        PlanModel model = this.fileTransformerService.importPlan(planCommonModelConfig);
        if (model == null) throw new MyNotFoundException("Plan Import Error");

        PlanPersist persist = new PlanPersist();

        persist.setLabel(planCommonModelConfig.getLabel());
        persist.setDescription(model.getDescription());
        switch (model.getAccessType()) {
            case Public -> persist.setAccessType(PlanAccessType.Public);
            case Restricted -> persist.setAccessType(PlanAccessType.Restricted);
            default -> throw new MyApplicationException("Unrecognized Type " + model.getAccessType().getValue());
        }
        persist.setLanguage(model.getLanguage());
        persist.setBlueprint(this.commonModelPlanBlueprintToPersist(model));
        persist.setDescriptionTemplates(this.commonModelPlanDescriptionTemplatesToPersist(model)); //TODO maybe we should create templates if not exists
        persist.setProperties(this.commonModelPlanPropertiesToPersist(model));

        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = null;
        if (persist.getBlueprint() != null) {
            PlanBlueprintEntity planBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(persist.getBlueprint()).first();
            definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());
            if (definition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{persist.getBlueprint(), org.opencdmp.commons.types.planblueprint.DefinitionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

        this.validatorFactory.validator(PlanPersist.PlanPersistValidator.class).validateForce(persist);
        Plan plan = this.persist(persist, BaseFieldSet.build(fields, Plan._id, Plan._hash));
        if (plan == null) throw new MyApplicationException("Error creating plan");

        if (!this.conventionService.isListNullOrEmpty(model.getDescriptions())){
            for (DescriptionModel description: model.getDescriptions()){
                if (definition != null && description.getSectionId() != null && !this.conventionService.isListNullOrEmpty(definition.getSections())) {
                    if (definition.getSections().stream().filter(x -> x.getId().equals(description.getSectionId()) && x.getHasTemplates()).findFirst().orElse(null) != null) {
                        this.descriptionService.importCommonModel(description, plan.getId(), fields != null ? fields.extractPrefixed(this.conventionService.asPrefix(Plan._description)) : null);
                    }
                }
            }
        }


        return plan;
    }

    private  List<PlanDescriptionTemplatePersist> commonModelPlanDescriptionTemplatesToPersist(PlanModel commonModel) throws InvalidApplicationException {
        if (!this.conventionService.isListNullOrEmpty(commonModel.getDescriptionTemplates())) {
            List<PlanDescriptionTemplatePersist> descriptionTemplates = new ArrayList<>();
            for (PlanDescriptionTemplateModel descriptionTemplate : commonModel.getDescriptionTemplates()) {
                descriptionTemplates.add(this.commonModelPlanDescriptionTemplateToPersist(descriptionTemplate));
            }
            // add description templates from blueprint
            if (commonModel.getPlanBlueprint() != null && commonModel.getPlanBlueprint().getId() != null) {
                PlanBlueprintEntity blueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, commonModel.getPlanBlueprint().getId(), true);
                if (blueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{commonModel.getPlanBlueprint().getId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprintEntity.getDefinition());
                List<SectionEntity> sections = definition.getSections().stream().filter(SectionEntity::getHasTemplates).collect(Collectors.toList());
                for (SectionEntity section: sections) {
                    if (section.getHasTemplates() && !this.conventionService.isListNullOrEmpty(section.getDescriptionTemplates())){
                        for (BlueprintDescriptionTemplateEntity descriptionTemplateEntity: section.getDescriptionTemplates()){
                            if (descriptionTemplates.stream().filter(x -> x.getDescriptionTemplateGroupId().equals(descriptionTemplateEntity.getDescriptionTemplateGroupId()) && x.getSectionId().equals(section.getId())).findFirst().orElse(null) == null) {
                                PlanDescriptionTemplateModel model = new PlanDescriptionTemplateModel();
                                model.setDescriptionTemplateGroupId(descriptionTemplateEntity.getDescriptionTemplateGroupId());
                                model.setSectionId(section.getId());
                                descriptionTemplates.add(this.commonModelPlanDescriptionTemplateToPersist(model));
                            }
                        }
                    }
                }

            }
            return descriptionTemplates.stream().filter(Objects::nonNull).toList();
        }
        return null;
    }

    private PlanDescriptionTemplatePersist commonModelPlanDescriptionTemplateToPersist(PlanDescriptionTemplateModel commonModel) {
        if (commonModel == null)
            return null;

        PlanDescriptionTemplatePersist persist = new PlanDescriptionTemplatePersist();

        persist.setDescriptionTemplateGroupId(commonModel.getDescriptionTemplateGroupId());
        persist.setSectionId(commonModel.getSectionId());

        return persist;
    }
    
    private UUID commonModelPlanBlueprintToPersist(PlanModel planXml) throws JAXBException, InvalidApplicationException, ParserConfigurationException, IOException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        if (planXml.getPlanBlueprint() != null){
            PlanBlueprintEntity planBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(planXml.getPlanBlueprint().getId()).first();
            if (planBlueprintEntity == null) planBlueprintEntity = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().groupIds(planXml.getPlanBlueprint().getGroupId()).versionStatuses(PlanBlueprintVersionStatus.Current).isActive(IsActive.Active).statuses(PlanBlueprintStatus.Finalized).first();
            if (planBlueprintEntity != null){
                return planBlueprintEntity.getId();
            } else {
                PlanBlueprint persisted = this.planBlueprintService.importCommonModel(planXml.getPlanBlueprint(), new BaseFieldSet().ensure(PlanBlueprint._label).ensure(PlanBlueprint._hash));
                return persisted.getId();
            }
        }
        return null;
    }

    private PlanPropertiesPersist commonModelPlanPropertiesToPersist(PlanModel commonModel) {
        if (commonModel == null || commonModel.getProperties() == null) return null;

        PlanPropertiesPersist persist = new PlanPropertiesPersist();
        persist.setContacts(this.commonModelToPlanContactPersist(commonModel.getProperties()));
        persist.setPlanBlueprintValues(this.commonModelToPlanBlueprintValuePersist(commonModel));

        return persist;
    }  
    
    private List<PlanContactPersist> commonModelToPlanContactPersist(PlanPropertiesModel commonModel){
        if (!this.conventionService.isListNullOrEmpty(commonModel.getContacts())) {
            List<PlanContactPersist> contacts = new ArrayList<>();
            for (PlanContactModel contact : commonModel.getContacts()) {
                contacts.add(this.commonModelPlanContactToPersist(contact));
            }
            return contacts;
        }
        return null;
    }

    private Map<UUID, PlanBlueprintValuePersist> commonModelToPlanBlueprintValuePersist(PlanModel commonModel){
        if (commonModel.getPlanBlueprint() != null && commonModel.getPlanBlueprint().getDefinition() != null && !this.conventionService.isListNullOrEmpty(commonModel.getPlanBlueprint().getDefinition().getSections())) {
            Map<UUID, PlanBlueprintValuePersist> planBlueprintValues = new HashMap<>();
            List<SectionModel> sections = commonModel.getPlanBlueprint().getDefinition().getSections();
            if (!this.conventionService.isListNullOrEmpty(sections)){
                for (SectionModel section : sections) {
                    if (!this.conventionService.isListNullOrEmpty(section.getFields())){
                        for (FieldModel field : section.getFields()) {
                            // reference
                            if (field.getCategory().equals(org.opencdmp.commonmodels.enums.PlanBlueprintFieldCategory.ReferenceType)){
                                ReferenceTypeFieldModel referenceField = (ReferenceTypeFieldModel) field;
                                List<PlanReferenceModel> planReferencesByField = commonModel.getReferences().stream().filter(x -> x.getData() != null && x.getData().getBlueprintFieldId().equals(referenceField.getId())).collect(Collectors.toList());
                                if (!this.conventionService.isListNullOrEmpty(planReferencesByField)){
                                    planBlueprintValues.put(referenceField.getId(), this.commonModelPlanReferenceFieldToPlanBlueprintValuePersist(referenceField, planReferencesByField));
                                }
                            } else {
                                // custom fields
                                if (field.getCategory().equals(org.opencdmp.commonmodels.enums.PlanBlueprintFieldCategory.Extra) && commonModel.getProperties() != null && !this.conventionService.isListNullOrEmpty(commonModel.getProperties().getPlanBlueprintValues())){
                                    PlanBlueprintValueModel planBlueprintValueModel = commonModel.getProperties().getPlanBlueprintValues().stream().filter(x -> x.getFieldId().equals(field.getId())).findFirst().orElse(null);
                                    ExtraFieldModel extraFieldModel = (ExtraFieldModel) field;
                                    if (planBlueprintValueModel != null) planBlueprintValues.put(planBlueprintValueModel.getFieldId(), this.commonModelPlanBlueprintValueToPersist(planBlueprintValueModel, extraFieldModel));
                                }
                            }
                        }
                    }
                }
            }
            return planBlueprintValues;
        }
        return null;
    }
    
    private PlanBlueprintValuePersist commonModelPlanReferenceFieldToPlanBlueprintValuePersist(ReferenceTypeFieldModel model, List<PlanReferenceModel> planReferences) {
        if (model == null || this.conventionService.isListNullOrEmpty(planReferences) || model.getReferenceType() == null)
            return null;

        ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(model.getReferenceType().getId()).first();//TODO: optimize
        if (referenceTypeEntity == null) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).codes(model.getReferenceType().getCode()).first();
        if (referenceTypeEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getReferenceType().getCode(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanBlueprintValuePersist persist = new PlanBlueprintValuePersist();

        persist.setFieldId(model.getId());
        if (model.getMultipleSelect()){
            List<ReferencePersist> references = new ArrayList<>();
            for (PlanReferenceModel planReference : planReferences) {
                references.add(this.commonPlanReferenceToReferencePersist(planReference.getReference(), referenceTypeEntity));
            }
            persist.setReferences(references);
        } else {
            persist.setReference(this.commonPlanReferenceToReferencePersist(planReferences.getFirst().getReference(), referenceTypeEntity));
        }

        return persist;
    }

    private ReferencePersist commonPlanReferenceToReferencePersist(ReferenceModel model, ReferenceTypeEntity referenceTypeEntity) {
        if (!referenceTypeEntity.getCode().equals(model.getType().getCode())) throw new MyApplicationException("Invalid reference for field " + model.getId());

        if (this.conventionService.isNullOrEmpty(model.getLabel()) && this.conventionService.isNullOrEmpty(model.getReference())) throw new MyApplicationException("Plan Reference without label and reference id ");

        ReferenceEntity referenceEntity = model.getId() != null ? this.queryFactory.query(ReferenceQuery.class).ids(model.getId()).first(): null; //TODO: optimize
        if (referenceEntity == null && !this.conventionService.isNullOrEmpty(model.getReference())) {
            List<ReferenceEntity> referenceEntities = this.queryFactory.query(ReferenceQuery.class).references(model.getReference()).typeIds(referenceTypeEntity.getId()).collect();
            if (referenceEntities != null && referenceEntities.size() == 1) referenceEntity = referenceEntities.getFirst();
        }

        ReferencePersist persist = new ReferencePersist();

        persist.setTypeId(referenceTypeEntity.getId());
        if (referenceEntity == null) {
            persist.setReference(!this.conventionService.isNullOrEmpty(model.getReference()) ? model.getReference() : UUID.randomUUID().toString());
            persist.setLabel(!this.conventionService.isNullOrEmpty(model.getLabel()) ? model.getLabel() : persist.getReference());
            persist.setSource("internal");
            persist.setSourceType(ReferenceSourceType.Internal);
        } else {
            persist.setId(referenceEntity.getId());
            persist.setLabel(referenceEntity.getLabel());
            persist.setReference(referenceEntity.getReference());
            persist.setSource(referenceEntity.getSource());
            persist.setSourceType(referenceEntity.getSourceType());
            persist.setAbbreviation(referenceEntity.getAbbreviation());
            persist.setDescription(referenceEntity.getDescription());
            persist.setHash(this.conventionService.hashValue(referenceEntity.getUpdatedAt()));
        }
        return persist;
    }

    private PlanBlueprintValuePersist commonModelPlanBlueprintValueToPersist(PlanBlueprintValueModel commonModel, ExtraFieldModel extraFieldModel) {
        if (commonModel == null || extraFieldModel == null)
            return null;
        PlanBlueprintValuePersist persist = new PlanBlueprintValuePersist();

        persist.setFieldId(commonModel.getFieldId());
        switch (extraFieldModel.getDataType()){
            case Date ->  persist.setDateValue(commonModel.getDateValue());
            case Number ->  persist.setNumberValue(commonModel.getNumberValue());
            case Text, RichTex ->  persist.setFieldValue(commonModel.getValue());
            default -> throw new MyApplicationException("unrecognized type " + extraFieldModel.getDataType());
        }

        return persist;
    }

    private PlanContactPersist commonModelPlanContactToPersist(PlanContactModel commonModel) {
        if (commonModel == null)
            return null;

        PlanContactPersist persist = new PlanContactPersist();

        persist.setEmail(commonModel.getEmail());
        persist.setFirstName(commonModel.getFirstName());
        persist.setLastName(commonModel.getLastName());

        return persist;
    }

}
