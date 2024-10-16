package org.opencdmp.service.reference;

import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;

@Service
public class ReferenceCacheService extends CacheService<ReferenceCacheService.ReferenceCacheValue> {

    public static class ReferenceCacheValue {

        public ReferenceCacheValue() {}

        public ReferenceCacheValue(String type, ExternalReferenceCriteria externalUrlCriteria) {
            this.type = type;
            this.externalUrlCriteria = externalUrlCriteria;
        }

        private String type;

        private ExternalReferenceCriteria externalUrlCriteria;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public ExternalReferenceCriteria getExternalUrlCriteria() {
            return externalUrlCriteria;
        }

        public void setExternalUrlCriteria(ExternalReferenceCriteria externalUrlCriteria) {
            this.externalUrlCriteria = externalUrlCriteria;
        }
    }

    @Autowired
    public ReferenceCacheService(ReferenceCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<ReferenceCacheValue> valueClass() {return ReferenceCacheValue.class;}

    public String keyOf(ReferenceCacheValue value) {
        return this.buildKey(value.getType(), value.getExternalUrlCriteria());
    }

    public String buildKey(String externalType, ExternalReferenceCriteria externalUrlCriteria) {
        HashMap<String, String> keyParts = new HashMap<>();

        keyParts.put("$type$", externalType.toLowerCase(Locale.ROOT));

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(externalUrlCriteria);
        keyParts.put("$criteria$", stringBuffer.toString().toLowerCase(Locale.ROOT));

        return this.generateKey(keyParts);
    }
}
