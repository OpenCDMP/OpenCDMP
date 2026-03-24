package org.opencdmp.interceptors.tenant;


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
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.tenant.TenantScopeImpl;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.UUID;

@Component
public class TenantScopeClaimInterceptor implements WebRequestInterceptor {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantScopeClaimInterceptor.class));
    private final TenantScopeFactory tenantScopeFactory;
    private final ConventionService conventionService;
    private final TenantScopeProperties tenantScopeProperties;
    private final ErrorThesaurusProperties errorThesaurusProperties;
    private final ClaimExtractorFactory claimExtractorFactory;
    private final CurrentPrincipalResolverFactory currentPrincipalResolverFactory;
    private final String clientTenantClaimName;
    private final ClaimExtractorContextFactory claimExtractorContextFactory;
    private final TenantByCodeCacheService tenantByCodeCacheService;
    private final TenantByIdCacheService tenantByIdCacheService;
    @PersistenceContext
    public EntityManager entityManager;

    @Autowired
    public TenantScopeClaimInterceptor(
            TenantScopeFactory tenantScopeFactory,
            ConventionService conventionService,
            ClaimExtractorFactory claimExtractorFactory,
            CurrentPrincipalResolverFactory currentPrincipalResolverFactory,
            ErrorThesaurusProperties errorThesaurusProperties,
            TenantScopeProperties tenantScopeProperties,
            ClaimExtractorContextFactory claimExtractorContextFactory,
            TenantByCodeCacheService tenantByCodeCacheService,
            TenantByIdCacheService tenantByIdCacheService
    ) {
        this.tenantScopeFactory = tenantScopeFactory;
        this.conventionService = conventionService;
        this.currentPrincipalResolverFactory = currentPrincipalResolverFactory;
        this.claimExtractorFactory = claimExtractorFactory;
        this.errorThesaurusProperties = errorThesaurusProperties;
        this.tenantScopeProperties = tenantScopeProperties;
        this.claimExtractorContextFactory = claimExtractorContextFactory;
        this.tenantByCodeCacheService = tenantByCodeCacheService;
        this.tenantByIdCacheService = tenantByIdCacheService;
        this.clientTenantClaimName = this.tenantScopeProperties.getClientClaimsPrefix() + ClaimNames.TenantClaimName;
    }

    @Override
    public void preHandle(@NotNull WebRequest request) throws InvalidApplicationException {
        if (!this.currentPrincipalResolverFactory.getInstance().currentPrincipal().isAuthenticated()) return;
        if (!this.tenantScopeFactory.getInstance().isMultitenant()) return;

        MyPrincipal principal = this.currentPrincipalResolverFactory.getInstance().currentPrincipal();
        if (principal != null && principal.isAuthenticated() /* principal.Claims.Any() */) {
            boolean scoped = this.scopeByPrincipal(principal);
            if (!scoped) scoped = this.scopeByClient(principal);
            if (!scoped && this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeProperties.getEnforceTrustedTenant())
                throw new MyForbiddenException(this.errorThesaurusProperties.getMissingTenant().getCode(), this.errorThesaurusProperties.getMissingTenant().getMessage());
        }
    }

    private boolean scopeByPrincipal(MyPrincipal principal) {
        String tenantCode = this.claimExtractorFactory.getInstance().tenantString(principal);
        if (this.conventionService.isNullOrEmpty(tenantCode)) tenantCode = this.claimExtractorFactory.getInstance().asString(principal, this.clientTenantClaimName);
        if (tenantCode == null || this.conventionService.isNullOrEmpty(tenantCode)) return false;

        if (this.tenantScopeFactory.getInstance().supportExpansionTenant() && tenantCode.equalsIgnoreCase(this.tenantScopeFactory.getInstance().getDefaultTenantCode())) {
            logger.debug("parsed tenant header and set tenant to default tenant");
            this.tenantScopeFactory.getInstance().setTenant(null, tenantCode);
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
                this.tenantByCodeCacheService.put(new TenantByCodeCacheService.TenantByCodeCacheValue(tenantCode, tenantId));
                this.tenantByIdCacheService.put(new TenantByIdCacheService.TenantByIdCacheValue(tenantCode, tenantId));
            }
        } else {
            logger.debug("tenant claim was set to {}", tenantId);
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
            this.tenantScopeFactory.getInstance().setTenant(tenantId, tenantCode);
            this.claimExtractorContextFactory.getInstance().putReplaceParameter(TenantScopeImpl.TenantReplaceParameter, tenantCode);
            return true;
        }
        return false;
    }

    private boolean scopeByClient(MyPrincipal principal) throws InvalidApplicationException {
        String client = this.claimExtractorFactory.getInstance().client(principal);

        Boolean isWhiteListed = this.tenantScopeProperties.getWhiteListedClients() != null && !this.conventionService.isNullOrEmpty(client) && this.tenantScopeProperties.getWhiteListedClients().contains(client);
        logger.debug("client is whitelisted : {}, scope is set: {}, with value {}", isWhiteListed, this.tenantScopeFactory.getInstance().isSet(), (this.tenantScopeFactory.getInstance().isSet() ? this.tenantScopeFactory.getInstance().getTenant() : null));

        return isWhiteListed && this.tenantScopeFactory.getInstance().isSet();
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
        this.tenantScopeFactory.getInstance().setTenant(null, null);
        this.claimExtractorContextFactory.getInstance().removeReplaceParameter(TenantScopeImpl.TenantReplaceParameter);
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
    }
}
