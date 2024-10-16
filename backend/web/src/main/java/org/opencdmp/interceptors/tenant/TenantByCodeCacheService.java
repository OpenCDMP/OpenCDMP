package org.opencdmp.interceptors.tenant;

import gr.cite.tools.cache.CacheService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.event.TenantTouchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class TenantByCodeCacheService extends CacheService<TenantByCodeCacheService.TenantByCodeCacheValue> {

    public static class TenantByCodeCacheValue {

        public TenantByCodeCacheValue() {
        }

        public TenantByCodeCacheValue(String tenantCode, UUID tenantId) {
            this.tenantCode = tenantCode;
            this.tenantId = tenantId;
        }

        private String tenantCode;

        public String getTenantCode() {
            return this.tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        private UUID tenantId;

        public UUID getTenantId() {
            return this.tenantId;
        }

        public void setTenantId(UUID tenantId) {
            this.tenantId = tenantId;
        }
    }

    private final ConventionService conventionService;

    @Autowired
    public TenantByCodeCacheService(TenantByCodeCacheOptions options, ConventionService conventionService) {
        super(options);
        this.conventionService = conventionService;
    }

    @EventListener
    public void handleTenantTouchedEvent(TenantTouchedEvent event) {
        if (!this.conventionService.isNullOrEmpty(event.getTenantCode()))
            this.evict(this.buildKey(event.getTenantCode()));
    }

    @Override
    protected Class<TenantByCodeCacheValue> valueClass() {
        return TenantByCodeCacheValue.class;
    }

    @Override
    public String keyOf(TenantByCodeCacheValue value) {
        return this.buildKey(value.getTenantCode());
    }

    public String buildKey(String code) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$code$", code);
        return this.generateKey(keyParts);
    }
}
