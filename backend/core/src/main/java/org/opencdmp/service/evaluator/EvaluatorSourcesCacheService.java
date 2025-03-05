package org.opencdmp.service.evaluator;

import gr.cite.tools.cache.CacheService;
import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class EvaluatorSourcesCacheService extends CacheService<EvaluatorSourcesCacheService.EvaluatorSourceCacheValue> {

    public static class EvaluatorSourceCacheValue {

        public EvaluatorSourceCacheValue() {
        }

        public EvaluatorSourceCacheValue(String tenantCode, List<EvaluatorSourceEntity> sources) {
            this.tenantCode = tenantCode;
            this.sources = sources;
        }

        private String tenantCode;

        private List<EvaluatorSourceEntity> sources;


        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public List<EvaluatorSourceEntity> getSources() {
            return sources;
        }

        public void setSources(List<EvaluatorSourceEntity> sources) {
            this.sources = sources;
        }
    }

    @Autowired
    public EvaluatorSourcesCacheService(EvaluatorSourcesCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<EvaluatorSourceCacheValue> valueClass() {
        return EvaluatorSourceCacheValue.class;
    }

    @Override
    public String keyOf(EvaluatorSourceCacheValue value) {
        return this.buildKey(value.getTenantCode());
    }

    public String buildKey(String tenantCod) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$tenantCode$", tenantCod);
        return this.generateKey(keyParts);
    }
}
