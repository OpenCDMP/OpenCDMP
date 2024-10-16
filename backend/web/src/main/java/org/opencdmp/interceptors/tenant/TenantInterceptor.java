package org.opencdmp.interceptors.tenant;


import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractor;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationConfiguration;
import org.opencdmp.authorization.ClaimNames;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.lock.LockByKeyManager;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.TenantUserEntity;
import org.opencdmp.data.UserEntity;
import org.opencdmp.data.UserRoleEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TenantInterceptor implements WebRequestInterceptor {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantInterceptor.class));
	private final TenantScope tenantScope;
	private final UserScope userScope;
	private final CurrentPrincipalResolver currentPrincipalResolver;
	private final ClaimExtractor claimExtractor;
	private final ApplicationContext applicationContext;
	private final TenantScopeProperties tenantScopeProperties;
	private final UserAllowedTenantCacheService userAllowedTenantCacheService;
	private final PlatformTransactionManager transactionManager;
	private final ErrorThesaurusProperties errors;
	private final QueryUtilsService queryUtilsService;
	private final LockByKeyManager lockByKeyManager;
	private final ConventionService conventionService;
	private final UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler;
	private final AuthorizationConfiguration authorizationConfiguration;
	private final UserTenantRolesCacheService userTenantRolesCacheService;
	public final TenantEntityManager tenantEntityManager;
	
	@PersistenceContext
	public EntityManager entityManager;

	@Autowired
	public TenantInterceptor(
			TenantScope tenantScope,
			UserScope userScope,
			CurrentPrincipalResolver currentPrincipalResolver,
			ClaimExtractor claimExtractor,
			ApplicationContext applicationContext,
			TenantScopeProperties tenantScopeProperties,
			UserAllowedTenantCacheService userAllowedTenantCacheService,
			PlatformTransactionManager transactionManager,
			ErrorThesaurusProperties errors, QueryUtilsService queryUtilsService, LockByKeyManager lockByKeyManager, ConventionService conventionService, UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler, AuthorizationConfiguration authorizationConfiguration, UserTenantRolesCacheService userTenantRolesCacheService, TenantEntityManager tenantEntityManager) {
		this.tenantScope = tenantScope;
		this.userScope = userScope;
		this.currentPrincipalResolver = currentPrincipalResolver;
		this.claimExtractor = claimExtractor;
		this.applicationContext = applicationContext;
		this.tenantScopeProperties = tenantScopeProperties;
		this.userAllowedTenantCacheService = userAllowedTenantCacheService;
		this.transactionManager = transactionManager;
		this.errors = errors;
		this.queryUtilsService = queryUtilsService;
		this.lockByKeyManager = lockByKeyManager;
		this.conventionService = conventionService;
		this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
		this.authorizationConfiguration = authorizationConfiguration;
		this.userTenantRolesCacheService = userTenantRolesCacheService;
		this.tenantEntityManager = tenantEntityManager;
	}

	@Override
	public void preHandle(@NotNull WebRequest request) throws InvalidApplicationException, InterruptedException {
		if (!this.currentPrincipalResolver.currentPrincipal().isAuthenticated()) return;
		if (!this.tenantScope.isMultitenant()) return;

		boolean isAllowedNoTenant = this.applicationContext.getBean(AuthorizationService.class).authorize(Permission.AllowNoTenant);
		if (this.tenantScope.isSet() && this.entityManager != null) {
			List<String> currentPrincipalTenantCodes = this.claimExtractor.asStrings(this.currentPrincipalResolver.currentPrincipal(), ClaimNames.TenantCodesClaimName);
			if ((currentPrincipalTenantCodes == null || !currentPrincipalTenantCodes.contains(this.tenantScope.getTenantCode())) && !isAllowedNoTenant) {
				logger.warn("tenant not allowed {}", this.tenantScope.getTenant());
				throw new MyForbiddenException(this.errors.getTenantNotAllowed().getCode(), this.errors.getTenantNotAllowed().getMessage());
			}

			boolean isUserAllowedTenant = false;
			if (this.tenantScope.supportExpansionTenant() && this.tenantScope.isDefaultTenant()){
				isUserAllowedTenant = true;
			} else {
				UserAllowedTenantCacheService.UserAllowedTenantCacheValue cacheValue = this.userAllowedTenantCacheService.lookup(this.userAllowedTenantCacheService.buildKey(this.userScope.getUserId(), this.tenantScope.getTenant()));
				if (cacheValue != null) {
					isUserAllowedTenant = cacheValue.isAllowed();
				} else {
					List<String> tenants = this.claimExtractor.asStrings(this.currentPrincipalResolver.currentPrincipal(), ClaimNames.TenantCodesClaimName);
					if (tenants.contains(this.tenantScope.getTenantCode())) isUserAllowedTenant = this.isUserAllowedTenant();
					this.userAllowedTenantCacheService.put(new UserAllowedTenantCacheService.UserAllowedTenantCacheValue(this.userScope.getUserId(), this.tenantScope.getTenant(), isUserAllowedTenant));
				}
			}

			if (isUserAllowedTenant) {
				this.tenantEntityManager.reloadTenantFilters();
				UserTenantRolesCacheService.UserTenantRolesCacheValue cacheValue = this.userTenantRolesCacheService.lookup(this.userTenantRolesCacheService.buildKey(this.userScope.getUserId(), this.tenantScope.isDefaultTenant() ? UUID.fromString("00000000-0000-0000-0000-000000000000") : this.tenantScope.getTenant()));
				if (cacheValue == null || !this.userRolesSynced(cacheValue.getRoles())) {
					this.syncUserWithClaims();
				}
			} else {
				if (isAllowedNoTenant || this.isWhiteListedEndpoint(request)) {
					this.tenantScope.setTenant(null, null);
				} else {
					logger.warn("tenant not allowed {}", this.tenantScope.getTenant());
					throw new MyForbiddenException(this.errors.getTenantNotAllowed().getCode(), this.errors.getTenantNotAllowed().getMessage());
				}
			}
		} else {
			if (!isAllowedNoTenant) {
				if (!this.isWhiteListedEndpoint(request)) {
					logger.warn("tenant scope not provided");
					throw new MyForbiddenException(this.errors.getMissingTenant().getCode(), this.errors.getMissingTenant().getMessage());
				}
			}
		}
	}

	private boolean isWhiteListedEndpoint(WebRequest request) {
		String servletPath = ((ServletWebRequest) request).getRequest().getServletPath();
		if (this.tenantScopeProperties.getWhiteListedEndpoints() != null) {
			for (String whiteListedEndpoint : this.tenantScopeProperties.getWhiteListedEndpoints()) {
				if (servletPath.toLowerCase(Locale.ROOT).startsWith(whiteListedEndpoint.toLowerCase(Locale.ROOT))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isUserAllowedTenant() throws InvalidApplicationException, InterruptedException {
		if (this.userScope.isSet()) {
			boolean usedResource = false;
			String lockId = this.userScope.getUserId().toString().toLowerCase(Locale.ROOT);
			try {
				if (this.tenantScopeProperties.getAutoCreateTenantUser()) usedResource = this.lockByKeyManager.tryLock(lockId, 5000, TimeUnit.MILLISECONDS);
				
				CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
				CriteriaQuery<UserEntity> query = criteriaBuilder.createQuery(UserEntity.class);
				Root<UserEntity> root = query.from(UserEntity.class);
				query.where(criteriaBuilder.and(
						criteriaBuilder.equal(root.get(UserEntity._isActive), IsActive.Active),
						criteriaBuilder.in(root.get(UserEntity._id)).value(this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(TenantUserEntity.class, UUID.class)
										.query(query)
										.criteriaBuilder(criteriaBuilder)
										.keyPathFunc((subQueryRoot) -> subQueryRoot.get(TenantUserEntity._userId))
										.filterFunc((subQueryRoot, cb) ->
												{
													try {
														return cb.and(
																criteriaBuilder.equal(subQueryRoot.get(TenantUserEntity._tenantId), this.tenantScope.getTenant()),
																criteriaBuilder.equal(subQueryRoot.get(TenantUserEntity._userId), this.userScope.getUserId()),
																criteriaBuilder.equal(subQueryRoot.get(TenantUserEntity._isActive), IsActive.Active)
														);
													} catch (InvalidApplicationException e) {
														throw new RuntimeException(e);
													}
												}
										)
								))
						)
				));
				query.multiselect(root.get(UserEntity._id).alias(UserEntity._id));
				List<UserEntity> results = this.entityManager.createQuery(query).getResultList();
				if (results.isEmpty() && this.tenantScopeProperties.getAutoCreateTenantUser()) {
					return this.createTenantUser();
				} else {
					return !results.isEmpty();
				}
			} finally {
				if (usedResource) this.lockByKeyManager.unlock(lockId);
			}
		}

		return false;
	}

	private boolean createTenantUser() throws InvalidApplicationException {
		TenantUserEntity user = new TenantUserEntity();
		user.setId(UUID.randomUUID());
		user.setCreatedAt(Instant.now());
		user.setUpdatedAt(Instant.now());
		user.setIsActive(IsActive.Active);
		user.setTenantId(this.tenantScope.getTenant());
		user.setUserId(this.userScope.getUserId());
		

		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setName(UUID.randomUUID().toString());
		definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = null;
		try {
			status = this.transactionManager.getTransaction(definition);
			this.entityManager.persist(user);
			this.entityManager.flush();
			this.transactionManager.commit(status);
		} catch (Exception ex) {
			if (status != null) this.transactionManager.rollback(status);
			throw ex;
		}
		this.userTouchedIntegrationEventHandler.handle(this.userScope.getUserId());
		return true;
	}

	private void syncUserWithClaims() throws InvalidApplicationException, InterruptedException {
		
		
		boolean usedResource = false;
		String lockId = this.userScope.getUserId().toString().toLowerCase(Locale.ROOT);
		boolean hasChanges = false;
		try {
			usedResource = this.lockByKeyManager.tryLock(lockId, 5000, TimeUnit.MILLISECONDS);

			DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
			definition.setName(UUID.randomUUID().toString());
			definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
			definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = null;
			try {
				status = this.transactionManager.getTransaction(definition);

				UserTenantRolesCacheService.UserTenantRolesCacheValue cacheValue = this.userTenantRolesCacheService.lookup(this.userTenantRolesCacheService.buildKey(this.userScope.getUserId(), this.tenantScope.isDefaultTenant() ? UUID.fromString("00000000-0000-0000-0000-000000000000") : this.tenantScope.getTenant()));
				List<String> existingUserRoles;
				if (cacheValue != null) {
					existingUserRoles = cacheValue.getRoles();
				} else {
					existingUserRoles = this.collectUserRoles();
					this.userTenantRolesCacheService.put(new UserTenantRolesCacheService.UserTenantRolesCacheValue(this.userScope.getUserId(), this.tenantScope.isDefaultTenant() ? UUID.fromString("00000000-0000-0000-0000-000000000000") : this.tenantScope.getTenant(), existingUserRoles));
				}
				if (!this.userRolesSynced(existingUserRoles)) {
					this.syncRoles();
					hasChanges = true;
				}

				this.entityManager.flush();
				this.transactionManager.commit(status);
			} catch (Exception ex) {
				if (status != null) this.transactionManager.rollback(status);
				throw ex;
			}
		} finally {
			if (usedResource) this.lockByKeyManager.unlock(lockId);
		}
		if (hasChanges){
			this.userTouchedIntegrationEventHandler.handle(this.userScope.getUserId());
		}
	}

	private List<String> getRolesFromClaims() {
		List<String> claimsRoles = this.claimExtractor.asStrings(this.currentPrincipalResolver.currentPrincipal(), ClaimNames.TenantRolesClaimName);
		if (claimsRoles == null) claimsRoles = new ArrayList<>();
		claimsRoles = claimsRoles.stream().filter(x -> x != null && !x.isBlank() && (this.conventionService.isListNullOrEmpty(this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()) || this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles().contains(x))).distinct().toList();
		return claimsRoles;
	}

	private List<String> collectUserRoles() throws InvalidApplicationException {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<UserRoleEntity> query = criteriaBuilder.createQuery(UserRoleEntity.class);
		Root<UserRoleEntity> root = query.from(UserRoleEntity.class);
		
		CriteriaBuilder.In<String> inRolesClause = criteriaBuilder.in(root.get(UserRoleEntity._role));
		for (String item : this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()) inRolesClause.value(item);
		
		query.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(UserRoleEntity._userId), this.userScope.getUserId()),
				this.conventionService.isListNullOrEmpty(this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()) ? criteriaBuilder.isNotNull(root.get(UserRoleEntity._role))  : inRolesClause,
				this.tenantScope.isDefaultTenant() ? criteriaBuilder.isNull(root.get(UserRoleEntity._tenantId)) : criteriaBuilder.equal(root.get(UserRoleEntity._tenantId), this.tenantScope.getTenant())
		)).multiselect(root.get(UserRoleEntity._role).alias(UserRoleEntity._role));
		List<UserRoleEntity> results = this.entityManager.createQuery(query).getResultList();

		return results.stream().map(UserRoleEntity::getRole).toList();
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

	private void syncRoles() throws InvalidApplicationException {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<UserRoleEntity> query = criteriaBuilder.createQuery(UserRoleEntity.class);
		Root<UserRoleEntity> root = query.from(UserRoleEntity.class);

		CriteriaBuilder.In<String> inRolesClause = criteriaBuilder.in(root.get(UserRoleEntity._role));
		for (String item : this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()) inRolesClause.value(item);
		query.where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(UserRoleEntity._userId), this.userScope.getUserId()),
				this.conventionService.isListNullOrEmpty(this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()) ? criteriaBuilder.isNotNull(root.get(UserRoleEntity._role))  : inRolesClause,
				this.tenantScope.isDefaultTenant() ? criteriaBuilder.isNull(root.get(UserRoleEntity._tenantId)) : criteriaBuilder.equal(root.get(UserRoleEntity._tenantId), this.tenantScope.getTenant())
		));
		List<UserRoleEntity> existingUserRoles = this.entityManager.createQuery(query).getResultList();

		List<UUID> foundRoles = new ArrayList<>();
		for (String claimRole : this.getRolesFromClaims()) {
			UserRoleEntity roleEntity = existingUserRoles.stream().filter(x -> x.getRole().equals(claimRole)).findFirst().orElse(null);
			if (roleEntity == null) {
				roleEntity = this.buildRole(claimRole);
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

	private UserRoleEntity buildRole(String role) throws InvalidApplicationException {
		UserRoleEntity data = new UserRoleEntity();
		data.setId(UUID.randomUUID());
		data.setUserId(this.userScope.getUserId());
		data.setRole(role);
		if (this.tenantScope.isDefaultTenant()) data.setTenantId(this.tenantScope.getTenant());
		data.setCreatedAt(Instant.now());
		return data;
	}

	@Override
	public void postHandle(@NonNull WebRequest request, ModelMap model) {
		this.tenantScope.setTenant(null, null);
		this.tenantEntityManager.disableTenantFilters();
	}

	@Override
	public void afterCompletion(@NonNull WebRequest request, Exception ex) {
	}
}
