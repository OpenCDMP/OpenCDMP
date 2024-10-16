package org.opencdmp.service.supportivematerial;


import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;

@Service
public class SupportiveMaterialCacheService extends CacheService<SupportiveMaterialCacheService.SupportiveMaterialCacheValue> {

    public static class SupportiveMaterialCacheValue {

        public SupportiveMaterialCacheValue() {}

        public SupportiveMaterialCacheValue(String language, SupportiveMaterialFieldType type, byte[] content) {
            this.language = language;
            this.type = type;
            this.content = content;
        }

        private String language;
        private SupportiveMaterialFieldType type;

        private byte[] content;

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public SupportiveMaterialFieldType getType() {
            return type;
        }

        public void setType(SupportiveMaterialFieldType type) {
            this.type = type;
        }
    }

    @Autowired
    public SupportiveMaterialCacheService(SupportiveMaterialCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<SupportiveMaterialCacheValue> valueClass() {return SupportiveMaterialCacheValue.class;}


    public String keyOf(SupportiveMaterialCacheValue value) {
        return this.buildKey(value.getLanguage(), value.getType());
    }

    public String buildKey(String language, SupportiveMaterialFieldType type) {
        HashMap<String, String> keyParts = new HashMap<>();

        keyParts.put("$lang$", language.toLowerCase(Locale.ROOT));
        keyParts.put("$type$", type.name().toLowerCase(Locale.ROOT));

        return this.generateKey(keyParts);
    }

}
