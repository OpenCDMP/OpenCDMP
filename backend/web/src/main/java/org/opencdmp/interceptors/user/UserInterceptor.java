package org.opencdmp.interceptors.user;


import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractor;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.commons.validator.routines.EmailValidator;
import org.opencdmp.authorization.AuthorizationConfiguration;
import org.opencdmp.authorization.ClaimNames;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.locale.LocaleProperties;
import org.opencdmp.commons.lock.LockByKeyManager;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.user.AdditionalInfoEntity;
import org.opencdmp.commons.types.usercredential.UserCredentialDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandlerImpl;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.query.UserContactInfoQuery;
import org.opencdmp.query.UserCredentialQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class UserInterceptor implements WebRequestInterceptor {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserInterceptor.class));
    private final UserScope userScope;
    private final ClaimExtractor claimExtractor;
    private final CurrentPrincipalResolver currentPrincipalResolver;
    private final PlatformTransactionManager transactionManager;
    private final UserInterceptorCacheService userInterceptorCacheService;
    private final JsonHandlingService jsonHandlingService;
    private final QueryFactory queryFactory;
    private final LockByKeyManager lockByKeyManager;
    private final LocaleProperties localeProperties;
    private final UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler;
    private final IndicatorAccessEventHandlerImpl indicatorAccessEventHandler;
    private final AuthorizationConfiguration authorizationConfiguration;
    private final ConventionService conventionService;
    @PersistenceContext
    public EntityManager entityManager;
    public final TenantEntityManager tenantEntityManager;
    

    @Autowired
    public UserInterceptor(
            UserScope userScope,
            ClaimExtractor claimExtractor,
            CurrentPrincipalResolver currentPrincipalResolver,
            PlatformTransactionManager transactionManager,
            UserInterceptorCacheService userInterceptorCacheService,
            JsonHandlingService jsonHandlingService,
            QueryFactory queryFactory,
            LockByKeyManager lockByKeyManager,
            LocaleProperties localeProperties, UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler, IndicatorAccessEventHandlerImpl indicatorAccessEventHandler, AuthorizationConfiguration authorizationConfiguration, ConventionService conventionService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.currentPrincipalResolver = currentPrincipalResolver;
        this.claimExtractor = claimExtractor;
        this.transactionManager = transactionManager;
        this.userInterceptorCacheService = userInterceptorCacheService;
        this.jsonHandlingService = jsonHandlingService;
        this.queryFactory = queryFactory;
        this.lockByKeyManager = lockByKeyManager;
        this.localeProperties = localeProperties;
	    this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.authorizationConfiguration = authorizationConfiguration;
	    this.conventionService = conventionService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    public void preHandle(WebRequest request) throws InterruptedException, InvalidApplicationException {
        UUID userId = null;
        if (this.currentPrincipalResolver.currentPrincipal().isAuthenticated()) {
            String subjectId = this.claimExtractor.subjectString(this.currentPrincipalResolver.currentPrincipal());
            if (subjectId == null || subjectId.isBlank()) throw new MyForbiddenException("Empty subjects not allowed");

            
            UserInterceptorCacheService.UserInterceptorCacheValue cacheValue = this.userInterceptorCacheService.lookup(this.userInterceptorCacheService.buildKey(subjectId));
            if (cacheValue != null && this.emailExistsToPrincipal(cacheValue.getProviderEmail()) && this.userRolesSynced(cacheValue.getRoles()) && this.providerExistsToPrincipal(cacheValue.getExternalProviderNames())) {
                userId = cacheValue.getUserId();
            } else {
                boolean usedResource = false;
                boolean shouldSendUserTouchedIntegrationEvent = false;
                try {
                    this.tenantEntityManager.disableTenantFilters();
                    
                    usedResource = this.lockByKeyManager.tryLock(subjectId, 5000, TimeUnit.MILLISECONDS);
                    String email = this.getEmailFromClaims();

                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setName(UUID.randomUUID().toString());
                    definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                    definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    TransactionStatus status = null;
                    try {
                        status = this.transactionManager.getTransaction(definition);

                        userId = this.findExistingUserFromDbBySubject(subjectId);
                        if (userId == null) {
                            userId = this.findExistingUserFromDbByEmailAndAddCredentail(subjectId, email);
                            shouldSendUserTouchedIntegrationEvent = userId != null;
                        }
                        boolean isNewUser = userId == null;
                        if (isNewUser) {
                            UserEntity user = this.addNewUser(subjectId, email);
                            userId = user.getId();
                            shouldSendUserTouchedIntegrationEvent = true;
                        }
                        this.entityManager.flush();

                        if (!isNewUser) {
                            boolean hasChanges = this.syncUserWithClaims(userId, subjectId);
                            if (hasChanges) shouldSendUserTouchedIntegrationEvent = true;
                        }
                        
                        this.entityManager.flush();

                        if (shouldSendUserTouchedIntegrationEvent){
                            this.userTouchedIntegrationEventHandler.handle(userId);
                            this.indicatorAccessEventHandler.handle(userId);
                        }

	                    this.transactionManager.commit(status);
                    } catch (Exception ex) {
                        if (status != null) this.transactionManager.rollback(status);
                        throw ex;
                    }

                    cacheValue = new UserInterceptorCacheService.UserInterceptorCacheValue(subjectId, userId);
                    cacheValue.setRoles(this.getRolesFromClaims());
                    if (email != null && !email.isBlank()) cacheValue.setProviderEmail(email);
                    UserCredentialEntity userCredential = this.queryFactory.query(UserCredentialQuery.class).externalIds(subjectId).firstAs(new BaseFieldSet().ensure(UserCredential._data));
                    if (userCredential != null && userCredential.getData() != null) {
                        UserCredentialDataEntity userCredentialDataEntity = this.jsonHandlingService.fromJsonSafe(UserCredentialDataEntity.class, userCredential.getData());
                        if (userCredentialDataEntity != null) cacheValue.setExternalProviderNames(userCredentialDataEntity.getExternalProviderNames());
                    }

                    this.userInterceptorCacheService.put(cacheValue);
                } finally {
                    if (usedResource) this.lockByKeyManager.unlock(subjectId);
                    this.tenantEntityManager.reloadTenantFilters();
                }

            }
        }
        this.userScope.setUserId(userId);
    }

    private boolean syncUserWithClaims(UUID userId, String subjectId) {
        List<String> existingUserEmails = this.collectUserEmails(userId);
        boolean hasChanges = false;
        if (!this.containsPrincipalEmail(existingUserEmails)) {
            String email = this.getEmailFromClaims();
            long contactUsedByOthersCount = this.queryFactory.query(UserContactInfoQuery.class).excludedUserIds(userId).types(ContactInfoType.Email).values(email).count();
            if (contactUsedByOthersCount > 0) {
                logger.warn("user contact exists to other user" + email);
            } else {
                Long emailContactsCount = this.queryFactory.query(UserContactInfoQuery.class).userIds(userId).types(ContactInfoType.Email).count();
                UserContactInfoEntity contactInfo = this.buildEmailContact(userId, email);
                contactInfo.setOrdinal(emailContactsCount.intValue());
                hasChanges = true;
                this.entityManager.persist(contactInfo);
            }
        }

        List<String> existingUserRoles = this.collectUserRoles(userId);
        if (!this.userRolesSynced(existingUserRoles)) {
            this.syncRoles(userId);
            hasChanges = true;
        }

        UserCredentialEntity userCredential = this.queryFactory.query(UserCredentialQuery.class).externalIds(subjectId).first();
        if (userCredential == null) {
            throw new MyForbiddenException("UserCredential not found");
        } else {
            boolean updatedUserCredential = false;
            UserCredentialDataEntity userCredentialDataEntity = this.jsonHandlingService.fromJsonSafe(UserCredentialDataEntity.class, userCredential.getData());
            if (userCredentialDataEntity == null) userCredentialDataEntity = new UserCredentialDataEntity();
            if (userCredentialDataEntity.getExternalProviderNames() == null) userCredentialDataEntity.setExternalProviderNames(new ArrayList<>());

            String email = this.getEmailFromClaims();
            String provider = this.getProviderFromClaims();

            if (email != null && !email.equalsIgnoreCase(userCredentialDataEntity.getEmail())) {
                userCredentialDataEntity.setEmail(email);
                updatedUserCredential = true;
            }
            if (provider != null && !provider.isBlank() && userCredentialDataEntity.getExternalProviderNames().stream().noneMatch(provider::equalsIgnoreCase)) {
                userCredentialDataEntity.getExternalProviderNames().add(provider);
                updatedUserCredential = true;
            }
            if (updatedUserCredential) {
                userCredential.setData(this.jsonHandlingService.toJsonSafe(userCredentialDataEntity));
                hasChanges = true;
                this.entityManager.persist(userCredential);
            }
        }
        return hasChanges;
    }

    private UUID findExistingUserFromDbBySubject(String subjectId) {
        UserCredentialEntity userCredential = this.queryFactory.query(UserCredentialQuery.class).externalIds(subjectId).firstAs(new BaseFieldSet().ensure(UserCredential._user));
        if (userCredential != null)  return userCredential.getUserId();
        return null;
    }

    private UUID findExistingUserFromDbByEmailAndAddCredentail(String subjectId, String email) {
        if (email != null && !email.isBlank()) {
            UserContactInfoEntity userContactInfo = this.queryFactory.query(UserContactInfoQuery.class).types(ContactInfoType.Email).values(email).firstAs(new BaseFieldSet().ensure(UserContactInfo._user));
            if (userContactInfo != null) {
                UserCredentialEntity credential = this.buildCredential(userContactInfo.getUserId(), subjectId);
                this.entityManager.persist(credential);

                return credential.getUserId();
            }
        } else {
            throw new MyForbiddenException("Email is required");
        }
        return null;
    }

    private List<String> getRolesFromClaims() {
        List<String> claimsRoles = this.claimExtractor.asStrings(this.currentPrincipalResolver.currentPrincipal(), ClaimNames.GlobalRolesClaimName);
        if (claimsRoles == null) claimsRoles = new ArrayList<>();
        claimsRoles = claimsRoles.stream().filter(x -> x != null && !x.isBlank() && (this.conventionService.isListNullOrEmpty(this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles()) || this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles().contains(x))).distinct().toList();
        claimsRoles = claimsRoles.stream().filter(x -> x != null && !x.isBlank()).distinct().toList();
        return claimsRoles;
    }

    private void syncRoles(UUID userId) {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<UserRoleEntity> query = criteriaBuilder.createQuery(UserRoleEntity.class);
        Root<UserRoleEntity> root = query.from(UserRoleEntity.class);

        CriteriaBuilder.In<String> inRolesClause = criteriaBuilder.in(root.get(UserRoleEntity._role));
        for (String item : this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles()) inRolesClause.value(item);
        query.where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get(UserRoleEntity._userId), userId),
                this.conventionService.isListNullOrEmpty(this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles()) ? criteriaBuilder.isNotNull(root.get(UserRoleEntity._role))  : inRolesClause,
                criteriaBuilder.isNull(root.get(UserRoleEntity._tenantId))
        ));
        List<UserRoleEntity> existingUserRoles = this.entityManager.createQuery(query).getResultList();
        
        List<UUID> foundRoles = new ArrayList<>();
        for (String claimRole : this.getRolesFromClaims()) {
            UserRoleEntity roleEntity = existingUserRoles.stream().filter(x -> x.getRole().equals(claimRole)).findFirst().orElse(null);
            if (roleEntity == null) {
                roleEntity = this.buildRole(userId, claimRole);
                this.entityManager.persist(roleEntity);
            }
            foundRoles.add(roleEntity.getId());
        }
        for (UserRoleEntity existing : existingUserRoles) {
            if (!foundRoles.contains(existing.getId())) {
                this.entityManager.remove(existing);
            }
        }
    }

    private List<String> collectUserRoles(UUID userId) {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<UserRoleEntity> query = criteriaBuilder.createQuery(UserRoleEntity.class);
        Root<UserRoleEntity> root = query.from(UserRoleEntity.class);

        CriteriaBuilder.In<String> inRolesClause = criteriaBuilder.in(root.get(UserRoleEntity._role));
        for (String item : this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles()) inRolesClause.value(item);
        
        query.where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get(UserRoleEntity._userId), userId),
                this.conventionService.isListNullOrEmpty(this.authorizationConfiguration.getAuthorizationProperties().getAllowedGlobalRoles()) ? criteriaBuilder.isNotNull(root.get(UserRoleEntity._role))  : inRolesClause,
                criteriaBuilder.isNull(root.get(UserRoleEntity._tenantId))
        )).multiselect(root.get(UserRoleEntity._role).alias(UserRoleEntity._role));
        List<UserRoleEntity> results = this.entityManager.createQuery(query).getResultList();
        
        return results.stream().map(UserRoleEntity::getRole).toList();
    }

    private List<String> collectUserEmails(UUID userId) {
        List<UserContactInfoEntity> items = this.queryFactory.query(UserContactInfoQuery.class).userIds(userId).types(ContactInfoType.Email).collectAs(new BaseFieldSet().ensure(UserContactInfo._value));
        return items == null ? new ArrayList<>() : items.stream().map(UserContactInfoEntity::getValue).toList();
    }

    private boolean containsPrincipalEmail(List<String> existingUserEmails) {
        String email = this.getEmailFromClaims();
        return email == null || email.isBlank() ||
                (existingUserEmails != null && existingUserEmails.stream().anyMatch(email::equals));
    }

    private boolean emailExistsToPrincipal(String existingUserEmail) {
        String email = this.getEmailFromClaims();
        return email == null || email.isBlank() || email.equalsIgnoreCase(existingUserEmail);
    }

    private boolean providerExistsToPrincipal(List<String> principalCredentialProviders) {
        String provider = this.getProviderFromClaims();
        return provider == null || provider.isBlank() ||
                (principalCredentialProviders != null && principalCredentialProviders.stream().anyMatch(provider::equalsIgnoreCase));
    }


    private boolean userRolesSynced(List<String> existingUserRoles) {
        List<String> claimsRoles = this.getRolesFromClaims();
        if (existingUserRoles == null) existingUserRoles = new ArrayList<>();
        existingUserRoles = existingUserRoles.stream().filter(x -> x != null && !x.isBlank()).distinct().toList();
        if (claimsRoles.size() != existingUserRoles.size()) return false;

        for (String claim : claimsRoles) {
            if (existingUserRoles.stream().noneMatch(claim::equalsIgnoreCase)) return false;
        }
        return true;
    }

    private String getEmailFromClaims() {
        String email = this.claimExtractor.email(this.currentPrincipalResolver.currentPrincipal());
        if (email == null || email.isBlank() || !EmailValidator.getInstance().isValid(email)) return null;
        return email.trim();
    }

    private String getProviderFromClaims() {
        String provider = this.claimExtractor.asString(this.currentPrincipalResolver.currentPrincipal(), ClaimNames.ExternalProviderName);
        if (provider == null || provider.isBlank()) return null;
        return provider.trim();
    }

    private UserCredentialEntity buildCredential(UUID userId, String subjectId) {
        UserCredentialEntity data = new UserCredentialEntity();
        UserCredentialDataEntity userCredentialDataEntity = new UserCredentialDataEntity();

        String email = this.getEmailFromClaims();
        String provider = this.getProviderFromClaims();
        if (email != null && !email.isBlank()) userCredentialDataEntity.setEmail(email);
        if (provider != null && !provider.isBlank()) userCredentialDataEntity.setExternalProviderNames(List.of(provider));
        data.setData(this.jsonHandlingService.toJsonSafe(userCredentialDataEntity));

        data.setId(UUID.randomUUID());
        data.setUserId(userId);
        data.setCreatedAt(Instant.now());
        data.setExternalId(subjectId);
        return data;
    }

    private UserRoleEntity buildRole(UUID userId, String role) {
        UserRoleEntity data = new UserRoleEntity();
        data.setId(UUID.randomUUID());
        data.setUserId(userId);
        data.setRole(role);
        data.setCreatedAt(Instant.now());
        return data;
    }

    private UserContactInfoEntity buildEmailContact(UUID userId, String email) {
        UserContactInfoEntity data = new UserContactInfoEntity();
        data.setId(UUID.randomUUID());
        data.setUserId(userId);
        data.setValue(email);
        data.setType(ContactInfoType.Email);
        data.setOrdinal(0);
        data.setCreatedAt(Instant.now());
        return data;
    }

    private UserEntity addNewUser(String subjectId, String email) {
        List<String> roles = this.getRolesFromClaims();
        String name = this.claimExtractor.name(this.currentPrincipalResolver.currentPrincipal());

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setIsActive(IsActive.Active);
        AdditionalInfoEntity additionalInfoEntity = new AdditionalInfoEntity();
        additionalInfoEntity.setCulture(this.localeProperties.getCulture());
        additionalInfoEntity.setLanguage(this.localeProperties.getLanguage());
        additionalInfoEntity.setTimezone(this.localeProperties.getTimezone());
        user.setAdditionalInfo(this.jsonHandlingService.toJsonSafe(additionalInfoEntity));
        this.entityManager.persist(user);

        UserCredentialEntity credential = this.buildCredential(user.getId(), subjectId);
        this.entityManager.persist(credential);

        if (email != null && !email.isBlank()) {
            UserContactInfoEntity contactInfo = this.buildEmailContact(user.getId(), email);
            this.entityManager.persist(contactInfo);
        }
        if (roles != null) {
            for (String role : roles) {
                UserRoleEntity roleEntity = this.buildRole(user.getId(), role);
                this.entityManager.persist(roleEntity);
            }
        }

        return user;
    }

    @Override
    public void postHandle(@NonNull WebRequest request, ModelMap model) {
        this.userScope.setUserId(null);
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
    }
}

