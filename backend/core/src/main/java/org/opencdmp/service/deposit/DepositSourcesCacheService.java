package org.opencdmp.service.deposit;

import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class DepositSourcesCacheService extends CacheService<DepositSourcesCacheService.DepositSourceCacheValue> {

    public static class DepositSourceCacheValue {

        public DepositSourceCacheValue() {
        }

        public DepositSourceCacheValue(String tenantCode, List<DepositSourceEntity> sources) {
            this.tenantCode = tenantCode;
            this.sources = sources;
        }

        private String tenantCode;

        private List<DepositSourceEntity> sources;

        public List<DepositSourceEntity> getSources() {
            return sources;
        }

        public void setSources(List<DepositSourceEntity> sources) {
            this.sources = sources;
        }

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }
    }


    @Autowired
    public DepositSourcesCacheService(DepositSourcesCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<DepositSourceCacheValue> valueClass() {
        return DepositSourceCacheValue.class;
    }

    @Override
    public String keyOf(DepositSourceCacheValue value) {
        return this.buildKey(value.getTenantCode());
    }

    public String buildKey(String tenantCod) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$tenantCode$", tenantCod);
        return this.generateKey(keyParts);
    }
}
