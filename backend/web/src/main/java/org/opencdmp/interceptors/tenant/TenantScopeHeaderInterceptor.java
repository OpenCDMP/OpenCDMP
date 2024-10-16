package org.opencdmp.interceptors.tenant;


import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorContext;
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
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntity;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.util.List;
import java.util.UUID;

@Component
public class TenantScopeHeaderInterceptor implements WebRequestInterceptor {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantScopeHeaderInterceptor.class));
	private final TenantScope tenantScope;
	private final ConventionService conventionService;
	private final TenantByCodeCacheService tenantByCodeCacheService;
	private final TenantByIdCacheService tenantByIdCacheService;
	private final ClaimExtractorContext claimExtractorContext;
	private final CurrentPrincipalResolver currentPrincipalResolver;
	@PersistenceContext
	public EntityManager entityManager;

	@Autowired
	public TenantScopeHeaderInterceptor(
			TenantScope tenantScope,
			ConventionService conventionService,
			TenantByCodeCacheService tenantByCodeCacheService,
			TenantByIdCacheService tenantByIdCacheService,
			ClaimExtractorContext claimExtractorContext,
			CurrentPrincipalResolver currentPrincipalResolver
	) {
		this.tenantScope = tenantScope;
		this.conventionService = conventionService;
		this.tenantByCodeCacheService = tenantByCodeCacheService;
		this.tenantByIdCacheService = tenantByIdCacheService;
		this.claimExtractorContext = claimExtractorContext;
		this.currentPrincipalResolver = currentPrincipalResolver;
	}

	@Override
	public void preHandle(@NotNull WebRequest request) {
		if (!this.currentPrincipalResolver.currentPrincipal().isAuthenticated()) return;
		if (!this.tenantScope.isMultitenant()) return;

		String tenantCode = request.getHeader(ClaimNames.TenantClaimName);
		logger.debug("retrieved request tenant header is: {}", tenantCode);
		if (tenantCode == null || this.conventionService.isNullOrEmpty(tenantCode)) return;

		if (this.tenantScope.supportExpansionTenant() && tenantCode.equalsIgnoreCase(this.tenantScope.getDefaultTenantCode())) {
			logger.debug("parsed tenant header and set tenant to default tenant");
			this.tenantScope.setTenant(null, tenantCode);
			this.claimExtractorContext.putReplaceParameter(TenantScope.TenantReplaceParameter, tenantCode);
			return;
		}

		UUID tenantId = this.conventionService.parseUUIDSafe(tenantCode);
		if (tenantId == null) {
			TenantByCodeCacheService.TenantByCodeCacheValue cacheValue = this.tenantByCodeCacheService.lookup(this.tenantByCodeCacheService.buildKey(tenantCode));
			if (cacheValue != null) {
				tenantId = cacheValue.getTenantId();
			} else {
				tenantId = this.getTenantIdFromDatabase(tenantCode);
				this.tenantByCodeCacheService.put(new TenantByCodeCacheService.TenantByCodeCacheValue(tenantCode, tenantId));
				this.tenantByIdCacheService.put(new TenantByIdCacheService.TenantByIdCacheValue(tenantCode, tenantId));
			}
		} else {
			TenantByIdCacheService.TenantByIdCacheValue cacheValue = this.tenantByIdCacheService.lookup(this.tenantByIdCacheService.buildKey(tenantId));
			if (cacheValue != null) {
				tenantCode = cacheValue.getTenantCode();
			} else {
				tenantCode = this.getTenantCodeFromDatabase(tenantId);
				this.tenantByCodeCacheService.put(new TenantByCodeCacheService.TenantByCodeCacheValue(tenantCode, tenantId));
				this.tenantByIdCacheService.put(new TenantByIdCacheService.TenantByIdCacheValue(tenantCode, tenantId));
			}
		}

		if (tenantId != null) {
			logger.debug("parsed tenant header and set tenant id to {}", tenantId);
			this.tenantScope.setTenant(tenantId, tenantCode);
			this.claimExtractorContext.putReplaceParameter(TenantScope.TenantReplaceParameter, tenantCode);
		}
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

	@Override
	public void postHandle(@NonNull WebRequest request, ModelMap model) {

		this.tenantScope.setTenant(null, null);
		this.claimExtractorContext.removeReplaceParameter(TenantScope.TenantReplaceParameter);
	}

	@Override
	public void afterCompletion(@NonNull WebRequest request, Exception ex) {
	}
}

