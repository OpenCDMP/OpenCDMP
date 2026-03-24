package org.opencdmp.websocket.interceptors;


import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorContextFactory;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.ClaimNames;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.tenant.TenantScopeImpl;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.interceptors.tenant.TenantByCodeCacheService;
import org.opencdmp.interceptors.tenant.TenantByIdCacheService;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Component
public class StompTenantScopeHeaderInterceptor implements ChannelInterceptor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StompTenantScopeHeaderInterceptor.class));

	@PersistenceContext
	public EntityManager entityManager;
	private final ConventionService conventionService;
	private final ClaimExtractorContextFactory claimExtractorContextFactory;
	private final TenantByCodeCacheService tenantByCodeCacheService;
	private final TenantByIdCacheService tenantByIdCacheService;
	private final TenantScopeFactory tenantScopeFactory;

	public StompTenantScopeHeaderInterceptor(ConventionService conventionService, ClaimExtractorContextFactory claimExtractorContextFactory, TenantByCodeCacheService tenantByCodeCacheService, TenantByIdCacheService tenantByIdCacheService, TenantScopeFactory tenantScopeFactory) {
	    this.conventionService = conventionService;
	    this.claimExtractorContextFactory = claimExtractorContextFactory;
	    this.tenantByCodeCacheService = tenantByCodeCacheService;
	    this.tenantByIdCacheService = tenantByIdCacheService;
        this.tenantScopeFactory = tenantScopeFactory;
    }

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
	    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	    assert accessor != null;
	    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			try {
				String tenantCode = accessor.getFirstNativeHeader(ClaimNames.TenantClaimName);
				logger.debug("retrieved request tenant header is: {}", tenantCode);
				if (tenantCode == null || this.conventionService.isNullOrEmpty(tenantCode)) return message;

				if (this.tenantScopeFactory.getInstance().supportExpansionTenant() && tenantCode.equalsIgnoreCase(this.tenantScopeFactory.getInstance().getDefaultTenantCode())) {
					logger.debug("parsed tenant header and set tenant to default tenant");
					this.tenantScopeFactory.getInstance().setTenant(null, tenantCode);
					this.claimExtractorContextFactory.getInstance().putReplaceParameter(TenantScopeImpl.TenantReplaceParameter, tenantCode);
					return message;
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
					TenantByIdCacheService.TenantByIdCacheValue cacheValue = this.tenantByIdCacheService.lookup(this.tenantByIdCacheService.buildKey(tenantId));
					if (cacheValue != null) {
						tenantCode = cacheValue.getTenantCode();
					} else {
						tenantCode = this.getTenantCodeFromDatabase(tenantId);
					}
				}

				if (tenantId != null) {
					logger.debug("parsed tenant header and set tenant id to {}", tenantId);
					this.tenantScopeFactory.getInstance().setTenant(tenantId, tenantCode);
					this.claimExtractorContextFactory.getInstance().putReplaceParameter(TenantScopeImpl.TenantReplaceParameter, tenantCode);
				}
			}catch (Exception e) {
				logger.error(e.getMessage());
				throw new RuntimeException(e);
			}
	    }

        return message;
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
