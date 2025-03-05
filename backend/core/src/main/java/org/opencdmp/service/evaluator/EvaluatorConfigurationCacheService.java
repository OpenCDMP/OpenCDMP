package org.opencdmp.service.evaluator;

import gr.cite.tools.cache.CacheService;
import org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class EvaluatorConfigurationCacheService extends CacheService<EvaluatorConfigurationCacheService.EvaluatorConfigurationCacheValue> {

    public static class EvaluatorConfigurationCacheValue {

        public EvaluatorConfigurationCacheValue() {
        }

        public EvaluatorConfigurationCacheValue(String repositoryId, String tenantCode, EvaluatorConfiguration configuration) {
            this.evaluatorId = repositoryId;
            this.configuration = configuration;
            this.tenantCode = tenantCode == null ? "" : tenantCode;
        }

        private String evaluatorId;
        private String tenantCode;

        public String getEvaluatorId() {
            return evaluatorId;
        }

        public void setEvaluatorId(String evaluatorId) {
            this.evaluatorId = evaluatorId;
        }

        private EvaluatorConfiguration configuration;

        public EvaluatorConfiguration getConfiguration() {
            return configuration;
        }

        public void setConfiguration(EvaluatorConfiguration configuration) {
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
    public EvaluatorConfigurationCacheService(EvaluatorConfigurationCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<EvaluatorConfigurationCacheService.EvaluatorConfigurationCacheValue> valueClass() {
        return EvaluatorConfigurationCacheService.EvaluatorConfigurationCacheValue.class;
    }

    @Override
    public String keyOf(EvaluatorConfigurationCacheService.EvaluatorConfigurationCacheValue value) {
        return this.buildKey(value.getEvaluatorId(), value.getTenantCode());
    }


    public String buildKey(String evaluatorId, String tenantCod) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$evaluatorId$", evaluatorId);
        keyParts.put("$tenantCode$", tenantCod);
        return this.generateKey(keyParts);
    }
}
