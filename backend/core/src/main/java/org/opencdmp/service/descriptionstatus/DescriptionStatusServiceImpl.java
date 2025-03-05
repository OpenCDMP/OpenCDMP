package org.opencdmp.service.descriptionstatus;

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
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionAuthorizationEntity;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionAuthorizationItemEntity;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.commons.types.descriptionworkflow.DescriptionWorkflowDefinitionEntity;
import org.opencdmp.commons.types.descriptionworkflow.DescriptionWorkflowDefinitionTransitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.event.DescriptionStatusTouchedEvent;
import org.opencdmp.event.EventBroker;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.descriptionstatus.DescriptionStatusBuilder;
import org.opencdmp.model.deleter.DescriptionStatusDeleter;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusDefinitionAuthorizationItemPersist;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusDefinitionAuthorizationPersist;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusDefinitionPersist;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusPersist;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.DescriptionStatusQuery;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.custompolicy.CustomPolicyService;
import org.opencdmp.service.descriptionworkflow.DescriptionWorkflowService;
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
public class DescriptionStatusServiceImpl implements DescriptionStatusService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionStatusServiceImpl.class));

    private final BuilderFactory builderFactory;
    private final DeleterFactory deleterFactory;

    private final AuthorizationService authService;
    private final TenantEntityManager entityManager;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final XmlHandlingService xmlHandlingService;
    private final EventBroker eventBroker;
    private final TenantScope tenantScope;
    private final DescriptionWorkflowService descriptionWorkflowService;
    private final CustomPolicyService customPolicyService;
    private final AuthorizationService authorizationService;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final QueryFactory queryFactory;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final StorageFileService storageFileService;

    public DescriptionStatusServiceImpl(BuilderFactory builderFactory, DeleterFactory deleterFactory, AuthorizationService authService, TenantEntityManager entityManager, ConventionService conventionService, MessageSource messageSource, XmlHandlingService xmlHandlingService, EventBroker eventBroker, TenantScope tenantScope, DescriptionWorkflowService descriptionWorkflowService, CustomPolicyService customPolicyService, AuthorizationService authorizationService, AuthorizationContentResolver authorizationContentResolver, QueryFactory queryFactory, UsageLimitService usageLimitService, AccountingService accountingService, StorageFileService storageFileService) {
        this.builderFactory = builderFactory;
        this.deleterFactory = deleterFactory;

        this.authService = authService;
        this.entityManager = entityManager;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.eventBroker = eventBroker;
        this.tenantScope = tenantScope;
        this.descriptionWorkflowService = descriptionWorkflowService;
        this.customPolicyService = customPolicyService;
        this.authorizationService = authorizationService;
        this.authorizationContentResolver = authorizationContentResolver;
        this.queryFactory = queryFactory;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.storageFileService = storageFileService;
    }


    @Override
    public DescriptionStatus persist(DescriptionStatusPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data description status").And("model", model).And("fields", fields));

        this.authService.authorizeForce(Permission.EditDescriptionStatus);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        DescriptionStatusEntity data;
        if (isUpdate) {
            data = this.entityManager.find(DescriptionStatusEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.DESCRIPTION_STATUS_COUNT);

            data = new DescriptionStatusEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setName(model.getName());
        data.setDescription(model.getDescription());
        data.setAction(model.getAction());
        data.setOrdinal(model.getOrdinal());
        data.setInternalStatus(model.getInternalStatus());
        DescriptionStatusDefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(data.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(DescriptionStatusDefinitionEntity.class, data.getDefinition());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDescriptionStatusDefinitionEntity(model.getDefinition(), oldDefinition != null ? oldDefinition.getStorageFileId(): null)));
        data.setUpdatedAt(Instant.now());

        if (isUpdate)
            this.entityManager.merge(data);
        else {
            this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_STATUS_COUNT.getValue());
            this.entityManager.persist(data);
        }
        this.entityManager.flush();

        this.eventBroker.emit(new DescriptionStatusTouchedEvent(data.getId(), this.tenantScope.getTenantCode()));

        return this.builderFactory.builder(DescriptionStatusBuilder.class).build(BaseFieldSet.build(fields, DescriptionStatus._id), data);
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting description status: {}", id);

        this.authService.authorizeForce(Permission.DeleteDescriptionStatus);

        this.deleterFactory.deleter(DescriptionStatusDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    private DescriptionStatusDefinitionEntity buildDescriptionStatusDefinitionEntity(DescriptionStatusDefinitionPersist persist, UUID oldStorageFileId) throws InvalidApplicationException {
        DescriptionStatusDefinitionEntity data = new DescriptionStatusDefinitionEntity();
        if (persist == null)
            return data;

        data.setAuthorization(this.buildDescriptionStatusDefinitionAuthorizationEntity(persist.getAuthorization()));
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

    private DescriptionStatusDefinitionAuthorizationEntity buildDescriptionStatusDefinitionAuthorizationEntity(DescriptionStatusDefinitionAuthorizationPersist persist) {
        DescriptionStatusDefinitionAuthorizationEntity data = new DescriptionStatusDefinitionAuthorizationEntity();
        if (persist == null)
            return data;

        data.setEdit(this.buildDescriptionStatusDefinitionAuthorizationItemEntity(persist.getEdit()));

        return data;
    }

    private DescriptionStatusDefinitionAuthorizationItemEntity buildDescriptionStatusDefinitionAuthorizationItemEntity(DescriptionStatusDefinitionAuthorizationItemPersist persist) {
        DescriptionStatusDefinitionAuthorizationItemEntity data = new DescriptionStatusDefinitionAuthorizationItemEntity();
        if (persist == null)
            return data;

        data.setPlanRoles(persist.getPlanRoles());
        data.setRoles(persist.getRoles());
        data.setAllowAnonymous(persist.getAllowAnonymous());
        data.setAllowAuthenticated(persist.getAllowAuthenticated());

        return data;
    }

    @Override
    public Map<UUID, List<UUID>> getAuthorizedAvailableStatusIds(List<UUID> descriptionsIds) {

        Map<UUID, List<UUID>> authorizedStatusMap = new HashMap<>();
        DescriptionWorkflowDefinitionEntity definition;
        try {
            definition = this.descriptionWorkflowService.getActiveWorkFlowDefinition();
        } catch (InvalidApplicationException e) {
            throw new RuntimeException(e);
        }

        List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).ids(descriptionsIds).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._status).ensure(Description._plan));
        List<DescriptionStatusEntity> statusEntities = this.queryFactory.query(DescriptionStatusQuery.class).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionStatus._id));
        for (DescriptionEntity description: descriptionEntities) {
            authorizedStatusMap.put(description.getId(), new ArrayList<>());
            AffiliatedResource affiliatedResource = this.authorizationContentResolver.descriptionAffiliation(description.getId());
            for (DescriptionStatusEntity status: statusEntities) {

                List<DescriptionWorkflowDefinitionTransitionEntity> availableTransitions = definition.getStatusTransitions().stream().filter(x -> x.getFromStatusId().equals(description.getStatusId())).collect(Collectors.toList());
                if (availableTransitions.stream().filter(x -> x.getToStatusId().equals(status.getId())).findFirst().orElse(null) != null) {
                    String editPermission = this.customPolicyService.getDescriptionStatusCanEditStatusPermission(status.getId());
                    Boolean isAllowed = affiliatedResource == null ? this.authorizationService.authorize(editPermission) : this.authorizationService.authorizeAtLeastOne(List.of(affiliatedResource), editPermission);
                    if (isAllowed) authorizedStatusMap.get(description.getId()).add(status.getId());
                }
            }
        }

        return authorizedStatusMap;
    }
}
