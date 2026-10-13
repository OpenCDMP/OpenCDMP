package org.opencdmp.service.usertermsacceptance;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.commons.types.tenantconfiguration.TermsAndConditionsTenantConfigurationEntity;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.UserTermsAcceptanceEntity;
import org.opencdmp.query.TenantConfigurationQuery;
import org.opencdmp.query.UserTermsAcceptanceQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserTermsAcceptanceServiceImpl implements UserTermsAcceptanceService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserTermsAcceptanceServiceImpl.class));

    private static final short DEFAULT_TERMS_VERSION = 1;

    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final QueryFactory queryFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final UserScopeFactory userScopeFactory;
    private final JsonHandlingService jsonHandlingService;
    private final AuditService auditService;

    @Autowired
    public UserTermsAcceptanceServiceImpl(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            QueryFactory queryFactory,
            TenantScopeFactory tenantScopeFactory,
            UserScopeFactory userScopeFactory,
            JsonHandlingService jsonHandlingService,
            AuditService auditService) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.queryFactory = queryFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.userScopeFactory = userScopeFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.auditService = auditService;
    }

    @Override
    public UserTermsStatus getStatus() throws InvalidApplicationException {
        logger.debug(new MapLogEntry("getting terms acceptance status"));

        UserTermsStatus result = new UserTermsStatus();

        TenantConfigurationEntity config = this.findActiveTermsConfig();
        if (config == null) {
            result.setTermsOfServiceAcceptanceRequired(false);
            return result;
        }

        TermsAndConditionsTenantConfigurationEntity configEntity = this.jsonHandlingService.fromJsonSafe(TermsAndConditionsTenantConfigurationEntity.class, config.getValue());
        String content = configEntity != null ? configEntity.getContent() : null;
        result.setContent(content);

        UUID userId = this.userScopeFactory.getInstance().getUserIdSafe();
        if (userId == null) {
            result.setTermsOfServiceAcceptanceRequired(true);
            return result;
        }

        short currentVersion = this.resolveVersion(configEntity);
        result.setTermsOfServiceAcceptanceRequired(!this.hasUserAccepted(userId, currentVersion));
        return result;
    }

    @Override
    public void accept(String clientIp) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("recording terms acceptance").And("clientIp", clientIp));

        UUID userId = this.userScopeFactory.getInstance().getUserId();

        TenantConfigurationEntity config = this.findActiveTermsConfig();
        String content = null;
        TermsAndConditionsTenantConfigurationEntity configEntity = null;
        if (config != null) {
            configEntity = this.jsonHandlingService.fromJsonSafe(TermsAndConditionsTenantConfigurationEntity.class, config.getValue());
            if (configEntity != null) content = configEntity.getContent();
        }

        short termsVersion = this.resolveVersion(configEntity);

        UserTermsAcceptanceEntity entity = new UserTermsAcceptanceEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setTenantId(this.tenantScopeFactory.getInstance().isDefaultTenant() ? null : this.tenantScopeFactory.getInstance().getTenant());
        entity.setAcceptedAt(Instant.now());
        entity.setClientIp(clientIp);
        entity.setTermsVersion(termsVersion);

        this.tenantEntityManagerFactory.getInstance().persist(entity);
        this.tenantEntityManagerFactory.getInstance().flush();

        this.auditService.track(AuditableAction.UserTermsAcceptance_Accept, Map.ofEntries(
                new AbstractMap.SimpleEntry<>("userId", userId),
                new AbstractMap.SimpleEntry<>("clientIp", clientIp),
                new AbstractMap.SimpleEntry<>("termsVersion", termsVersion),
                new AbstractMap.SimpleEntry<>("content", content)
        ));
    }

    private TenantConfigurationEntity findActiveTermsConfig() throws InvalidApplicationException {
        TenantConfigurationQuery query = this.queryFactory.query(TenantConfigurationQuery.class)
                .disableTracking()
                .isActive(IsActive.Active)
                .types(TenantConfigurationType.TermsAndConditions);

        if (this.tenantScopeFactory.getInstance().isDefaultTenant()) {
            query.tenantIsSet(false);
        } else {
            query.tenantIsSet(true).tenantIds(this.tenantScopeFactory.getInstance().getTenant());
        }

        return query.first();
    }

    private boolean hasUserAccepted(UUID userId, short termsVersion) throws InvalidApplicationException {

        UserTermsAcceptanceQuery query = this.queryFactory.query(UserTermsAcceptanceQuery.class).disableTracking().userIds(userId).termsVersions(termsVersion);

        if (this.tenantScopeFactory.getInstance().isDefaultTenant()) {
            query.tenantIsSet(false);
        } else {
            query.tenantIsSet(true).tenantIds(this.tenantScopeFactory.getInstance().getTenant());
        }

        return query.count() > 0;
    }

    private short resolveVersion(TermsAndConditionsTenantConfigurationEntity configEntity) {
        if (configEntity == null || configEntity.getVersion() == null) return DEFAULT_TERMS_VERSION;
        return configEntity.getVersion();
    }
}
