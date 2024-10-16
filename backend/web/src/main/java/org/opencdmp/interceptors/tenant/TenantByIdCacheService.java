package org.opencdmp.interceptors.tenant;

import gr.cite.tools.cache.CacheService;
import org.opencdmp.event.TenantTouchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class TenantByIdCacheService extends CacheService<TenantByIdCacheService.TenantByIdCacheValue> {

    public static class TenantByIdCacheValue {

        public TenantByIdCacheValue() {
        }

        public TenantByIdCacheValue(String tenantCode, UUID tenantId) {
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


    @Autowired
    public TenantByIdCacheService(TenantByIdCacheOptions options) {
        super(options);
    }

    @EventListener
    public void handleTenantTouchedEvent(TenantTouchedEvent event) {
        if (event.getTenantId() != null)
            this.evict(this.buildKey(event.getTenantId()));
    }

    @Override
    protected Class<TenantByIdCacheValue> valueClass() {
        return TenantByIdCacheValue.class;
    }

    @Override
    public String keyOf(TenantByIdCacheValue value) {
        return this.buildKey(value.getTenantId());
    }

    public String buildKey(UUID id) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$tenantId$", id.toString().toLowerCase(Locale.ROOT));
        return this.generateKey(keyParts);
    }
}
