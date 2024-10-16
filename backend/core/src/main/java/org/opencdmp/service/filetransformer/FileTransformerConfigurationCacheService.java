package org.opencdmp.service.filetransformer;

import org.opencdmp.filetransformerbase.interfaces.FileTransformerConfiguration;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class FileTransformerConfigurationCacheService extends CacheService<FileTransformerConfigurationCacheService.FileTransformerConfigurationCacheValue> {

    public static class FileTransformerConfigurationCacheValue {

        public FileTransformerConfigurationCacheValue() {
        }

        public FileTransformerConfigurationCacheValue(String repositoryId, String tenantCode, FileTransformerConfiguration configuration) {
            this.transformerId = repositoryId;
            this.configuration = configuration;
            this.tenantCode = tenantCode == null ? "" : tenantCode;
        }

        private String transformerId;
        private String tenantCode;

        public String getTransformerId() {
            return transformerId;
        }

        public void setTransformerId(String transformerId) {
            this.transformerId = transformerId;
        }

        private FileTransformerConfiguration configuration;

        public FileTransformerConfiguration getConfiguration() {
            return configuration;
        }

        public void setConfiguration(FileTransformerConfiguration configuration) {
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
    public FileTransformerConfigurationCacheService(FileTransformerConfigurationCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<FileTransformerConfigurationCacheService.FileTransformerConfigurationCacheValue> valueClass() {
        return FileTransformerConfigurationCacheService.FileTransformerConfigurationCacheValue.class;
    }

    @Override
    public String keyOf(FileTransformerConfigurationCacheService.FileTransformerConfigurationCacheValue value) {
        return this.buildKey(value.getTransformerId(), value.getTenantCode());
    }


    public String buildKey(String transformerId, String tenantCod) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$transformerId$", transformerId);
        keyParts.put("$tenantCode$", tenantCod);
        return this.generateKey(keyParts);
    }
}
