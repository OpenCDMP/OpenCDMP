package org.opencdmp.service.deposit;

import org.opencdmp.depositbase.repository.DepositConfiguration;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class DepositConfigurationCacheService extends CacheService<DepositConfigurationCacheService.DepositConfigurationCacheValue> {

    public static class DepositConfigurationCacheValue {

        public DepositConfigurationCacheValue() {
        }

        public DepositConfigurationCacheValue(String repositoryId, String tenantCode, DepositConfiguration configuration) {
            this.repositoryId = repositoryId;
            this.configuration = configuration;
            this.tenantCode = tenantCode == null ? "" : tenantCode;
        }

        private String repositoryId;
        private String tenantCode;

        public String getRepositoryId() {
            return repositoryId;
        }

        public void setRepositoryId(String repositoryId) {
            this.repositoryId = repositoryId;
        }

        private DepositConfiguration configuration;

        public DepositConfiguration getConfiguration() {
            return configuration;
        }

        public void setConfiguration(DepositConfiguration configuration) {
            this.configuration = configuration;
        }

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }
    }


    @Autowired
    public DepositConfigurationCacheService(DepositConfigurationCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<DepositConfigurationCacheValue> valueClass() {
        return DepositConfigurationCacheValue.class;
    }

    @Override
    public String keyOf(DepositConfigurationCacheValue value) {
        return this.buildKey(value.getRepositoryId(), value.getTenantCode());
    }


    public String buildKey(String repositoryId, String tenantCod) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$repositoryId$", repositoryId);
        keyParts.put("$tenantCode$", tenantCod);
        return this.generateKey(keyParts);
    }
}
