package org.opencdmp.websocket.interceptors;


import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorContextFactory;
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
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.tenant.TenantScopeImpl;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.interceptors.tenant.TenantByCodeCacheService;
import org.opencdmp.interceptors.tenant.TenantByIdCacheService;
import org.opencdmp.interceptors.tenant.TenantScopeProperties;
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
public class StompTenantScopeClaimInterceptor implements ChannelInterceptor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StompTenantScopeClaimInterceptor.class));

	@PersistenceContext
	public EntityManager entityManager;
	private final ConventionService conventionService;
	private final ClaimExtractorContextFactory claimExtractorContextFactory;
	private final TenantByCodeCacheService tenantByCodeCacheService;
	private final TenantByIdCacheService tenantByIdCacheService;
	private final TenantScopeProperties tenantScopeProperties;
	private final ErrorThesaurusProperties errorThesaurusProperties;
	private final ClaimExtractorFactory claimExtractorFactory;
	private final String clientTenantClaimName;
	private final TenantScopeFactory tenantScopeFactory;
	private final CurrentPrincipalResolverFactory currentPrincipalResolver;

	public StompTenantScopeClaimInterceptor(ConventionService conventionService, ClaimExtractorContextFactory claimExtractorContextFactory, TenantByCodeCacheService tenantByCodeCacheService, TenantByIdCacheService tenantByIdCacheService, TenantScopeProperties tenantScopeProperties, ErrorThesaurusProperties errorThesaurusProperties, ClaimExtractorFactory claimExtractorFactory, TenantScopeFactory tenantScopeFactory, CurrentPrincipalResolverFactory currentPrincipalResolver) {
	    this.conventionService = conventionService;
	    this.claimExtractorContextFactory = claimExtractorContextFactory;
	    this.tenantByCodeCacheService = tenantByCodeCacheService;
	    this.tenantByIdCacheService = tenantByIdCacheService;
	    this.tenantScopeProperties = tenantScopeProperties;
	    this.errorThesaurusProperties = errorThesaurusProperties;
	    this.claimExtractorFactory = claimExtractorFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.currentPrincipalResolver = currentPrincipalResolver;
        this.clientTenantClaimName = this.tenantScopeProperties.getClientClaimsPrefix() + ClaimNames.TenantClaimName;
    }

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
	    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	    assert accessor != null;
	    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			try {
				if (!currentPrincipalResolver.getInstance().currentPrincipal().isAuthenticated()) return message;
				if (!tenantScopeFactory.getInstance().isMultitenant()) return message;

				MyPrincipal principal = currentPrincipalResolver.getInstance().currentPrincipal();
				if (principal != null && principal.isAuthenticated() /* principal.Claims.Any() */) {
					boolean scoped = this.scopeByPrincipal(principal, tenantScopeFactory.getInstance());
					if (!scoped) scoped = this.scopeByClient(principal, tenantScopeFactory.getInstance());
					if (!scoped && tenantScopeFactory.getInstance().isSet() && this.tenantScopeProperties.getEnforceTrustedTenant())
						throw new MyForbiddenException(this.errorThesaurusProperties.getMissingTenant().getCode(), this.errorThesaurusProperties.getMissingTenant().getMessage());
				}
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
	    }

        return message;
    }

	private boolean scopeByPrincipal(MyPrincipal principal, TenantScope tenantScope) {
		String tenantCode = this.claimExtractorFactory.getInstance().tenantString(principal);
		if (this.conventionService.isNullOrEmpty(tenantCode)) tenantCode = this.claimExtractorFactory.getInstance().asString(principal, this.clientTenantClaimName);
		if (tenantCode == null || this.conventionService.isNullOrEmpty(tenantCode)) return false;

		if (tenantScope.supportExpansionTenant() && tenantCode.equalsIgnoreCase(tenantScope.getDefaultTenantCode())) {
			logger.debug("parsed tenant header and set tenant to default tenant");
			tenantScope.setTenant(null, tenantCode);
			this.claimExtractorContextFactory.getInstance().putReplaceParameter(TenantScopeImpl.TenantReplaceParameter, tenantCode);
			return true;
		}

		UUID tenantId = this.conventionService.parseUUIDSafe(tenantCode);
		if (tenantId == null) {
			TenantByCodeCacheService.TenantByCodeCacheValue cacheValue = this.tenantByCodeCacheService.lookup(this.tenantByCodeCacheService.buildKey(tenantCode));
			if (cacheValue != null) {
				tenantId = cacheValue.getTenantId();
			} else {
				tenantId = this.getTenantIdFromDatabase(tenantCode);
			}
		} else {
			logger.debug("tenant claim was set to {}", tenantId);
			TenantByIdCacheService.TenantByIdCacheValue cacheValue = this.tenantByIdCacheService.lookup(this.tenantByIdCacheService.buildKey(tenantId));

			if (cacheValue != null) {
				tenantCode = cacheValue.getTenantCode();
			} else {
				tenantCode = this.getTenantCodeFromDatabase(tenantId);
			}
		}

		if (tenantId != null) {
			logger.debug("parsed tenant header and set tenant id to {}", tenantId);
			tenantScope.setTenant(tenantId, tenantCode);
			this.claimExtractorContextFactory.getInstance().putReplaceParameter(TenantScopeImpl.TenantReplaceParameter, tenantCode);
			return true;
		}
		return false;
	}

	private boolean scopeByClient(MyPrincipal principal, TenantScope tenantScope) throws InvalidApplicationException {
		String client = this.claimExtractorFactory.getInstance().client(principal);

		Boolean isWhiteListed = this.tenantScopeProperties.getWhiteListedClients() != null && !this.conventionService.isNullOrEmpty(client) && this.tenantScopeProperties.getWhiteListedClients().contains(client);
		logger.debug("client is whitelisted : {}, scope is set: {}, with value {}", isWhiteListed, tenantScope.isSet(), (tenantScope.isSet() ? tenantScope.getTenant() : null));

		return isWhiteListed && tenantScope.isSet();
	}

	private UUID getTenantIdFromDatabase(String tenantCode) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<TenantEntity> query = criteriaBuilder.createQuery(TenantEntity.class);
		Root<TenantEntity> root = query.from(TenantEntity.class);
		query = query.where(
				criteriaBuilder.and(
						criteriaBuilder.equal(root.get(TenantEntity._code), tenantCode),
						criteriaBuilder.equal(root.get(TenantEntity._isActive), IsActive.Active)
				)
		).multiselect(root.get(TenantEntity._id).alias(TenantEntity._id));
		List<TenantEntity> results = this.entityManager.createQuery(query).getResultList();
		if (results.size() == 1) {
			return results.getFirst().getId();
		}
		return null;
	}

	private String getTenantCodeFromDatabase(UUID tenantId) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<TenantEntity> query = criteriaBuilder.createQuery(TenantEntity.class);
		Root<TenantEntity> root = query.from(TenantEntity.class);
		query = query.where(
				criteriaBuilder.and(
						criteriaBuilder.equal(root.get(TenantEntity._id), tenantId),
						criteriaBuilder.equal(root.get(TenantEntity._isActive), IsActive.Active)
				)
		).multiselect(root.get(TenantEntity._code).alias(TenantEntity._code));
		List<TenantEntity> results = this.entityManager.createQuery(query).getResultList();
		if (results.size() == 1) {
			return results.getFirst().getCode();
		}
		return null;
	}
}
