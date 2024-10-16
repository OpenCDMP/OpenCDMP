package org.opencdmp.interceptors.tenant;

import org.opencdmp.event.UserAddedToTenantEvent;
import org.opencdmp.event.UserRemovedFromTenantEvent;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserAllowedTenantCacheService extends CacheService<UserAllowedTenantCacheService.UserAllowedTenantCacheValue> {

    public static class UserAllowedTenantCacheValue {

        public UserAllowedTenantCacheValue() {
        }

        public UserAllowedTenantCacheValue(UUID userId, UUID tenantId, boolean isAllowed) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.isAllowed = isAllowed;
        }

        private UUID userId;

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        private UUID tenantId;

        public UUID getTenantId() {
            return tenantId;
        }

        public void setTenantId(UUID tenantId) {
            this.tenantId = tenantId;
        }

        private boolean isAllowed;

        public boolean isAllowed() {
            return isAllowed;
        }

        public void setAllowed(boolean allowed) {
            isAllowed = allowed;
        }
    }

    @Autowired
    public UserAllowedTenantCacheService(UserAllowedTenantCacheOptions options) {
        super(options);
    }

    @EventListener
    public void handleUserRemovedFromTenantEvent(UserRemovedFromTenantEvent event) {
        this.evict(this.buildKey(event.getUserId(), event.getTenantId()));
    }

    @EventListener
    public void handleUserAddedToTenantEvent(UserAddedToTenantEvent event) {
        this.evict(this.buildKey(event.getUserId(), event.getTenantId()));
    }

    @Override
    protected Class<UserAllowedTenantCacheValue> valueClass() {
        return UserAllowedTenantCacheValue.class;
    }

    @Override
    public String keyOf(UserAllowedTenantCacheValue value) {
        return this.buildKey(value.getUserId(), value.getTenantId());
    }

    public String buildKey(UUID userId, UUID tenantId) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$user_id$", userId.toString().toLowerCase(Locale.ROOT));
        keyParts.put("$tenant_id$", tenantId.toString().toLowerCase(Locale.ROOT));
        return this.generateKey(keyParts);
    }
}

