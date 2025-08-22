package org.opencdmp.service.user;

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
import gr.cite.tools.validation.ValidatorFactory;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationConfiguration;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.enums.notification.NotificationContactType;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.actionconfirmation.MergeAccountConfirmationEntity;
import org.opencdmp.commons.types.actionconfirmation.RemoveCredentialRequestEntity;
import org.opencdmp.commons.types.actionconfirmation.UserInviteToTenantRequestEntity;
import org.opencdmp.commons.types.notification.*;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.commons.types.user.AdditionalInfoEntity;
import org.opencdmp.commons.types.usercredential.UserCredentialDataEntity;
import org.opencdmp.commons.users.UsersProperties;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.*;
import org.opencdmp.integrationevent.outbox.annotationentitytouch.AnnotationEntityTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandlerImpl;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.userremoval.UserRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.deleter.*;
import org.opencdmp.model.persist.*;
import org.opencdmp.model.persist.actionconfirmation.MergeAccountConfirmationPersist;
import org.opencdmp.model.persist.actionconfirmation.RemoveCredentialRequestPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationUserPersist;
import org.opencdmp.model.persist.referencedefinition.DefinitionPersist;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.actionconfirmation.ActionConfirmationService;
import org.opencdmp.service.elastic.ElasticService;
import org.opencdmp.service.keycloak.KeycloakService;
import org.opencdmp.service.pluginconfiguration.PluginConfigurationService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserServiceImpl.class));

    private final TenantEntityManager entityManager;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final ErrorThesaurusProperties errors;

    private final MessageSource messageSource;
    private final EventBroker eventBroker;
    private final JsonHandlingService jsonHandlingService;
    private final XmlHandlingService xmlHandlingService;
    private final QueryFactory queryFactory;
    private final UserScope userScope;
    private final KeycloakService keycloakService;
    private final ActionConfirmationService actionConfirmationService;
    private final NotificationProperties notificationProperties;
    private final NotifyIntegrationEventHandler eventHandler;
    private final ValidatorFactory validatorFactory;
    private final ElasticService elasticService;
    private final UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler;
    private final UserRemovalIntegrationEventHandler userRemovalIntegrationEventHandler;
    private final IndicatorAccessEventHandlerImpl indicatorAccessEventHandler;
    private final AuthorizationConfiguration authorizationConfiguration;
    private final TenantScope tenantScope;
    private final AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final UsersProperties usersProperties;
    private final PluginConfigurationService pluginConfigurationService;

    @Autowired
    public UserServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            EventBroker eventBroker,
            JsonHandlingService jsonHandlingService,
            XmlHandlingService xmlHandlingService, QueryFactory queryFactory,
            UserScope userScope, KeycloakService keycloakService, ActionConfirmationService actionConfirmationService, NotificationProperties notificationProperties, NotifyIntegrationEventHandler eventHandler, ValidatorFactory validatorFactory, ElasticService elasticService, UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler, UserRemovalIntegrationEventHandler userRemovalIntegrationEventHandler, IndicatorAccessEventHandlerImpl indicatorAccessEventHandler, AuthorizationConfiguration authorizationConfiguration, TenantScope tenantScope, AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler, UsageLimitService usageLimitService, AccountingService accountingService, UsersProperties usersProperties, PluginConfigurationService pluginConfigurationService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.eventBroker = eventBroker;
        this.jsonHandlingService = jsonHandlingService;
        this.xmlHandlingService = xmlHandlingService;
        this.queryFactory = queryFactory;
        this.userScope = userScope;
        this.keycloakService = keycloakService;
        this.actionConfirmationService = actionConfirmationService;
        this.notificationProperties = notificationProperties;
        this.eventHandler = eventHandler;
        this.validatorFactory = validatorFactory;
	    this.elasticService = elasticService;
	    this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
	    this.userRemovalIntegrationEventHandler = userRemovalIntegrationEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.authorizationConfiguration = authorizationConfiguration;
	    this.tenantScope = tenantScope;
	    this.annotationEntityTouchedIntegrationEventHandler = annotationEntityTouchedIntegrationEventHandler;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.usersProperties = usersProperties;
        this.pluginConfigurationService = pluginConfigurationService;
    }

    //region persist
    
    @Override
    public User persist(UserPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JsonProcessingException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("persisting data User").And("model", model).And("fields", fields));

        this.authorizationService.authorizeAtLeastOneForce(model.getId() != null ? List.of(new OwnedResource(model.getId())) : null, Permission.EditUser);
        
        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        UserEntity data;
        if (isUpdate) {
            data = this.entityManager.find(UserEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new UserEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        AdditionalInfoEntity oldAdditionalInfo = this.conventionService.isNullOrEmpty(data.getAdditionalInfo()) ? null : this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, data.getAdditionalInfo());
        data.setAdditionalInfo(this.jsonHandlingService.toJson(this.buildAdditionalInfoEntity(model.getAdditionalInfo(), oldAdditionalInfo)));
        
        data.setName(model.getName());
        data.setUpdatedAt(Instant.now());
        if (isUpdate) this.entityManager.merge(data);
        else this.entityManager.persist(data);

        this.entityManager.flush();

        this.eventBroker.emit(new UserTouchedEvent(data.getId()));

        this.userTouchedIntegrationEventHandler.handle(data.getId());
        return this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, User._id), data);
    }

    private @NotNull AdditionalInfoEntity buildAdditionalInfoEntity(UserAdditionalInfoPersist persist, AdditionalInfoEntity oldAdditionalInfo) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        AdditionalInfoEntity data = new AdditionalInfoEntity();
        if (persist == null) return data;
        if (persist.getOrganization() != null) {
            ReferenceEntity organization = this.buildReferenceEntity(persist.getOrganization());
            data.setOrganizationId(organization.getId());
        }
        data.setRoleOrganization(persist.getRoleOrganization());
        data.setCulture(persist.getCulture());
        data.setTimezone(persist.getTimezone());
        data.setLanguage(persist.getLanguage());
        data.setAvatarUrl(persist.getAvatarUrl());
        if (!this.conventionService.isListNullOrEmpty(persist.getPluginConfigurations())) {
            data.setPluginConfigurations(new ArrayList<>());
            for (PluginConfigurationUserPersist pluginConfigurationUser : persist.getPluginConfigurations()) {
                data.getPluginConfigurations().add(this.pluginConfigurationService.buildPluginConfigurationUserEntity(pluginConfigurationUser, oldAdditionalInfo != null ? oldAdditionalInfo.getPluginConfigurations() : null));
            }
        }

        return data;
    }

    private @NotNull ReferenceEntity buildReferenceEntity(ReferencePersist model) throws InvalidApplicationException {

        ReferenceEntity referenceEntity;
        if (this.conventionService.isValidGuid(model.getId())) {
            referenceEntity = this.entityManager.find(ReferenceEntity.class, model.getId());
            if (referenceEntity == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Reference.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        } else {
            referenceEntity = this.queryFactory.query(ReferenceQuery.class).sourceTypes(model.getSourceType()).typeIds(model.getTypeId()).sources(model.getSource()).isActive(IsActive.Active).references(model.getReference()).first();
            if (referenceEntity == null) {
                this.usageLimitService.checkIncrease(UsageLimitTargetMetric.REFERENCE_COUNT);

                referenceEntity = new ReferenceEntity();
                referenceEntity.setId(UUID.randomUUID());
                referenceEntity.setLabel(model.getLabel());
                referenceEntity.setDescription(model.getDescription());
                referenceEntity.setIsActive(IsActive.Active);
                referenceEntity.setCreatedAt(Instant.now());
                referenceEntity.setTypeId(model.getTypeId());

                referenceEntity.setDefinition(this.xmlHandlingService.toXmlSafe(this.buildDefinitionEntity(model.getDefinition())));
                referenceEntity.setUpdatedAt(Instant.now());
                referenceEntity.setReference(model.getReference());
                referenceEntity.setAbbreviation(model.getAbbreviation());
                referenceEntity.setSource(model.getSource());
                referenceEntity.setSourceType(model.getSourceType());
                try {
                    ReferenceTypeEntity referenceType = this.queryFactory.query(ReferenceTypeQuery.class).ids(model.getTypeId()).firstAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceTypeEntity._tenantId));
                    if (referenceType == null)
                        throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getTypeId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                    if (referenceEntity.getSourceType().equals(ReferenceSourceType.External) && !this.tenantScope.isDefaultTenant() && referenceType.getTenantId() == null) {
                        this.tenantScope.setTempTenant(this.entityManager, null, this.tenantScope.getDefaultTenantCode());
                    }
                    this.entityManager.persist(referenceEntity);
                    this.accountingService.increase(UsageLimitTargetMetric.REFERENCE_COUNT.getValue());
                    this.accountingService.increase(UsageLimitTargetMetric.REFERENCE_BY_TYPE_COUNT.getValue().replace("{type_code}", referenceType.getCode()));
                } finally {
                    this.tenantScope.removeTempTenant(this.entityManager);
                }
            }
        }

        this.entityManager.flush();

        return referenceEntity;
    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist) {
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null) return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getFields())) {
            data.setFields(new ArrayList<>());
            for (org.opencdmp.model.persist.referencedefinition.FieldPersist fieldPersist : persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }

        return data;
    }

    private @NotNull org.opencdmp.commons.types.reference.FieldEntity buildFieldEntity(org.opencdmp.model.persist.referencedefinition.FieldPersist persist) {
        org.opencdmp.commons.types.reference.FieldEntity data = new org.opencdmp.commons.types.reference.FieldEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setDataType(persist.getDataType());
        data.setValue(persist.getValue());

        return data;
    }

    //endregion
    
    //region delete

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting User: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteUser);
        try {
            this.entityManager.disableTenantFilters();
            this.deleterFactory.deleter(UserDeleter.class).deleteAndSaveByIds(List.of(id));
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        this.userRemovalIntegrationEventHandler.handle(id);

        this.eventBroker.emit(new UserTouchedEvent(id));
    }
    
    //endregion
    
    //region export

    @Override
    public byte[] exportCsv(boolean hasTenantAdminMode) throws IOException, InvalidApplicationException {
        this.authorizationService.authorizeForce(Permission.ExportUsers);
        
        FieldSet fieldSet = new BaseFieldSet().ensure(User._id).ensure(User._name).ensure(User._contacts + "." + UserContactInfo._value).ensure(User._contacts + "." + UserContactInfo._type);
        List<User> users = null;
        if (hasTenantAdminMode && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            if (this.tenantScope.getTenant() == null) throw new MyApplicationException("Tenant not found");
            TenantUserQuery tenantUserQuery = this.queryFactory.query(TenantUserQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).tenantIds(this.tenantScope.getTenant()).isActive(IsActive.Active);
            users = this.builderFactory.builder(UserBuilder.class).build(fieldSet, this.queryFactory.query(UserQuery.class).tenantUserSubQuery(tenantUserQuery).isActive(IsActive.Active).disableTracking().collectAs(fieldSet));
        } else {
            users = this.builderFactory.builder(UserBuilder.class).build(fieldSet, this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).collectAs(fieldSet));
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("User Id", "User Name", "User Email").withQuoteMode(QuoteMode.NON_NUMERIC);
        final CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);

        for (User user : users) {
            csvPrinter.printRecord(user.getId(), user.getName(), (user.getContacts() != null ? String.join(" ", user.getContacts().stream().filter(x-> ContactInfoType.Email.equals(x.getType())).map(UserContactInfo::getValue).toList()) : ""));
        }
            
        csvPrinter.flush();
        return out.toByteArray();
    }

    //endregion
    
    
    @Override
    public User patchRoles(UserRolePatchPersist model, FieldSet fields) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data UserRole").And("model", model).And("fields", fields));

        if (!model.getHasTenantAdminMode()) {
            this.authorizationService.authorizeForce(Permission.EditUser);
            boolean foundGlobalRole = false;
            for (String role: model.getRoles()) {
                if (authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles().contains(role)) foundGlobalRole = true;
            }
            if (!foundGlobalRole) throw new MyValidationException(this.errors.getMissingGlobalRole().getCode(), this.errors.getMissingGlobalRole().getMessage());
        } else {
            this.authorizationService.authorizeForce(Permission.EditTenantUserRole);
        }

        UserEntity data = this.entityManager.find(UserEntity.class, model.getId(), true);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());

        List<UserCredentialEntity> userCredentials = this.queryFactory.query(UserCredentialQuery.class).disableTracking().userIds(data.getId()).collect();
        if (userCredentials.isEmpty())
            throw new MyApplicationException(this.errors.getUserProfileInactive().getCode(), this.errors.getUserProfileInactive().getMessage());
        if (userCredentials.getFirst().getExternalId() == null)
            throw new MyApplicationException(this.errors.getUserProfileInactive().getCode(), this.errors.getUserProfileInactive().getMessage());

        if (!model.getHasTenantAdminMode()) this.applyGlobalRoles(data.getId(), model);
        
        this.applyTenantRoles(data.getId(), model);

        this.entityManager.flush();
        
        this.eventBroker.emit(new UserTouchedEvent(data.getId()));

        this.syncKeycloakRoles(data.getId());

        if (model.getRoles().stream().noneMatch(authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()::contains)){
            this.deleteTenantUser(model.getId());
        }
        
        this.userTouchedIntegrationEventHandler.handle(data.getId());
        this.indicatorAccessEventHandler.handle(data.getId());
        return this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, User._id), data);
    }
    
    private void applyGlobalRoles(UUID userId, UserRolePatchPersist model) throws InvalidApplicationException {
        try {
            this.entityManager.disableTenantFilters();
            
            List<UserRoleEntity> existingItems = this.queryFactory.query(UserRoleQuery.class).userIds(userId).tenantIsSet(false).roles(this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles()).collect();
            List<UUID> foundIds = new ArrayList<>();
            for (String roleName : model.getRoles().stream().filter(x -> x != null && !x.isBlank() && this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles().contains(x)).distinct().toList()) {
                UserRoleEntity item = existingItems.stream().filter(x -> x.getRole().equals(roleName)).findFirst().orElse(null);
                if (item == null) {
                    item = new UserRoleEntity();
                    item.setId(UUID.randomUUID());
                    item.setUserId(userId);
                    item.setRole(roleName);
                    item.setCreatedAt(Instant.now());
                    this.entityManager.persist(item);
                }
                foundIds.add(item.getId());
            }

            this.entityManager.flush();

            List<UserRoleEntity> toDelete = existingItems.stream().filter(x -> foundIds.stream().noneMatch(y -> y.equals(x.getId()))).collect(Collectors.toList());
            this.deleterFactory.deleter(UserRoleDeleter.class).deleteAndSave(toDelete);

            this.entityManager.flush();
        } finally {
            this.entityManager.reloadTenantFilters();
        }
    }

    private void applyTenantRoles(UUID userId, UserRolePatchPersist model) throws InvalidApplicationException {
        if (!this.tenantScope.isSet())  throw new MyForbiddenException("tenant scope required");

        UserRoleQuery userRoleQuery = this.queryFactory.query(UserRoleQuery.class).userIds(userId).roles(this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles());
        if (this.tenantScope.isDefaultTenant()) userRoleQuery.tenantIsSet(false);
        else userRoleQuery.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());

        boolean hasTenantUser = this.queryFactory.query(TenantUserQuery.class).isActive(IsActive.Active).userIds(userId).count() > 0;
        
        List<UserRoleEntity> existingItems = userRoleQuery.collect();
        List<UUID> foundIds = new ArrayList<>();
        for (String roleName : model.getRoles().stream().filter(x-> x != null && !x.isBlank() && this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles().contains(x)).distinct().toList()) {
            UserRoleEntity item =  existingItems.stream().filter(x-> x.getRole().equals(roleName)).findFirst().orElse(null);
            if (item == null) {
                item = new UserRoleEntity();
                item.setId(UUID.randomUUID());
                item.setUserId(userId);
                item.setRole(roleName);
                item.setCreatedAt(Instant.now());
                item.setTenantId(this.tenantScope.getTenant());
                this.entityManager.persist(item);
            }
            foundIds.add(item.getId());
        }

        if (!hasTenantUser && !model.getRoles().isEmpty() && !this.tenantScope.isDefaultTenant()){
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.USER_COUNT);
            TenantUserEntity tenantUserEntity = new TenantUserEntity();
            tenantUserEntity.setId(UUID.randomUUID());
            tenantUserEntity.setUserId(userId);
            tenantUserEntity.setIsActive(IsActive.Active);
            tenantUserEntity.setTenantId(this.tenantScope.getTenant());
            tenantUserEntity.setCreatedAt(Instant.now());
            tenantUserEntity.setUpdatedAt(Instant.now());
            this.entityManager.persist(tenantUserEntity);

            this.eventBroker.emit(new UserAddedToTenantEvent(tenantUserEntity.getUserId(), tenantUserEntity.getTenantId()));
            this.accountingService.increase(UsageLimitTargetMetric.USER_COUNT.getValue());
        }

        this.entityManager.flush();

        List<UserRoleEntity> toDelete = existingItems.stream().filter(x-> foundIds.stream().noneMatch(y-> y.equals(x.getId()))).collect(Collectors.toList());
        this.deleterFactory.deleter(UserRoleDeleter.class).deleteAndSave(toDelete);

        this.entityManager.flush();
       
    }

    private void deleteTenantUser(UUID userId) throws InvalidApplicationException {
        if (!this.tenantScope.isSet())  throw new MyForbiddenException("tenant scope required");
        if (this.tenantScope.isDefaultTenant()) return;

        TenantUserEntity tenantUser = this.queryFactory.query(TenantUserQuery.class).isActive(IsActive.Active).userIds(userId).tenantIds(this.tenantScope.getTenant()).first();
        if (tenantUser == null) throw new MyApplicationException("tenant user not found");

        this.deleterFactory.deleter(TenantUserDeleter.class).delete(List.of(tenantUser));
    }

    //region mine

    @Override
    public void updateLanguageMine(String language) throws JsonProcessingException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting User language").And("language", language));
        
        UUID userId = this.userScope.getUserIdSafe();

        if (userId == null) throw new MyForbiddenException(this.errors.getForbidden().getCode(),  this.errors.getForbidden().getMessage());
       

        UserEntity data = this.entityManager.find(UserEntity.class, userId);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userId, User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
       
        AdditionalInfoEntity additionalInfoEntity = this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, data.getAdditionalInfo());
        if (additionalInfoEntity == null) additionalInfoEntity = new AdditionalInfoEntity();
        additionalInfoEntity.setLanguage(language);
        
        data.setAdditionalInfo(this.jsonHandlingService.toJson(additionalInfoEntity));

        data.setUpdatedAt(Instant.now());
        this.entityManager.merge(data);

        this.entityManager.flush();

        this.userTouchedIntegrationEventHandler.handle(data.getId());

        this.eventBroker.emit(new UserTouchedEvent(data.getId()));
    }

    @Override
    public void updateTimezoneMine(String timezone) throws JsonProcessingException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting User timezone").And("timezone", timezone));

        UUID userId = this.userScope.getUserIdSafe();

        if (userId == null) throw new MyForbiddenException(this.errors.getForbidden().getCode(),  this.errors.getForbidden().getMessage());


        UserEntity data = this.entityManager.find(UserEntity.class, userId);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userId, User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        AdditionalInfoEntity additionalInfoEntity = this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, data.getAdditionalInfo());
        if (additionalInfoEntity == null) additionalInfoEntity = new AdditionalInfoEntity();
        additionalInfoEntity.setTimezone(timezone);

        data.setAdditionalInfo(this.jsonHandlingService.toJson(additionalInfoEntity));

        data.setUpdatedAt(Instant.now());
        this.entityManager.merge(data);

        this.entityManager.flush();

        this.userTouchedIntegrationEventHandler.handle(data.getId());

        this.eventBroker.emit(new UserTouchedEvent(data.getId()));
    }

    @Override
    public void updateCultureMine(String culture) throws JsonProcessingException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting User culture").And("culture", culture));

        UUID userId = this.userScope.getUserIdSafe();

        if (userId == null) throw new MyForbiddenException(this.errors.getForbidden().getCode(),  this.errors.getForbidden().getMessage());

        UserEntity data = this.entityManager.find(UserEntity.class, userId);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userId, User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        AdditionalInfoEntity additionalInfoEntity = this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, data.getAdditionalInfo());
        if (additionalInfoEntity == null) additionalInfoEntity = new AdditionalInfoEntity();
        additionalInfoEntity.setCulture(culture);

        data.setAdditionalInfo(this.jsonHandlingService.toJson(additionalInfoEntity));

        data.setUpdatedAt(Instant.now());
        this.entityManager.merge(data);

        this.entityManager.flush();

        this.userTouchedIntegrationEventHandler.handle(data.getId());
        
        this.eventBroker.emit(new UserTouchedEvent(data.getId()));
    }
    
    //endregion

    //notifications
    public void sendMergeAccountConfirmation(UserMergeRequestPersist model) throws InvalidApplicationException, JAXBException {
        UserContactInfoEntity userContactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().values(model.getEmail()).types(ContactInfoType.Email).first();
        if (userContactInfoEntity == null) throw new MyValidationException(this.errors.getInvalidUserEmail().getCode(), this.errors.getInvalidUserEmail().getMessage());
        
        UserEntity user = this.queryFactory.query(UserQuery.class).ids(userContactInfoEntity.getUserId()).isActive(IsActive.Active).first();
        if (user == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userContactInfoEntity.getUserId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (this.userScope.getUserIdSafe() == null) throw new MyForbiddenException(this.errors.getForbidden().getCode(),  this.errors.getForbidden().getMessage());
        
        String token = this.createMergeAccountConfirmation(model.getEmail());
	    this.createMergeNotificationEvent(token, user);
    }

    private void createMergeNotificationEvent(String token, UserEntity user) throws InvalidApplicationException {
        UserEntity currentUser = this.entityManager.find(UserEntity.class,  this.userScope.getUserIdSafe());
        if (currentUser == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{ this.userScope.getUserIdSafe(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        event.setNotificationType(this.notificationProperties.getMergeAccountConfirmationType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{userName}", DataType.String, currentUser.getName()));
        fieldInfoList.add(new FieldInfo("{confirmationToken}", DataType.String, token));
        fieldInfoList.add(new FieldInfo("{expiration_time}", DataType.String, this.secondsToTime(this.usersProperties.getEmailExpirationTimeSeconds().getMergeAccountExpiration())));
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));
	    this.eventHandler.handle(event);
    }

    public void sendRemoveCredentialConfirmation(RemoveCredentialRequestPersist model) throws InvalidApplicationException, JAXBException {
        UserCredentialEntity data = this.entityManager.find(UserCredentialEntity.class, model.getCredentialId(), true);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getCredentialId(), UserCredentialEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!data.getUserId().equals(this.userScope.getUserId())) throw new MyForbiddenException(this.errors.getForbidden().getCode(),  this.errors.getForbidden().getMessage());
        String email = null;
        if (data.getData() != null){
            UserCredentialDataEntity userCredentialDataEntity = this.jsonHandlingService.fromJsonSafe(UserCredentialDataEntity.class, data.getData());
            email = userCredentialDataEntity.getEmail();
        }

        String token = this.createRemoveConfirmation(data.getId());
        this.createRemoveCredentialNotificationEvent(token, data.getUserId(), email);
    }

    private void createRemoveCredentialNotificationEvent(String token, UUID userId, String email) throws InvalidApplicationException {
        UserEntity user = this.entityManager.find(UserEntity.class, userId, true);
        if (user == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userId, UserEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        if (email != null) {
            List<ContactPair> contactPairs = new ArrayList<>();
            contactPairs.add(new ContactPair(ContactInfoType.Email, email));
            NotificationContactData contactData = new NotificationContactData(contactPairs, null, null);
            event.setContactHint(this.jsonHandlingService.toJsonSafe(contactData));
        } else event.setUserId(userId);

        event.setContactTypeHint(NotificationContactType.EMAIL);
        event.setNotificationType(this.notificationProperties.getRemoveCredentialConfirmationType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{confirmationToken}", DataType.String, token));
        fieldInfoList.add(new FieldInfo("{expiration_time}", DataType.String, this.secondsToTime(this.usersProperties.getEmailExpirationTimeSeconds().getRemoveCredentialExpiration())));
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));
	    this.eventHandler.handle(event);
    }

    private String createMergeAccountConfirmation(String email) throws JAXBException, InvalidApplicationException {
        ActionConfirmationPersist persist = new ActionConfirmationPersist();
        persist.setType(ActionConfirmationType.MergeAccount);
        persist.setStatus(ActionConfirmationStatus.Requested);
        persist.setToken(UUID.randomUUID().toString());
        persist.setMergeAccountConfirmation(new MergeAccountConfirmationPersist());
        persist.getMergeAccountConfirmation().setEmail(email);
        persist.setExpiresAt(Instant.now().plusSeconds(this.usersProperties.getEmailExpirationTimeSeconds().getMergeAccountExpiration()));
        this.validatorFactory.validator(ActionConfirmationPersist.ActionConfirmationPersistValidator.class).validateForce(persist);
        this.actionConfirmationService.persist(persist, null);
        
        try {
            this.entityManager.disableTenantFilters();
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        return persist.getToken();
    }

    private String createRemoveConfirmation(UUID credentialId) throws JAXBException, InvalidApplicationException {
        ActionConfirmationPersist persist = new ActionConfirmationPersist();
        persist.setType(ActionConfirmationType.RemoveCredential);
        persist.setStatus(ActionConfirmationStatus.Requested);
        persist.setToken(UUID.randomUUID().toString());
        persist.setRemoveCredentialRequest(new RemoveCredentialRequestPersist());
        persist.getRemoveCredentialRequest().setCredentialId(credentialId);
        persist.setExpiresAt(Instant.now().plusSeconds(this.usersProperties.getEmailExpirationTimeSeconds().getRemoveCredentialExpiration()));
        this.validatorFactory.validator(ActionConfirmationPersist.ActionConfirmationPersistValidator.class).validateForce(persist);
        try {
            this.entityManager.disableTenantFilters();
            this.actionConfirmationService.persist(persist, null);
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        return persist.getToken();
    }

    private String secondsToTime(int seconds) {
        int sec = seconds % 60;
        int hour = seconds / 60;
        int min = hour % 60;
        hour = hour / 60;
        return (String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec));
    }

    public boolean doesTokenBelongToLoggedInUser(String token) throws IOException, InvalidApplicationException {
        ActionConfirmationEntity action;
        try {
            this.entityManager.disableTenantFilters();
            action = this.queryFactory.query(ActionConfirmationQuery.class).disableTracking().tokens(token).types(ActionConfirmationType.MergeAccount).isActive(IsActive.Active).first();
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        if (action == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{token, ActionConfirmationEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.checkActionState(action, false);

        MergeAccountConfirmationEntity mergeAccountConfirmationEntity = this.xmlHandlingService.fromXmlSafe(MergeAccountConfirmationEntity.class, action.getData());
        if (mergeAccountConfirmationEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{action.getId(), MergeAccountConfirmationEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        UserContactInfoEntity userContactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().values(mergeAccountConfirmationEntity.getEmail()).types(ContactInfoType.Email).first();
        if (userContactInfoEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{mergeAccountConfirmationEntity.getEmail(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        UserEntity userToBeMerge = this.queryFactory.query(UserQuery.class).disableTracking().ids(userContactInfoEntity.getUserId()).isActive(IsActive.Active).first();

        if (userToBeMerge == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userContactInfoEntity.getUserId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!this.userScope.getUserIdSafe().equals(userToBeMerge.getId())) throw new MyValidationException(this.errors.getAnotherUserToken().getCode(), this.errors.getAnotherUserToken().getMessage());

        return this.userScope.getUserIdSafe().equals(userToBeMerge.getId());
    }

    public void confirmMergeAccount(String token) throws IOException, InvalidApplicationException {
        ActionConfirmationEntity action;
        try {
            this.entityManager.disableTenantFilters();
            action = this.queryFactory.query(ActionConfirmationQuery.class).tokens(token).types(ActionConfirmationType.MergeAccount).isActive(IsActive.Active).first();
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        if (action == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{token, ActionConfirmationEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        this.checkActionState(action, false);

        MergeAccountConfirmationEntity mergeAccountConfirmationEntity = this.xmlHandlingService.fromXmlSafe(MergeAccountConfirmationEntity.class, action.getData());
        if (mergeAccountConfirmationEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{action.getId(), MergeAccountConfirmationEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        UserContactInfoEntity userContactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).values(mergeAccountConfirmationEntity.getEmail()).types(ContactInfoType.Email).first();
        if (userContactInfoEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{mergeAccountConfirmationEntity.getEmail(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        UserEntity userToBeMerge = this.queryFactory.query(UserQuery.class).ids(userContactInfoEntity.getUserId()).isActive(IsActive.Active).first();
        if (userToBeMerge == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userContactInfoEntity.getUserId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!this.userScope.getUserIdSafe().equals(userToBeMerge.getId())) throw new MyValidationException(this.errors.getAnotherUserToken().getCode(), this.errors.getAnotherUserToken().getMessage());;

        UserEntity newUser = this.queryFactory.query(UserQuery.class).ids(action.getCreatedById()).isActive(IsActive.Active).first();
        if (newUser == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{action.getCreatedById(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!newUser.getId().equals(userToBeMerge.getId())){
            this.mergeNewUserToOld(newUser, userToBeMerge);
        }

        this.entityManager.flush();
        try {
            this.entityManager.disableTenantFilters();
            action.setUpdatedAt(Instant.now());
            action.setStatus(ActionConfirmationStatus.Accepted);
            this.entityManager.merge(action);
            
            this.entityManager.flush();
        } finally {
            this.entityManager.reloadTenantFilters();
        }

        if (!newUser.getId().equals(userToBeMerge.getId())) {
            this.syncKeycloakRoles(newUser.getId());
        }

        this.userTouchedIntegrationEventHandler.handle(newUser.getId());
        this.userRemovalIntegrationEventHandler.handle(userToBeMerge.getId());

        this.indicatorAccessEventHandler.handle(newUser.getId());

        this.eventBroker.emit(new UserTouchedEvent(newUser.getId()));
        this.eventBroker.emit(new UserTouchedEvent(userToBeMerge.getId()));
    }
    
    private void syncKeycloakRoles(UUID userId) throws InvalidApplicationException {
        try {
            this.entityManager.disableTenantFilters();
            List<UserCredentialEntity> userCredentials = this.queryFactory.query(UserCredentialQuery.class).disableTracking().userIds(userId).collect();
            List<UserRoleEntity> userRoles = this.queryFactory.query(UserRoleQuery.class).disableTracking().userIds(userId).collect();
            List<TenantEntity> tenants = this.queryFactory.query(TenantQuery.class).disableTracking().ids(userRoles.stream().map(TenantScopedBaseEntity::getTenantId).filter(Objects::nonNull).toList()).collect();

            for (UserCredentialEntity userCredential : userCredentials){
                this.keycloakService.removeFromAllGroups(userCredential.getExternalId());
                for (UserRoleEntity userRole : userRoles) {
                    if (this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles().contains(userRole.getRole())){
                        this.keycloakService.addUserToGlobalRoleGroup(userCredential.getExternalId(), userRole.getRole());
                    } else if (this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles().contains(userRole.getRole())){
                        String tenantCode = userRole.getTenantId() == null ? this.tenantScope.getDefaultTenantCode() : tenants.stream().filter(x-> x.getId().equals(userRole.getTenantId())).map(TenantEntity::getCode).findFirst().orElse(null);
                        if (!this.conventionService.isNullOrEmpty(tenantCode)) this.keycloakService.addUserToTenantRoleGroup(userCredential.getExternalId(), tenantCode, userRole.getRole());
                    }
                }

                this.eventBroker.emit(new UserCredentialTouchedEvent(userCredential.getId(), userCredential.getExternalId()));
            }
            
        } finally {
            this.entityManager.reloadTenantFilters();
        }
    }

    private void mergeNewUserToOld(UserEntity newUser, UserEntity oldUser) throws IOException, InvalidApplicationException {
        try {
            this.entityManager.disableTenantFilters();
            List<UserCredentialEntity> userCredentials = this.queryFactory.query(UserCredentialQuery.class).userIds(oldUser.getId()).collect();
            for (UserCredentialEntity userCredential : userCredentials) {
                userCredential.setUserId(newUser.getId());
                this.entityManager.merge(userCredential);

                this.eventBroker.emit(new UserCredentialTouchedEvent(userCredential.getId(), userCredential.getExternalId()));
            }

            List<UserContactInfoEntity> userContacts = this.queryFactory.query(UserContactInfoQuery.class).userIds(oldUser.getId()).collect();
            UserContactInfoQuery newUserContactInfoQuery = this.queryFactory.query(UserContactInfoQuery.class).userIds(newUser.getId());
            newUserContactInfoQuery.setOrder(new Ordering().addDescending(UserContactInfo._ordinal));
            UserContactInfoEntity newUserContactInfo = newUserContactInfoQuery.first();
            int ordinal = newUserContactInfo == null ? 0 : newUserContactInfo.getOrdinal() + 1;
            for (UserContactInfoEntity userContactInfo : userContacts) {
                userContactInfo.setUserId(newUser.getId());
                userContactInfo.setOrdinal(ordinal);
                this.entityManager.merge(userContactInfo);
                ordinal++;
            }

            List<UserRoleEntity> userRoles = this.queryFactory.query(UserRoleQuery.class).userIds(oldUser.getId()).collect();
            List<UserRoleEntity> newUserRoles = this.queryFactory.query(UserRoleQuery.class).userIds(newUser.getId()).collect();
            List<UserRoleEntity> rolesToDelete = new ArrayList<>();
            for (UserRoleEntity userRole : userRoles) {
                if (newUserRoles.stream().anyMatch(x -> Objects.equals(x.getTenantId(), userRole.getTenantId())  && x.getRole().equals(userRole.getRole()))) {
                    rolesToDelete.add(userRole);
                } else {
                    userRole.setUserId(newUser.getId());
                    this.entityManager.merge(userRole);
                }
            }
            this.deleterFactory.deleter(UserRoleDeleter.class).delete(rolesToDelete);

            List<TenantUserEntity> userTenantUsers = this.queryFactory.query(TenantUserQuery.class).userIds(oldUser.getId()).collect();
            List<TenantUserEntity> newTenantUsers = this.queryFactory.query(TenantUserQuery.class).userIds(newUser.getId()).collect();
            List<TenantUserEntity> tenantUsersToDelete = new ArrayList<>();
            for (TenantUserEntity userTenantUser : userTenantUsers) {
                if (newTenantUsers.stream().anyMatch(x -> Objects.equals(x.getTenantId(), userTenantUser.getTenantId()))) {
                    tenantUsersToDelete.add(userTenantUser);
                } else {
                    this.eventBroker.emit(new UserRemovedFromTenantEvent(userTenantUser.getUserId(), userTenantUser.getTenantId()));
                    this.accountingService.decrease(UsageLimitTargetMetric.USER_COUNT.getValue());
                    userTenantUser.setUserId(newUser.getId());
                    this.entityManager.merge(userTenantUser);
                    this.eventBroker.emit(new UserAddedToTenantEvent(userTenantUser.getUserId(), userTenantUser.getTenantId()));
                    this.accountingService.increase(UsageLimitTargetMetric.USER_COUNT.getValue());
                }
            }
            this.deleterFactory.deleter(TenantUserDeleter.class).delete(tenantUsersToDelete);

            List<UserSettingsEntity> userSettings = this.queryFactory.query(UserSettingsQuery.class).entityIds(oldUser.getId()).collect();
            List<UserSettingsEntity> newUserSettings = this.queryFactory.query(UserSettingsQuery.class).entityIds(newUser.getId()).collect();
            List<UserSettingsEntity> userSettingsToDelete = new ArrayList<>();
            for (UserSettingsEntity userSetting : userSettings) {
                if (newUserSettings.stream().anyMatch(x -> Objects.equals(x.getTenantId(), userSetting.getTenantId())  &&x.getKey().equals(userSetting.getKey()))) {
                    userSettingsToDelete.add(userSetting);
                } else {
                    userSetting.setEntityId(newUser.getId());
                    this.entityManager.merge(userSetting);
                }
            }
            this.deleterFactory.deleter(UserSettingsSettingsDeleter.class).delete(userSettingsToDelete);

            List<TagEntity> tags = this.queryFactory.query(TagQuery.class).createdByIds(oldUser.getId()).collect();
            for (TagEntity tag : tags) {
                tag.setCreatedById(newUser.getId());
                this.entityManager.merge(tag);
            }

            List<StorageFileEntity> storageFiles = this.queryFactory.query(StorageFileQuery.class).ownerIds(oldUser.getId()).collect();
            for (StorageFileEntity storageFile : storageFiles) {
                storageFile.setOwnerId(newUser.getId());
                this.entityManager.merge(storageFile);
            }

            List<LockEntity> locks = this.queryFactory.query(LockQuery.class).lockedByIds(oldUser.getId()).collect();
            for (LockEntity lock : locks) {
                lock.setLockedBy(newUser.getId());
                this.entityManager.merge(lock);
            }

            List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).userIds(oldUser.getId()).collect();
            for (PlanUserEntity planUser : planUsers) {
                planUser.setUserId(newUser.getId());
                this.entityManager.merge(planUser);
            }

            List<UserDescriptionTemplateEntity> userDescriptionTemplates = this.queryFactory.query(UserDescriptionTemplateQuery.class).userIds(oldUser.getId()).collect();
            for (UserDescriptionTemplateEntity userDescriptionTemplate : userDescriptionTemplates) {
                userDescriptionTemplate.setUserId(newUser.getId());
                this.entityManager.merge(userDescriptionTemplate);
            }

            List<PlanEntity> plans = this.queryFactory.query(PlanQuery.class).creatorIds(oldUser.getId()).collect();
            for (PlanEntity plan : plans) {
                plan.setCreatorId(newUser.getId());
                this.entityManager.merge(plan);
            }

            List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class).createdByIds(oldUser.getId()).collect();
            for (DescriptionEntity description : descriptions) {
                description.setCreatedById(newUser.getId());
                this.entityManager.merge(description);
            }

            oldUser.setIsActive(IsActive.Inactive);

            this.entityManager.merge(oldUser);

            this.entityManager.flush();

            for (PlanEntity plan : plans) {
                this.elasticService.persistPlan(plan);
            }

            for (DescriptionEntity description : descriptions) {
                this.elasticService.persistDescription(description);
            }
            
            for (PlanEntity plan : plans) {
                this.annotationEntityTouchedIntegrationEventHandler.handlePlan(plan.getId());
            }

            for (DescriptionEntity description : descriptions) {
                this.annotationEntityTouchedIntegrationEventHandler.handleDescription(description.getId());
            }
        } finally {
            this.entityManager.reloadTenantFilters();
        }
    }

    public void confirmRemoveCredential(String token) throws InvalidApplicationException {
        ActionConfirmationEntity action;
        try {
            this.entityManager.disableTenantFilters();
            action = this.queryFactory.query(ActionConfirmationQuery.class).tokens(token).types(ActionConfirmationType.RemoveCredential).isActive(IsActive.Active).first();
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        if (action == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{token, ActionConfirmationEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        this.checkActionState(action, false);

        RemoveCredentialRequestEntity removeCredentialRequestEntity = this.xmlHandlingService.fromXmlSafe(RemoveCredentialRequestEntity.class, action.getData());
        if (removeCredentialRequestEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{action.getId(), RemoveCredentialRequestEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        UserCredentialEntity userCredentialEntity = this.queryFactory.query(UserCredentialQuery.class).ids(removeCredentialRequestEntity.getCredentialId()).first();
        if (userCredentialEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{removeCredentialRequestEntity.getCredentialId(), UserCredential.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!this.userScope.getUserIdSafe().equals(userCredentialEntity.getUserId())) throw new MyValidationException(this.errors.getAnotherUserToken().getCode(), this.errors.getAnotherUserToken().getMessage());

        if (userCredentialEntity.getData() != null){
            UserCredentialDataEntity userCredentialDataEntity = this.jsonHandlingService.fromJsonSafe(UserCredentialDataEntity.class, userCredentialEntity.getData());
            if (userCredentialDataEntity != null && !this.conventionService.isNullOrEmpty(userCredentialDataEntity.getEmail())) {
                List<UserContactInfoEntity> userContacts = this.queryFactory.query(UserContactInfoQuery.class).values(userCredentialDataEntity.getEmail()).userIds(userCredentialEntity.getUserId()).collect();
                if (!this.conventionService.isListNullOrEmpty(userContacts)) 
                    this.deleterFactory.deleter(UserContactInfoDeleter.class).delete(userContacts);
            }
        }
        this.deleterFactory.deleter(UserCredentialDeleter.class).delete(List.of(userCredentialEntity));

        this.entityManager.flush();

        action.setUpdatedAt(Instant.now());
        action.setStatus(ActionConfirmationStatus.Accepted);
        this.entityManager.merge(action);
        this.entityManager.flush();

        this.keycloakService.removeFromAllGroups(userCredentialEntity.getExternalId());
        this.addToDefaultUserGroups(userCredentialEntity.getExternalId());

        this.userTouchedIntegrationEventHandler.handle(userCredentialEntity.getUserId());
        this.indicatorAccessEventHandler.handle(userCredentialEntity.getUserId());

        this.eventBroker.emit(new UserCredentialTouchedEvent(userCredentialEntity.getId(), userCredentialEntity.getExternalId()));
        this.eventBroker.emit(new UserTouchedEvent(userCredentialEntity.getUserId()));
    }

    private void addToDefaultUserGroups(String subjectId){
        this.keycloakService.addUserToGlobalRoleGroup(subjectId, this.authorizationConfiguration.getAuthorizationProperties().getGlobalUserRole());
        this.keycloakService.addUserToTenantRoleGroup(subjectId, this.tenantScope.getDefaultTenantCode(), this.authorizationConfiguration.getAuthorizationProperties().getTenantUserRole());
    }

    private void checkActionState(ActionConfirmationEntity action, boolean isUserInvite) throws MyApplicationException {
        if (action.getStatus().equals(ActionConfirmationStatus.Accepted)){
            if (isUserInvite)  throw new MyValidationException(this.errors.getInviteUserAlreadyConfirmed().getCode(), this.errors.getInviteUserAlreadyConfirmed().getMessage());
            else throw new MyValidationException(this.errors.getAccountRequestAlreadyConfirmed().getCode(), this.errors.getAccountRequestAlreadyConfirmed().getMessage());
        }
        if (action.getExpiresAt().compareTo(Instant.now()) < 0){
            throw new MyValidationException(this.errors.getRequestHasExpired().getCode(), this.errors.getRequestHasExpired().getMessage());
        }
    }

    public void sendUserToTenantInvitation(UserTenantUsersInviteRequest users) throws InvalidApplicationException, JAXBException {
        this.authorizationService.authorizeForce(Permission.InviteTenantUser);

        this.validatorFactory.validator(UserTenantUsersInviteRequest.UserTenantUsersInviteRequestValidator.class).validateForce(users);

        TenantEntity tenantEntity = null;
        String tenantName = null;
        String tenantCode;
        if (this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())) {
            tenantEntity = this.queryFactory.query(TenantQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).codes(this.tenantScope.getTenantCode()).isActive(IsActive.Active).first();
            if (tenantEntity == null) throw new MyApplicationException("Tenant not found");
            tenantName = tenantEntity.getName();
            tenantCode = tenantEntity.getCode();
        } else {
            tenantCode = this.tenantScope.getDefaultTenantCode();
        }
        for (UserInviteToTenantRequestPersist user: users.getUsers()) {
            String token = this.createUserInviteToTenantConfirmation(user, tenantCode);
            UserContactInfoEntity contactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().values(user.getEmail()).types(ContactInfoType.Email).first();
            if (contactInfoEntity != null && contactInfoEntity.getUserId() != null){
                if (tenantEntity != null){
                    if (this.queryFactory.query(TenantUserQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).tenantIds(tenantEntity.getId()).userIds(contactInfoEntity.getUserId()).isActive(IsActive.Active).count() > 0){
                        this.createTenantSpecificInvitationUserNotificationEvent(token, user.getEmail(), tenantName, contactInfoEntity.getUserId());
                    } else this.createTenantSpecificInvitationUserNotificationEvent(token, user.getEmail(), tenantName, null);
                } else if (tenantCode.equals(this.tenantScope.getDefaultTenantCode())){
                    this.createTenantSpecificInvitationUserNotificationEvent(token, user.getEmail(), tenantName, contactInfoEntity.getUserId());
                }
            } else {
                this.createTenantSpecificInvitationUserNotificationEvent(token, user.getEmail(), tenantName, null);
            }
        }

    }

    private String createUserInviteToTenantConfirmation(UserInviteToTenantRequestPersist model, String tenantCode) throws JAXBException, InvalidApplicationException {
        ActionConfirmationPersist persist = new ActionConfirmationPersist();
        persist.setType(ActionConfirmationType.UserInviteToTenant);
        persist.setStatus(ActionConfirmationStatus.Requested);
        persist.setToken(UUID.randomUUID().toString());
        persist.setUserInviteToTenantRequest(new UserInviteToTenantRequestPersist());
        persist.getUserInviteToTenantRequest().setEmail(model.getEmail());
        persist.getUserInviteToTenantRequest().setRoles(model.getRoles());
        persist.getUserInviteToTenantRequest().setTenantCode(tenantCode);
        persist.setExpiresAt(Instant.now().plusSeconds(this.usersProperties.getEmailExpirationTimeSeconds().getTenantSpecificInvitationExpiration()));
        this.validatorFactory.validator(ActionConfirmationPersist.ActionConfirmationPersistValidator.class).validateForce(persist);
        this.actionConfirmationService.persist(persist, null);

        try {
            this.entityManager.disableTenantFilters();
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        return persist.getToken();
    }

    private void createTenantSpecificInvitationUserNotificationEvent(String token, String email, String tenantName, UUID existingRecipient) throws InvalidApplicationException {
        UserEntity sender = this.entityManager.find(UserEntity.class,  this.userScope.getUserIdSafe());
        if (sender == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{ this.userScope.getUserIdSafe(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        NotifyIntegrationEvent event = new NotifyIntegrationEvent();

        if (existingRecipient == null) {
            List<ContactPair> contactPairs = new ArrayList<>();
            contactPairs.add(new ContactPair(ContactInfoType.Email, email));

            NotificationContactData contactData = new NotificationContactData(contactPairs, null, null);
            event.setContactHint(this.jsonHandlingService.toJsonSafe(contactData));
            event.setContactTypeHint(NotificationContactType.EMAIL);

            event.setNotificationType(this.notificationProperties.getTenantSpecificInvitationExternalUserType());
        } else {
            event.setUserId(existingRecipient);
            event.setNotificationType(this.notificationProperties.getTenantSpecificInvitationExistingUserType());
        }

        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{userName}", DataType.String, sender.getName()));
        fieldInfoList.add(new FieldInfo("{confirmationToken}", DataType.String, token));
        fieldInfoList.add(new FieldInfo("{expiration_time}", DataType.String, this.secondsToTime(this.usersProperties.getEmailExpirationTimeSeconds().getTenantSpecificInvitationExpiration())));
        if (!this.conventionService.isNullOrEmpty(tenantName)) fieldInfoList.add(new FieldInfo("{tenantName}", DataType.String, tenantName));

        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));
        this.eventHandler.handle(event);
    }

    public void confirmUserInviteToTenant(String token) throws InvalidApplicationException {
        ActionConfirmationEntity action;
        try {
            this.entityManager.disableTenantFilters();
            action = this.queryFactory.query(ActionConfirmationQuery.class).tokens(token).types(ActionConfirmationType.UserInviteToTenant).isActive(IsActive.Active).first();
        } finally {
            this.entityManager.reloadTenantFilters();
        }
        if (action == null)
            throw new MyValidationException(this.errors.getTokenNotExist().getCode(), this.errors.getTokenNotExist().getMessage());
        this.checkActionState(action, true);

        UserInviteToTenantRequestEntity userInviteToTenantRequest = this.xmlHandlingService.fromXmlSafe(UserInviteToTenantRequestEntity.class, action.getData());
        if (userInviteToTenantRequest == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{action.getId(), UserInviteToTenantRequestEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        UserContactInfoEntity contactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().userIds(this.userScope.getUserId()).values(userInviteToTenantRequest.getEmail()).types(ContactInfoType.Email).first();
        if (contactInfoEntity == null){
            throw new MyValidationException(this.errors.getAnotherUserToken().getCode(), this.errors.getAnotherUserToken().getMessage());
        }
        TenantEntity tenantEntity = null;
        if (!userInviteToTenantRequest.getTenantCode().equals(this.tenantScope.getTenantCode())) {
            tenantEntity = this.queryFactory.query(TenantQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).codes(userInviteToTenantRequest.getTenantCode()).isActive(IsActive.Active).first();
            if (tenantEntity == null) throw new MyApplicationException("Tenant not found");
        }
        this.addUserToTenant(tenantEntity, userInviteToTenantRequest);

        action.setStatus(ActionConfirmationStatus.Accepted);

        try {
            this.entityManager.disableTenantFilters();
            this.entityManager.merge(action);
            this.entityManager.flush();
        } finally {
            this.entityManager.reloadTenantFilters();
        }

    }

    private void addUserToTenant(TenantEntity tenant, UserInviteToTenantRequestEntity userInviteToTenantRequest) throws InvalidApplicationException {

        UUID userId = null;
        try {
            this.entityManager.disableTenantFilters();

            UserContactInfoEntity contactInfoEntity = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().values(userInviteToTenantRequest.getEmail()).types(ContactInfoType.Email).first();
            if (contactInfoEntity != null){
                userId = contactInfoEntity.getUserId();
            }
            if (userId != null) {
                if (!userId.equals(this.userScope.getUserId())) throw new MyApplicationException("Cannot confirm invitation for different User");
                UserCredentialEntity userCredential = this.queryFactory.query(UserCredentialQuery.class).disableTracking().userIds(userId).first();
                if (userCredential == null) throw new MyApplicationException();

                if (tenant != null){
                    boolean hasTenantUser = this.queryFactory.query(TenantUserQuery.class).tenantIds(tenant.getId()).isActive(IsActive.Active).userIds(userId).count() > 0;
                    if (!hasTenantUser) {
                        TenantUserEntity tenantUserEntity = new TenantUserEntity();
                        tenantUserEntity.setId(UUID.randomUUID());
                        tenantUserEntity.setUserId(userId);
                        tenantUserEntity.setIsActive(IsActive.Active);
                        tenantUserEntity.setTenantId(tenant.getId());
                        tenantUserEntity.setCreatedAt(Instant.now());
                        tenantUserEntity.setUpdatedAt(Instant.now());
                        this.entityManager.persist(tenantUserEntity);
                        this.eventBroker.emit(new UserAddedToTenantEvent(tenantUserEntity.getUserId(), tenantUserEntity.getTenantId()));
                        this.accountingService.increase(UsageLimitTargetMetric.USER_COUNT.getValue());
                    }
                }

                this.entityManager.disableTenantFilters();
                for (String role: userInviteToTenantRequest.getRoles()) {
                    UserRoleEntity item = new UserRoleEntity();
                    item.setId(UUID.randomUUID());
                    item.setUserId(userId);
                    if (tenant != null) item.setTenantId(tenant.getId());
                    else item.setTenantId(null);
                    item.setRole(role);
                    item.setCreatedAt(Instant.now());
                    this.entityManager.persist(item);
                }
                this.eventBroker.emit(new UserCredentialTouchedEvent(userCredential.getId(), userCredential.getExternalId()));

                this.entityManager.flush();

                this.eventBroker.emit(new UserTouchedEvent(userId));

                this.entityManager.flush();

                for (String role: userInviteToTenantRequest.getRoles()) {
                    if (tenant != null && !this.conventionService.isNullOrEmpty(tenant.getCode())) this.keycloakService.addUserToTenantRoleGroup(userCredential.getExternalId(), tenant.getCode(), role);
                    else this.keycloakService.addUserToTenantRoleGroup(userCredential.getExternalId(), tenantScope.getDefaultTenantCode(), role);
                }

                this.userTouchedIntegrationEventHandler.handle(userId);
                this.indicatorAccessEventHandler.handle(userId);
            }

        } finally {
            this.entityManager.reloadTenantFilters();
        }
    }

}