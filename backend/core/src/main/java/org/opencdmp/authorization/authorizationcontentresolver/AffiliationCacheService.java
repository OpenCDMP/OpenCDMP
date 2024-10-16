package org.opencdmp.authorization.authorizationcontentresolver;

import gr.cite.tools.cache.CacheService;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.convention.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class AffiliationCacheService extends CacheService<AffiliationCacheService.AffiliationCacheValue> {

    public static class AffiliationCacheValue {

        public AffiliationCacheValue() {
        }

        public AffiliationCacheValue(UUID tenantId, UUID userId, UUID entityId, String entityType, AffiliatedResource affiliatedResource) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.entityId = entityId;
            this.entityType = entityType;
            this.affiliatedResource = affiliatedResource;
        }

        private UUID userId;
        private UUID tenantId;
        private UUID entityId;
        private String entityType;
        private AffiliatedResource affiliatedResource;

        public UUID getUserId() {
            return this.userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getTenantId() {
            return this.tenantId;
        }

        public void setTenantId(UUID tenantId) {
            this.tenantId = tenantId;
        }

        public UUID getEntityId() {
            return this.entityId;
        }

        public void setEntityId(UUID entityId) {
            this.entityId = entityId;
        }

        public String getEntityType() {
            return this.entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public AffiliatedResource getAffiliatedResource() {
            return this.affiliatedResource;
        }

        public void setAffiliatedResource(AffiliatedResource affiliatedResource) {
            this.affiliatedResource = affiliatedResource;
        }
    }

    private final ConventionService conventionService;
    @Autowired
    public AffiliationCacheService(AffiliationCacheOptions options, ConventionService conventionService) {
        super(options);
	    this.conventionService = conventionService;
    }

    @Override
    protected Class<AffiliationCacheValue> valueClass() {
        return AffiliationCacheValue.class;
    }

    @Override
    public String keyOf(AffiliationCacheValue value) {
        return this.buildKey(value.getTenantId(), value.getUserId(), value.getEntityId(), value.getEntityType());
    }


    public String buildKey(UUID tenantId, UUID userId, UUID entityId, String entityType) {
        if (userId == null) throw new IllegalArgumentException("userId id is required");
        if (entityId == null) throw new IllegalArgumentException("entityId id is required");
        if (this.conventionService.isNullOrEmpty(entityType)) throw new IllegalArgumentException("entityType id is required");
        
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$user$", userId.toString().replace("-", "").toLowerCase(Locale.ROOT));
        keyParts.put("$tenant$", tenantId == null ? "" : userId.toString().replace("-", "").toLowerCase(Locale.ROOT));
        keyParts.put("$entity$", entityId.toString().replace("-", "").toLowerCase(Locale.ROOT));
        keyParts.put("$type$", entityType);
        return this.generateKey(keyParts);
    }
}
