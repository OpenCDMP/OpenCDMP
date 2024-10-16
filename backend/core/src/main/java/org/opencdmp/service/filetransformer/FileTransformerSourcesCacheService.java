package org.opencdmp.service.filetransformer;

import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class FileTransformerSourcesCacheService extends CacheService<FileTransformerSourcesCacheService.FileTransformerSourceCacheValue> {

    public static class FileTransformerSourceCacheValue {

        public FileTransformerSourceCacheValue() {
        }

        public FileTransformerSourceCacheValue(String tenantCode, List<FileTransformerSourceEntity> sources) {
            this.tenantCode = tenantCode;
            this.sources = sources;
        }

        private String tenantCode;
        
        private List<FileTransformerSourceEntity> sources;


        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public List<FileTransformerSourceEntity> getSources() {
            return sources;
        }

        public void setSources(List<FileTransformerSourceEntity> sources) {
            this.sources = sources;
        }
    }


    @Autowired
    public FileTransformerSourcesCacheService(FileTransformerSourcesCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<FileTransformerSourceCacheValue> valueClass() {
        return FileTransformerSourceCacheValue.class;
    }

    @Override
    public String keyOf(FileTransformerSourceCacheValue value) {
        return this.buildKey(value.getTenantCode());
    }

    public String buildKey(String tenantCod) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$tenantCode$", tenantCod);
        return this.generateKey(keyParts);
    }
}
