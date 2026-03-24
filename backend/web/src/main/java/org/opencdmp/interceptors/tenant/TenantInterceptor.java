package org.opencdmp.interceptors.tenant;


import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorFactory;
import gr.cite.tools.exception.MyApplicationException;
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
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.lock.LockByKeyManager;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.outbox.indicatoraccess.IndicatorAccessEventHandlerImpl;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.kpi.KpiService;
import org.opencdmp.service.usagelimit.UsageLimitService;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TenantInterceptor implements WebRequestInterceptor {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantInterceptor.class));
	private final TenantScopeFactory tenantScopeFactory;
	private final UserScopeFactory userScopeFactory;
	private final CurrentPrincipalResolverFactory currentPrincipalResolverFactory;
	private final ClaimExtractorFactory claimExtractorFactory;
	private final ApplicationContext applicationContext;
	private final TenantScopeProperties tenantScopeProperties;
	private final UserAllowedTenantCacheService userAllowedTenantCacheService;
	private final PlatformTransactionManager transactionManager;
	private final ErrorThesaurusProperties errors;
	private final QueryUtilsService queryUtilsService;
	private final LockByKeyManager lockByKeyManager;
	private final ConventionService conventionService;
	private final UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler;
	private final IndicatorAccessEventHandlerImpl indicatorAccessEventHandler;
	private final AuthorizationConfiguration authorizationConfiguration;
	private final UserTenantRolesCacheService userTenantRolesCacheService;
	public final TenantEntityManagerFactory tenantEntityManagerFactory;
	private final UsageLimitService usageLimitService;
	private final AccountingService accountingService;
	private final KpiService kpiService;
	
	@PersistenceContext
	public EntityManager entityManager;

	@Autowired
	public TenantInterceptor(
            TenantScopeFactory tenantScopeFactory,
            UserScopeFactory userScopeFactory,
            CurrentPrincipalResolverFactory currentPrincipalResolverFactory,
            ClaimExtractorFactory claimExtractorFactory,
            ApplicationContext applicationContext,
            TenantScopeProperties tenantScopeProperties,
            UserAllowedTenantCacheService userAllowedTenantCacheService,
            PlatformTransactionManager transactionManager,
            ErrorThesaurusProperties errors, QueryUtilsService queryUtilsService, LockByKeyManager lockByKeyManager, ConventionService conventionService, UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler, IndicatorAccessEventHandlerImpl indicatorAccessEventHandler, AuthorizationConfiguration authorizationConfiguration, UserTenantRolesCacheService userTenantRolesCacheService, TenantEntityManagerFactory tenantEntityManagerFactory, UsageLimitService usageLimitService, AccountingService accountingService, KpiService kpiService) {
		this.tenantScopeFactory = tenantScopeFactory;
		this.userScopeFactory = userScopeFactory;
		this.currentPrincipalResolverFactory = currentPrincipalResolverFactory;
		this.claimExtractorFactory = claimExtractorFactory;
		this.applicationContext = applicationContext;
		this.tenantScopeProperties = tenantScopeProperties;
		this.userAllowedTenantCacheService = userAllowedTenantCacheService;
		this.transactionManager = transactionManager;
		this.errors = errors;
		this.queryUtilsService = queryUtilsService;
		this.lockByKeyManager = lockByKeyManager;
		this.conventionService = conventionService;
		this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
        this.indicatorAccessEventHandler = indicatorAccessEventHandler;
        this.authorizationConfiguration = authorizationConfiguration;
		this.userTenantRolesCacheService = userTenantRolesCacheService;
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.kpiService = kpiService;
    }

	@Override
	public void preHandle(@NotNull WebRequest request) throws InvalidApplicationException, InterruptedException {
		if (!this.currentPrincipalResolverFactory.getInstance().currentPrincipal().isAuthenticated()) return;
		if (!this.tenantScopeFactory.getInstance().isMultitenant()) return;

		boolean isAllowedNoTenant = this.applicationContext.getBean(AuthorizationService.class).authorize(Permission.AllowNoTenant);
		if (this.tenantScopeFactory.getInstance().isSet() && this.entityManager != null) {
			List<String> currentPrincipalTenantCodes = this.claimExtractorFactory.getInstance().asStrings(this.currentPrincipalResolverFactory.getInstance().currentPrincipal(), ClaimNames.TenantCodesClaimName);
			if ((currentPrincipalTenantCodes == null || !currentPrincipalTenantCodes.contains(this.tenantScopeFactory.getInstance().getTenantCode())) && !isAllowedNoTenant) {
				logger.warn("tenant not allowed {}", this.tenantScopeFactory.getInstance().getTenant());
				throw new MyForbiddenException(this.errors.getTenantNotAllowed().getCode(), this.errors.getTenantNotAllowed().getMessage());
			}

			boolean isUserAllowedTenant = false;
			if (this.tenantScopeFactory.getInstance().supportExpansionTenant() && this.tenantScopeFactory.getInstance().isDefaultTenant()){
				isUserAllowedTenant = true;
			} else {
				UserAllowedTenantCacheService.UserAllowedTenantCacheValue cacheValue = this.userAllowedTenantCacheService.lookup(this.userAllowedTenantCacheService.buildKey(this.userScopeFactory.getInstance().getUserId(), this.tenantScopeFactory.getInstance().getTenant()));
				if (cacheValue != null) {
					isUserAllowedTenant = cacheValue.isAllowed();
				} else {
					List<String> tenants = this.claimExtractorFactory.getInstance().asStrings(this.currentPrincipalResolverFactory.getInstance().currentPrincipal(), ClaimNames.TenantCodesClaimName);
					if (tenants.contains(this.tenantScopeFactory.getInstance().getTenantCode())) isUserAllowedTenant = this.isUserAllowedTenant();
					this.userAllowedTenantCacheService.put(new UserAllowedTenantCacheService.UserAllowedTenantCacheValue(this.userScopeFactory.getInstance().getUserId(), this.tenantScopeFactory.getInstance().getTenant(), isUserAllowedTenant));
				}
			}

			if (isUserAllowedTenant) {
				this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
				if (!this.userRolesSynced()) {
					this.syncUserWithClaims();
				}
			} else {
				if (isAllowedNoTenant || this.isWhiteListedEndpoint(request)) {
					this.tenantScopeFactory.getInstance().setTenant(null, null);
				} else {
					logger.warn("tenant not allowed {}", this.tenantScopeFactory.getInstance().getTenant());
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
		if (this.userScopeFactory.getInstance().isSet()) {
			boolean usedResource = false;
			String lockId = this.userScopeFactory.getInstance().getUserId().toString().toLowerCase(Locale.ROOT);
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
																criteriaBuilder.equal(subQueryRoot.get(TenantUserEntity._tenantId), this.tenantScopeFactory.getInstance().getTenant()),
																criteriaBuilder.equal(subQueryRoot.get(TenantUserEntity._userId), this.userScopeFactory.getInstance().getUserId()),
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
		this.usageLimitService.checkIncrease(UsageLimitTargetMetric.USER_COUNT);

		TenantUserEntity user = new TenantUserEntity();
		user.setId(UUID.randomUUID());
		user.setCreatedAt(Instant.now());
		user.setUpdatedAt(Instant.now());
		user.setIsActive(IsActive.Active);
		user.setTenantId(this.tenantScopeFactory.getInstance().getTenant());
		user.setUserId(this.userScopeFactory.getInstance().getUserId());
		

		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setName(UUID.randomUUID().toString());
		definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = null;
		try {
			status = this.transactionManager.getTransaction(definition);
			this.entityManager.persist(user);
			this.entityManager.flush();
			this.accountingService.increase(UsageLimitTargetMetric.USER_COUNT.getValue());
			this.kpiService.sendIndicatorPointUserEntry(KpiDirectionType.Increase);
			this.userTouchedIntegrationEventHandler.handle(this.userScopeFactory.getInstance().getUserId());
			this.indicatorAccessEventHandler.handle(this.userScopeFactory.getInstance().getUserId());
			this.transactionManager.commit(status);
		} catch (Exception ex) {
			if (status != null) this.transactionManager.rollback(status);
			throw ex;
		}
		return true;
	}

	private void syncUserWithClaims() throws InvalidApplicationException, InterruptedException {
		
		
		boolean usedResource = false;
		String lockId = this.userScopeFactory.getInstance().getUserId().toString().toLowerCase(Locale.ROOT);
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

				if (!this.userRolesSynced()) {
					this.syncRoles();
					hasChanges = true;
				}

				this.entityManager.flush();

				if (hasChanges){
					this.userTouchedIntegrationEventHandler.handle(this.userScopeFactory.getInstance().getUserId());
					this.indicatorAccessEventHandler.handle(this.userScopeFactory.getInstance().getUserId());
				}
				this.transactionManager.commit(status);
			} catch (Exception ex) {
				if (status != null) this.transactionManager.rollback(status);
				throw ex;
			}
		} finally {
			if (usedResource) this.lockByKeyManager.unlock(lockId);
		}
	}

	private Map<String, List<String>> getRolesFromClaims() {
		List<String> claimsRoles = this.claimExtractorFactory.getInstance().asStrings(this.currentPrincipalResolverFactory.getInstance().currentPrincipal(), ClaimNames.AllTenantRolesClaimName);
		if (claimsRoles == null) claimsRoles = new ArrayList<>();

		claimsRoles = claimsRoles.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(x -> !x.isEmpty())
				.filter(x -> {
					List<String> allowedRoles = this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles();

					if (this.conventionService.isListNullOrEmpty(allowedRoles)) return true;

					String[] parts = x.split(":", 2);
					return parts.length == 2 && allowedRoles.contains(parts[0]);
				})
				.distinct()
				.toList();


		// create map that contains tenant code as key and tenant roles as value
        return claimsRoles.stream()
				.map(s -> s.split(":", 2))
				.collect(Collectors.groupingBy(
						parts -> parts[1],
						Collectors.mapping(
								parts -> parts[0],
								Collectors.toList()
						)
				));
	}

	private boolean userRolesSynced() throws InvalidApplicationException {

		//sync for all tenants by user id
		Map<String, List<String>> claimRolesPerTenant = this.getRolesFromClaims();

		if (claimRolesPerTenant == null || claimRolesPerTenant.isEmpty()) return false;

		List<UserTenantRolesCacheService.UserTenantRolesCacheValue.TenantRole> tenantRoles = new ArrayList<>();
		UserTenantRolesCacheService.UserTenantRolesCacheValue cacheValue = this.userTenantRolesCacheService.lookup(this.userTenantRolesCacheService.buildKey(this.userScopeFactory.getInstance().getUserId()));
		if (cacheValue == null || this.conventionService.isListNullOrEmpty( cacheValue.getTenantRoles())) {
			List<UserRoleEntity> existingUserRoles = this.getUserRolesFromDatabase();
			List<TenantEntity> tenants = this.getTenantsFromDatabase();

			for (TenantEntity tenant: tenants) {
				UserTenantRolesCacheService.UserTenantRolesCacheValue.TenantRole tenantRole = new UserTenantRolesCacheService.UserTenantRolesCacheValue.TenantRole();
				tenantRole.setTenantCode(tenant.getCode());

				List<UserRoleEntity> userRolesEntities = existingUserRoles.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).toList();
				tenantRole.setRoles(userRolesEntities.stream().map(UserRoleEntity::getRole).distinct().toList());

				tenantRoles.add(tenantRole);
			}

			//add default tenant roles
			UserTenantRolesCacheService.UserTenantRolesCacheValue.TenantRole tenantRole = new UserTenantRolesCacheService.UserTenantRolesCacheValue.TenantRole();
			tenantRole.setTenantCode(this.tenantScopeFactory.getInstance().getDefaultTenantCode());

			List<UserRoleEntity> userRolesEntities = existingUserRoles.stream().filter(x -> x.getTenantId() == null).toList();
			tenantRole.setRoles(userRolesEntities.stream().map(UserRoleEntity::getRole).distinct().toList());

			tenantRoles.add(tenantRole);

			this.userTenantRolesCacheService.put(new UserTenantRolesCacheService.UserTenantRolesCacheValue(this.userScopeFactory.getInstance().getUserId(), tenantRoles));
		} else {
			tenantRoles = cacheValue.getTenantRoles();
		}

		for (UserTenantRolesCacheService.UserTenantRolesCacheValue.TenantRole tenantRole: tenantRoles) {
			List<String> claimsRoles = claimRolesPerTenant.getOrDefault(tenantRole.getTenantCode(), null);
			if (this.conventionService.isListNullOrEmpty(claimsRoles)) claimsRoles = new ArrayList<>();

			//check if db tenant roles are different from each tenant claims
			if (claimsRoles.size() != tenantRole.getRoles().size()) return false;

			for (String claim : claimsRoles) {
				if (tenantRole.getRoles().stream().noneMatch(claim::equalsIgnoreCase)) return false;
			}
		}
		return true;
	}

	private void syncRoles() throws InvalidApplicationException {
		List<UserRoleEntity> existingUserRoles = this.getUserRolesFromDatabase();

		Map<String, List<String>> claimRolesPerTenant = this.getRolesFromClaims();
		if (claimRolesPerTenant == null || claimRolesPerTenant.isEmpty()) {
			//remove all tenant user roles from db
			for (UserRoleEntity existing: existingUserRoles) {
				this.removeUserRoleEntity(existing, null);
			}
		} else {
			List<TenantEntity> tenants = this.getTenantsFromDatabase();
			//update roles for tenants that exist in claims
			for (String tenantCode: claimRolesPerTenant.keySet()) {
				boolean isDefaultTenant = tenantCode.equals(this.tenantScopeFactory.getInstance().getDefaultTenantCode());

				TenantEntity tenant = tenants.stream().filter(x -> x.getCode().equals(tenantCode)).findFirst().orElse(null);
				if (tenant == null && !isDefaultTenant) {
					logger.warn("can't sync tenant roles because tenant not found with code {}", tenantCode);
					continue;
				}

				List<UserRoleEntity> existingUserTenantRolesEntities = existingUserRoles.stream().filter(x -> isDefaultTenant ? x.getTenantId() == null: x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).toList();
				List<String> claimsRoles = claimRolesPerTenant.getOrDefault(tenantCode, null);
				if (this.conventionService.isListNullOrEmpty(claimsRoles)) {
					// remove all roles
					for (UserRoleEntity existing : existingUserTenantRolesEntities) {
						this.removeUserRoleEntity(existing, tenantCode);
					}
				} else {
					List<UUID> foundRoles = new ArrayList<>();
					for (String claimRole : claimsRoles) {
						UserRoleEntity roleEntity = existingUserTenantRolesEntities.stream().filter(x -> x.getRole().equals(claimRole)).findFirst().orElse(null);
						if (roleEntity == null) {
							try {
								this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
								//create role for this tenant
								roleEntity = this.buildRole(claimRole, isDefaultTenant ? null: tenant.getId());
								this.entityManager.persist(roleEntity);
							} finally {
								this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
							}
						}
						foundRoles.add(roleEntity.getId());
					}
					for (UserRoleEntity existing : existingUserTenantRolesEntities) {
						if (!foundRoles.contains(existing.getId())) {
							//remove role for this tenant
							this.removeUserRoleEntity(existing, tenantCode);
						}
					}
				}

			}

			//remove user roles from tenants that not exist in claims
			List<TenantEntity> notIncludedTenants = tenants.stream().filter(x -> !claimRolesPerTenant.containsKey(x.getCode())).toList();
            for (TenantEntity tenant: notIncludedTenants) {
                List<UserRoleEntity> existingUserTenantRolesEntities = existingUserRoles.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(tenant.getId())).toList();
				for (UserRoleEntity existing : existingUserTenantRolesEntities) {
					this.removeUserRoleEntity(existing, tenant.getCode());
				}
            }

			// delete user roles for default tenant if not exist in claims
			if (!claimRolesPerTenant.containsKey(this.tenantScopeFactory.getInstance().getDefaultTenantCode())) {
				List<UserRoleEntity> existingUserDefaultRolesEntities = existingUserRoles.stream().filter(x -> x.getTenantId() == null).toList();
				for (UserRoleEntity existing : existingUserDefaultRolesEntities) {
					this.removeUserRoleEntity(existing, this.tenantScopeFactory.getInstance().getDefaultTenantCode());
				}

			}
		}
	}

	private void removeUserRoleEntity(UserRoleEntity entity, String tenantCode) throws InvalidApplicationException {
		if (entity.getTenantId() != null) {
			try {
				this.tenantScopeFactory.getInstance().setTempTenant(this.tenantEntityManagerFactory.getInstance(), entity.getTenantId(), tenantCode);
				this.entityManager.remove(entity);
			} finally {
				this.tenantScopeFactory.getInstance().removeTempTenant(this.tenantEntityManagerFactory.getInstance());
			}
		} else {
			try {
				this.tenantScopeFactory.getInstance().setTempTenant(this.tenantEntityManagerFactory.getInstance(), null, this.tenantScopeFactory.getInstance().getDefaultTenantCode());
				this.entityManager.remove(entity);
			} finally {
				this.tenantScopeFactory.getInstance().removeTempTenant(this.tenantEntityManagerFactory.getInstance());
			}
		}
	}

	private List<TenantEntity> getTenantsFromDatabase() {

		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<TenantEntity> query = criteriaBuilder.createQuery(TenantEntity.class);
		Root<TenantEntity> root = query.from(TenantEntity.class);
		query = query.where(
				criteriaBuilder.and(
						criteriaBuilder.equal(root.get(TenantEntity._isActive), IsActive.Active)
				)
		);

		List<TenantEntity> tenantEntities = this.entityManager.createQuery(query).getResultList();
		if (tenantEntities == null) tenantEntities = new ArrayList<>();
		return tenantEntities;
	}

	private List<UserRoleEntity> getUserRolesFromDatabase() throws InvalidApplicationException {

		List<UserRoleEntity> existingUserRoles;

		try {
			this.tenantEntityManagerFactory.getInstance().disableTenantFilters();

			CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
			CriteriaQuery<UserRoleEntity> query = criteriaBuilder.createQuery(UserRoleEntity.class);
			Root<UserRoleEntity> root = query.from(UserRoleEntity.class);

			CriteriaBuilder.In<String> inRolesClause = criteriaBuilder.in(root.get(UserRoleEntity._role));
			for (String item : this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()) inRolesClause.value(item);
			query.where(criteriaBuilder.and(
					criteriaBuilder.equal(root.get(UserRoleEntity._userId), this.userScopeFactory.getInstance().getUserId()),
					this.conventionService.isListNullOrEmpty(this.authorizationConfiguration.getAuthorizationProperties().getAllowedTenantRoles()) ? criteriaBuilder.isNotNull(root.get(UserRoleEntity._role))  : inRolesClause
			));
			existingUserRoles = this.entityManager.createQuery(query).getResultList();

		} finally {
			this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
		}

		return existingUserRoles;
	}

	private UserRoleEntity buildRole(String role, UUID tenantId) throws InvalidApplicationException {
		UserRoleEntity data = new UserRoleEntity();
		data.setId(UUID.randomUUID());
		data.setUserId(this.userScopeFactory.getInstance().getUserId());
		data.setRole(role);
		if (tenantId != null) data.setTenantId(tenantId);
		data.setCreatedAt(Instant.now());
		return data;
	}

	@Override
	public void postHandle(@NonNull WebRequest request, ModelMap model) {
		this.tenantScopeFactory.getInstance().setTenant(null, null);
		this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
	}

	@Override
	public void afterCompletion(@NonNull WebRequest request, Exception ex) {
	}
}
