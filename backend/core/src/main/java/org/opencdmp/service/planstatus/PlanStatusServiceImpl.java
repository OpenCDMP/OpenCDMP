package org.opencdmp.service.planstatus;

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
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionAuthorizationEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionAuthorizationItemEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionEntity;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionTransitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.EventBroker;
import org.opencdmp.event.PlanStatusTouchedEvent;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.planstatus.PlanStatusBuilder;
import org.opencdmp.model.deleter.PlanStatusDeleter;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionAuthorizationItemPersist;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionAuthorizationPersist;
import org.opencdmp.model.persist.planstatus.PlanStatusDefinitionPersist;
import org.opencdmp.model.persist.planstatus.PlanStatusPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.PlanStatusQuery;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.custompolicy.CustomPolicyService;
import org.opencdmp.service.planworkflow.PlanWorkflowService;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanStatusServiceImpl implements PlanStatusService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanStatusServiceImpl.class));

    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;

    private final AuthorizationService authorizationService;
    private final ConventionService conventionService;
    private final XmlHandlingService xmlHandlingService;
    private final TenantEntityManager entityManager;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final EventBroker eventBroker;
    private final TenantScope tenantScope;
    private final PlanWorkflowService planWorkflowService;
    private final CustomPolicyService customPolicyService;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final QueryFactory queryFactory;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final StorageFileService storageFileService;

    public PlanStatusServiceImpl(BuilderFactory builderFactory, DeleterFactory deleterFactory, AuthorizationService authorizationService, ConventionService conventionService, XmlHandlingService xmlHandlingService, TenantEntityManager entityManager, MessageSource messageSource, ErrorThesaurusProperties errors, EventBroker eventBroker, TenantScope tenantScope, PlanWorkflowService planWorkflowService, CustomPolicyService customPolicyService, AuthorizationContentResolver authorizationContentResolver, QueryFactory queryFactory, UsageLimitService usageLimitService, AccountingService accountingService, StorageFileService storageFileService) {
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;

        this.authorizationService = authorizationService;
        this.conventionService = conventionService;
        this.xmlHandlingService = xmlHandlingService;
        this.entityManager = entityManager;
        this.messageSource = messageSource;
        this.errors = errors;
	    this.eventBroker = eventBroker;
        this.tenantScope = tenantScope;
        this.planWorkflowService = planWorkflowService;
        this.customPolicyService = customPolicyService;
        this.authorizationContentResolver = authorizationContentResolver;
        this.queryFactory = queryFactory;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.storageFileService = storageFileService;
    }

    @Override
    public PlanStatus persist(PlanStatusPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data plan status").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPlanStatus);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanStatusEntity data;
        if (isUpdate) {
            data = this.entityManager.find(PlanStatusEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.PLAN_STATUS_COUNT);

            data = new PlanStatusEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setName(model.getName());
        data.setDescription(model.getDescription());
        data.setAction(model.getAction());
        data.setOrdinal(model.getOrdinal());
        data.setInternalStatus(model.getInternalStatus());
        PlanStatusDefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(data.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(PlanStatusDefinitionEntity.class, data.getDefinition());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildPlanStatusDefinitionEntity(model.getDefinition(), oldDefinition != null ? oldDefinition.getStorageFileId(): null)));
        data.setUpdatedAt(Instant.now());

        if (isUpdate)
            this.entityManager.merge(data);
        else {
            this.accountingService.increase(UsageLimitTargetMetric.PLAN_STATUS_COUNT.getValue());
            this.entityManager.persist(data);
        }
        this.entityManager.flush();

        this.eventBroker.emit(new PlanStatusTouchedEvent(data.getId(), this.tenantScope.getTenantCode()));

        return this.builderFactory.builder(PlanStatusBuilder.class).build(BaseFieldSet.build(fields, PlanStatus._id), data);
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting plan status: {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePlanStatus);

        this.deleterFactory.deleter(PlanStatusDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    private @NotNull PlanStatusDefinitionEntity buildPlanStatusDefinitionEntity(PlanStatusDefinitionPersist persist, UUID oldStorageFileId) throws InvalidApplicationException {
        PlanStatusDefinitionEntity data = new PlanStatusDefinitionEntity();
        if (persist == null)
            return data;

        if (persist.getAuthorization() != null) {
            PlanStatusDefinitionAuthorizationEntity definitionAuthorizationData = this.buildPlanStatusDefinitionAuthorizationEntity(persist.getAuthorization());
            data.setAuthorization(definitionAuthorizationData);
        }

        data.setMatIconName(persist.getMatIconName());
        if (persist.getStorageFileId() != null) {
            if (!persist.getStorageFileId().equals(oldStorageFileId)) {
                StorageFileEntity storageFileEntity = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(persist.getStorageFileId()).firstAs(new BaseFieldSet().ensure(StorageFile._id).ensure(StorageFile._storageType));
                if (storageFileEntity == null || storageFileEntity.getStorageType().equals(StorageType.Temp)) {
                    StorageFile storageFile = this.storageFileService.copyToStorage(persist.getStorageFileId(), StorageType.Main, true, new BaseFieldSet().ensure(StorageFile._id));
                    this.storageFileService.updatePurgeAt(storageFile.getId(), null);
                    if (oldStorageFileId != null) this.storageFileService.updatePurgeAt(oldStorageFileId, Instant.now().plusSeconds(60));
                    data.setStorageFileId(storageFile.getId());
                }
            } else {
                data.setStorageFileId(persist.getStorageFileId());
            }
        } else {
            if (oldStorageFileId != null) this.storageFileService.updatePurgeAt(oldStorageFileId, Instant.now().plusSeconds(60));
        }
        data.setStatusColor(persist.getStatusColor());

        if (!this.conventionService.isListNullOrEmpty(persist.getAvailableActions())) data.setAvailableActions(persist.getAvailableActions());
        return data;
    }

    private @NotNull PlanStatusDefinitionAuthorizationEntity buildPlanStatusDefinitionAuthorizationEntity(PlanStatusDefinitionAuthorizationPersist persist) {
        PlanStatusDefinitionAuthorizationEntity data = new PlanStatusDefinitionAuthorizationEntity();
        if (persist == null)
            return data;

        if (persist.getEdit() != null) {
            PlanStatusDefinitionAuthorizationItemEntity definitionAuthorizationData = this.buildPlanStatusDefinitionAuthorizationItemEntity(persist.getEdit());
            data.setEdit(definitionAuthorizationData);
        }
        return data;
    }

    private @NotNull PlanStatusDefinitionAuthorizationItemEntity buildPlanStatusDefinitionAuthorizationItemEntity(PlanStatusDefinitionAuthorizationItemPersist persist) {
        PlanStatusDefinitionAuthorizationItemEntity data = new PlanStatusDefinitionAuthorizationItemEntity();
        if (persist == null)
            return data;

        data.setPlanRoles(persist.getPlanRoles());
        data.setRoles(persist.getRoles());
        data.setAllowAuthenticated(persist.getAllowAuthenticated());
        data.setAllowAnonymous(persist.getAllowAnonymous());

        return data;
    }

    @Override
    public Map<UUID, List<UUID>> getAuthorizedAvailableStatusIds(List<UUID> planIds) {

        Map<UUID, List<UUID>> authorizedStatusMap = new HashMap<>();
        PlanWorkflowDefinitionEntity definition;
        try {
            definition = this.planWorkflowService.getActiveWorkFlowDefinition();
        } catch (InvalidApplicationException e) {
            throw new RuntimeException(e);
        }

        List<PlanEntity> planEntities = this.queryFactory.query(PlanQuery.class).ids(planIds).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._status));
        List<PlanStatusEntity> statusEntities = this.queryFactory.query(PlanStatusQuery.class).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanStatus._id));
        for (PlanEntity plan: planEntities) {
            authorizedStatusMap.put(plan.getId(), new ArrayList<>());
            AffiliatedResource affiliatedResource = this.authorizationContentResolver.planAffiliation(plan.getId());
            for (PlanStatusEntity status: statusEntities) {

                List<PlanWorkflowDefinitionTransitionEntity> availableTransitions = definition.getStatusTransitions().stream().filter(x -> x.getFromStatusId().equals(plan.getStatusId())).collect(Collectors.toList());
                if (availableTransitions.stream().filter(x -> x.getToStatusId().equals(status.getId())).findFirst().orElse(null) != null) {
                    String editPermission = this.customPolicyService.getPlanStatusCanEditStatusPermission(status.getId());
                    Boolean isAllowed = affiliatedResource == null ? this.authorizationService.authorize(editPermission) : this.authorizationService.authorizeAtLeastOne(List.of(affiliatedResource), editPermission);
                    if (isAllowed) authorizedStatusMap.get(plan.getId()).add(status.getId());
                }
            }
        }

        return authorizedStatusMap;
    }
}
