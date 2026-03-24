package org.opencdmp.websocket.interceptors;


import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorFactory;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.ClaimNames;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.TenantUserEntity;
import org.opencdmp.data.UserEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.interceptors.tenant.UserAllowedTenantCacheService;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.UUID;


@Component
public class StompTenantInterceptor implements ChannelInterceptor {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StompTenantInterceptor.class));

	@PersistenceContext
	public EntityManager entityManager;
	private final ClaimExtractorFactory claimExtractorFactory;
	private final ErrorThesaurusProperties errors;
	private final UserAllowedTenantCacheService userAllowedTenantCacheService;
	private final QueryUtilsService queryUtilsService;
	private final UserScopeFactory userScopeFactory;
	private final TenantScopeFactory tenantScopeFactory;
	private final TenantEntityManagerFactory tenantEntityManagerFactory;
	private final CurrentPrincipalResolverFactory currentPrincipalResolverFactory;
	private final AuthorizationService authorizationService;
	public StompTenantInterceptor(ClaimExtractorFactory claimExtractorFactory, ErrorThesaurusProperties errors, UserAllowedTenantCacheService userAllowedTenantCacheService, QueryUtilsService queryUtilsService, UserScopeFactory userScopeFactory, TenantScopeFactory tenantScopeFactory, TenantEntityManagerFactory tenantEntityManagerFactory, CurrentPrincipalResolverFactory currentPrincipalResolverFactory, AuthorizationService authorizationService) {
		this.claimExtractorFactory = claimExtractorFactory;
		this.errors = errors;
		this.userAllowedTenantCacheService = userAllowedTenantCacheService;
		this.queryUtilsService = queryUtilsService;
        this.userScopeFactory = userScopeFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.currentPrincipalResolverFactory = currentPrincipalResolverFactory;
        this.authorizationService = authorizationService;
    }

	@Override
	public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		assert accessor != null;
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			try {

				if (!currentPrincipalResolverFactory.getInstance().currentPrincipal().isAuthenticated()) return message;
				if (!tenantScopeFactory.getInstance().isMultitenant()) return message;

				boolean isAllowedNoTenant = this.authorizationService.authorize(Permission.AllowNoTenant);
				if (tenantScopeFactory.getInstance().isSet() && this.entityManager != null) {
					List<String> currentPrincipalTenantCodes = this.claimExtractorFactory.getInstance().asStrings(currentPrincipalResolverFactory.getInstance().currentPrincipal(), ClaimNames.TenantCodesClaimName);
					if ((currentPrincipalTenantCodes == null || !currentPrincipalTenantCodes.contains(tenantScopeFactory.getInstance().getTenantCode())) && !isAllowedNoTenant) {
						logger.warn("tenant not allowed {}", tenantScopeFactory.getInstance().getTenant());
						throw new MyForbiddenException(this.errors.getTenantNotAllowed().getCode(), this.errors.getTenantNotAllowed().getMessage());
					}

					boolean isUserAllowedTenant = false;
					if (tenantScopeFactory.getInstance().supportExpansionTenant() && tenantScopeFactory.getInstance().isDefaultTenant()) {
						isUserAllowedTenant = true;
					} else {
						UserAllowedTenantCacheService.UserAllowedTenantCacheValue cacheValue = this.userAllowedTenantCacheService.lookup(this.userAllowedTenantCacheService.buildKey(this.userScopeFactory.getInstance().getUserId(), tenantScopeFactory.getInstance().getTenant()));
						if (cacheValue != null) {
							isUserAllowedTenant = cacheValue.isAllowed();
						} else {
							List<String> tenants = this.claimExtractorFactory.getInstance().asStrings(currentPrincipalResolverFactory.getInstance().currentPrincipal(), ClaimNames.TenantCodesClaimName);
							if (tenants.contains(tenantScopeFactory.getInstance().getTenantCode())) isUserAllowedTenant = this.isUserAllowedTenant(userScopeFactory, tenantScopeFactory.getInstance());
						}
					}

					if (isUserAllowedTenant) {
						tenantEntityManagerFactory.getInstance().reloadTenantFilters();
					} else {
						if (isAllowedNoTenant) {
							tenantScopeFactory.getInstance().setTenant(null, null);
						} else {
							logger.warn("tenant not allowed {}", tenantScopeFactory.getInstance().getTenant());
							throw new MyForbiddenException(this.errors.getTenantNotAllowed().getCode(), this.errors.getTenantNotAllowed().getMessage());
						}
					}
				} else {
					if (!isAllowedNoTenant) {
						logger.warn("tenant scope not provided");
						throw new MyForbiddenException(this.errors.getMissingTenant().getCode(), this.errors.getMissingTenant().getMessage());
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return message;
	}

	private boolean isUserAllowedTenant(UserScopeFactory userScopeImpl, TenantScope tenantScope) {
		if (userScopeImpl.getInstance().isSet()) {
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
															criteriaBuilder.equal(subQueryRoot.get(TenantUserEntity._tenantId), tenantScope.getTenant()),
															criteriaBuilder.equal(subQueryRoot.get(TenantUserEntity._userId), userScopeImpl.getInstance().getUserId()),
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
			return !results.isEmpty();
		}

		return false;
	}
}
