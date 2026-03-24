package org.opencdmp.service.tenant;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorFactory;
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
import org.opencdmp.authorization.AuthorizationConfiguration;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.ClaimNames;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.*;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandlerImpl;
import org.opencdmp.integrationevent.outbox.tenantremoval.TenantRemovalIntegrationEvent;
import org.opencdmp.integrationevent.outbox.tenantremoval.TenantRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.tenanttouched.TenantTouchedIntegrationEvent;
import org.opencdmp.integrationevent.outbox.tenanttouched.TenantTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.builder.TenantBuilder;
import org.opencdmp.model.deleter.TenantDeleter;
import org.opencdmp.model.persist.TenantPersist;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.UserCredentialQuery;
import org.opencdmp.query.UserRoleQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.keycloak.KeycloakService;
import org.opencdmp.service.kpi.KpiService;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TenantServiceImpl implements TenantService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantServiceImpl.class));

    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final TenantTouchedIntegrationEventHandler tenantTouchedIntegrationEventHandler;
    private final TenantRemovalIntegrationEventHandler tenantRemovalIntegrationEventHandler;
    private final UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler;
    private final IndicatorAccessEventHandlerImpl indicatorAccessEventHandler;
    private final KeycloakService keycloakService;
    private final AuthorizationConfiguration authorizationConfiguration;
    private final TenantScopeFactory tenantScopeFactory;
    private final QueryFactory queryFactory;
    private final CurrentPrincipalResolverFactory currentPrincipalResolverFactory;
    private final ClaimExtractorFactory claimExtractorFactory;
    private final EventBroker eventBroker;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final KpiService kpiService;

    
    @Autowired
    public TenantServiceImpl(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            ErrorThesaurusProperties errors, TenantTouchedIntegrationEventHandler tenantTouchedIntegrationEventHandler, TenantRemovalIntegrationEventHandler tenantRemovalIntegrationEventHandler, UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler, IndicatorAccessEventHandlerImpl indicatorAccessEventHandler, KeycloakService keycloakService, AuthorizationConfiguration authorizationConfiguration, TenantScopeFactory tenantScopeFactory, QueryFactory queryFactory, CurrentPrincipalResolverFactory currentPrincipalResolverFactory, ClaimExtractorFactory claimExtractorFactory, EventBroker eventBroker, UsageLimitService usageLimitService, AccountingService accountingService, KpiService kpiService) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.errors = errors;
	    this.tenantTouchedIntegrationEventHandler = tenantTouchedIntegrationEventHandler;
	    this.tenantRemovalIntegrationEventHandler = tenantRemovalIntegrationEventHandler;
	    this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.keycloakService = keycloakService;
	    this.authorizationConfiguration = authorizationConfiguration;
	    this.tenantScopeFactory = tenantScopeFactory;
	    this.queryFactory = queryFactory;
	    this.currentPrincipalResolverFactory = currentPrincipalResolverFactory;
	    this.claimExtractorFactory = claimExtractorFactory;
	    this.eventBroker = eventBroker;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.kpiService = kpiService;
    }

    @Override
    public Tenant persist(TenantPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditTenant);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        TenantEntity data;
        if (isUpdate) {
            data = this.tenantEntityManagerFactory.getInstance().find(TenantEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Tenant.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash()))
                throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
            if (!data.getCode().equals(model.getCode()))
                throw new MyValidationException(this.errors.getModelValidation().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            data = new TenantEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }

        data.setCode(model.getCode());
        data.setName(model.getName());
        data.setDescription(model.getDescription());
        data.setUpdatedAt(Instant.now());

        if (isUpdate) this.tenantEntityManagerFactory.getInstance().merge(data);
        else this.tenantEntityManagerFactory.getInstance().persist(data);

        this.tenantEntityManagerFactory.getInstance().flush();

        if (!isUpdate) {
            Long tenantsWithThisCode = this.queryFactory.query(TenantQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .codes(data.getCode())
                    .count();

            if (tenantsWithThisCode > 0) throw new MyValidationException(this.errors.getTenantCodeExists().getCode(), this.errors.getTenantCodeExists().getMessage());
        }

        TenantTouchedIntegrationEvent tenantTouchedIntegrationEvent = new TenantTouchedIntegrationEvent();
        tenantTouchedIntegrationEvent.setId(data.getId());
        tenantTouchedIntegrationEvent.setCode(data.getCode());
        
        if (!isUpdate) {
            this.keycloakService.createTenantGroups(data.getCode());
            this.tenantTouchedIntegrationEventHandler.handle(tenantTouchedIntegrationEvent);
            this.kpiService.sendIndicatorPointTenantEntry(KpiDirectionType.Increase);
            this.autoAssignGlobalAdminsToNewTenant(data);
        } else this.tenantTouchedIntegrationEventHandler.handle(tenantTouchedIntegrationEvent);

        this.eventBroker.emit(new TenantTouchedEvent(data.getId(), data.getCode()));

        return this.builderFactory.builder(TenantBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Tenant._id), data);
    }
    
    private void autoAssignGlobalAdminsToNewTenant(TenantEntity tenant) throws InvalidApplicationException {
        if (!this.authorizationConfiguration.getAuthorizationProperties().getAutoAssignGlobalAdminToNewTenants()) return;
        List<UserRoleEntity> existingItems;
        List<UserCredentialEntity> userCredentialEntities;
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();

            existingItems = this.queryFactory.query(UserRoleQuery.class).disableTracking().tenantIsSet(false).roles(this.authorizationConfiguration.getAuthorizationProperties().getGlobalAdminRoles()).collect();
            userCredentialEntities = this.queryFactory.query(UserCredentialQuery.class).disableTracking().userIds(existingItems.stream().map(UserRoleEntity::getUserId).distinct().toList()).collect();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }

        List<String> keycloakIdsToAddToTenantGroup = new ArrayList<>();
        try {
            this.tenantScopeFactory.getInstance().setTempTenant(this.tenantEntityManagerFactory.getInstance(), tenant.getId(), tenant.getCode());
            for (UUID userId : existingItems.stream().map(UserRoleEntity::getUserId).distinct().toList()) {
                this.usageLimitService.checkIncrease(UsageLimitTargetMetric.USER_COUNT);
                TenantUserEntity tenantUserEntity = new TenantUserEntity();
                tenantUserEntity.setId(UUID.randomUUID());
                tenantUserEntity.setUserId(userId);
                tenantUserEntity.setIsActive(IsActive.Active);
                tenantUserEntity.setTenantId(tenant.getId());
                tenantUserEntity.setCreatedAt(Instant.now());
                tenantUserEntity.setUpdatedAt(Instant.now());
                this.tenantEntityManagerFactory.getInstance().persist(tenantUserEntity);
                this.eventBroker.emit(new UserAddedToTenantEvent(tenantUserEntity.getUserId(), tenantUserEntity.getTenantId()));
                this.accountingService.increase(UsageLimitTargetMetric.USER_COUNT.getValue());
                this.kpiService.sendIndicatorPointUserEntry(KpiDirectionType.Increase);

                UserCredentialEntity userCredential = userCredentialEntities.stream().filter(x -> !this.conventionService.isNullOrEmpty(x.getExternalId()) && x.getUserId().equals(userId)).findFirst().orElse(null);
                if (userCredential == null) continue;
                UserRoleEntity item = new UserRoleEntity();
                item.setId(UUID.randomUUID());
                item.setUserId(userId);
                item.setTenantId(tenant.getId());
                if (existingItems.stream().filter(x -> x.getUserId().equals(userId) && x.getRole().equals(this.authorizationConfiguration.getAuthorizationProperties().getAdminRole())).findFirst().orElse(null) != null) {
                    item.setRole(this.authorizationConfiguration.getAuthorizationProperties().getTenantAdminRole()); // admin
                } else {
                    item.setRole(this.authorizationConfiguration.getAuthorizationProperties().getTenantUserRole()); // installation admin
                }
                item.setCreatedAt(Instant.now());
                this.tenantEntityManagerFactory.getInstance().persist(item);
                keycloakIdsToAddToTenantGroup.add(userCredential.getExternalId());

                this.eventBroker.emit(new UserCredentialTouchedEvent(userCredential.getId(), userCredential.getExternalId()));
            }

            this.tenantEntityManagerFactory.getInstance().flush();
        } finally {
            this.tenantScopeFactory.getInstance().removeTempTenant(this.tenantEntityManagerFactory.getInstance());
        }

        boolean atLeastOneKeycloakIdAdded = false;
        for (String externalId : keycloakIdsToAddToTenantGroup) {
            try {
                this.keycloakService.addUserToTenantRoleGroup(externalId, tenant.getCode(), this.authorizationConfiguration.getAuthorizationProperties().getTenantAdminRole());
                atLeastOneKeycloakIdAdded = true;
            } catch (Exception e) {
                logger.error("Cannot add user to tenant group for tenant code " + tenant.getCode() + "and credential " + externalId + ". Skipping:" + e.getMessage());
            }
        }

        if (!atLeastOneKeycloakIdAdded) throw new MyApplicationException("Cannot create a new tenant without a valid tenant admin");

        for (UUID userId : existingItems.stream().map(UserRoleEntity::getUserId).distinct().toList()) {
            this.userTouchedIntegrationEventHandler.handle(userId);
            this.indicatorAccessEventHandler.handle(userId);
            this.eventBroker.emit(new UserTouchedEvent(userId));
        }
       
    }

    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);
        TenantEntity data = this.tenantEntityManagerFactory.getInstance().find(TenantEntity.class, id);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Tenant.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.authorizationService.authorizeForce(Permission.DeleteTenant);

        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            this.deleterFactory.deleter(TenantDeleter.class).deleteAndSaveByIds(List.of(id));
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }

        TenantRemovalIntegrationEvent tenantRemovalIntegrationEvent = new TenantRemovalIntegrationEvent();
        tenantRemovalIntegrationEvent.setId(id);
        this.tenantRemovalIntegrationEventHandler.handle(tenantRemovalIntegrationEvent);

        this.eventBroker.emit(new TenantTouchedEvent(data.getId(), data.getCode()));
    }

    

    @Override
    public List<Tenant> myTenants(FieldSet fieldSet) throws MyForbiddenException {
        logger.debug("my tenants");

        MyPrincipal principal = this.currentPrincipalResolverFactory.getInstance().currentPrincipal();
        List<String> tenants = this.claimExtractorFactory.getInstance().asStrings(principal, ClaimNames.TenantCodesClaimName);
        List<Tenant> models = new ArrayList<>();

        if (tenants != null && !tenants.isEmpty()) {
            if (fieldSet == null || fieldSet.isEmpty()) {
                fieldSet = new BaseFieldSet(
                        Tenant._id,
                        Tenant._code,
                        Tenant._name);
            }

            TenantQuery query = this.queryFactory.query(TenantQuery.class).codes(tenants).isActive(IsActive.Active);
            query.setOrder(new Ordering().addAscending(Tenant._name));
            List<TenantEntity> data = query.collectAs(fieldSet);
            models.addAll(this.builderFactory.builder(TenantBuilder.class).build(fieldSet, data));

            if (tenants.contains(this.tenantScopeFactory.getInstance().getDefaultTenantCode())){
                Tenant tenant = new Tenant();
                tenant.setCode(this.tenantScopeFactory.getInstance().getDefaultTenantCode());
                tenant.setName(this.messageSource.getMessage("DefaultTenant_Name", new Object[]{}, LocaleContextHolder.getLocale()));
                models.addFirst(tenant);
            }
        }
        return models;
    }

}

