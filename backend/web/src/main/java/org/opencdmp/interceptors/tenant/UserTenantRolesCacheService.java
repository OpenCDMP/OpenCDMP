package org.opencdmp.interceptors.tenant;

import org.opencdmp.event.UserAddedToTenantEvent;
import org.opencdmp.event.UserRemovedFromTenantEvent;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserTenantRolesCacheService extends CacheService<UserTenantRolesCacheService.UserTenantRolesCacheValue> {

    public static class UserTenantRolesCacheValue {

        public UserTenantRolesCacheValue() {
        }

        public UserTenantRolesCacheValue(UUID userId, UUID tenantId, List<String> roles) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.roles = roles;
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

        private List<String> roles;

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }

    @Autowired
    public UserTenantRolesCacheService(UserTenantRolesCacheOptions options) {
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
    protected Class<UserTenantRolesCacheValue> valueClass() {
        return UserTenantRolesCacheValue.class;
    }

    @Override
    public String keyOf(UserTenantRolesCacheValue value) {
        return this.buildKey(value.getUserId(), value.getTenantId());
    }

    public String buildKey(UUID userId, UUID tenantId) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$user_id$", userId.toString().toLowerCase(Locale.ROOT));
        keyParts.put("$tenant_id$", tenantId.toString().toLowerCase(Locale.ROOT));
        return this.generateKey(keyParts);
    }
}
