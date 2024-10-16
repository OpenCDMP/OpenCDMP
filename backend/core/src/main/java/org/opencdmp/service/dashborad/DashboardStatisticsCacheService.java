package org.opencdmp.service.dashborad;

import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.model.DashboardStatistics;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class DashboardStatisticsCacheService extends CacheService<DashboardStatisticsCacheService.DashboardStatisticsCacheValue> {

    public static final String publicKey  = "Public";
    public static class DashboardStatisticsCacheValue {

        public DashboardStatisticsCacheValue() {
            this.isPublic = true;
        }

        public DashboardStatisticsCacheValue(UUID userId, String tenantCode) {
            this.userId = userId;
            this.tenantCode = tenantCode;
            this.isPublic = false;
        }

        private UUID userId;
        private String tenantCode;
        private boolean isPublic;

        private DashboardStatistics dashboardStatistics;

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public DashboardStatistics getDashboardStatistics() {
            return dashboardStatistics;
        }

        public void setDashboardStatistics(DashboardStatistics dashboardStatistics) {
            this.dashboardStatistics = dashboardStatistics;
        }

        public boolean isPublic() {
            return isPublic;
        }

        public void setPublic(boolean aPublic) {
            isPublic = aPublic;
        }
    }


    @Autowired
    public DashboardStatisticsCacheService(DashboardStatisticsCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<DashboardStatisticsCacheValue> valueClass() {
        return DashboardStatisticsCacheValue.class;
    }

    @Override
    public String keyOf(DashboardStatisticsCacheValue value) {
        if (value.userId == null){
            if (value.isPublic) return this.buildKey(publicKey);
            else throw new IllegalArgumentException("Key not set");
        } else {
            return this.buildKey(this.generateUserTenantCacheKey(value.userId, value.tenantCode));
        }
    }

    public String buildKey(String key) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$key$", key);
        return this.generateKey(keyParts);
    }

    public String generateUserTenantCacheKey(UUID userId, String tenantCode) {
        StringBuilder builder = new StringBuilder();
        builder.append(userId.toString().toLowerCase(Locale.ROOT));
        if (tenantCode != null) {
            builder.append("_");
            builder.append(tenantCode.toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }
}
