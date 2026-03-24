package org.opencdmp.commons;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ObjectReader;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JsonHandlingService {
    private final JsonMapper objectMapper;

    public JsonHandlingService() {
        this.objectMapper = JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    public String toJson(Object item) throws JacksonException {
        if (item == null) return null;
        return this.objectMapper.writeValueAsString(item);
    }

    public String toJsonSafe(Object item) {
        if (item == null) return null;
        try {
            return this.objectMapper.writeValueAsString(item);
        } catch (Exception ex) {
            return null;
        }
    }

    public <T> T fromJson(Class<T> type, String json) throws JacksonException {
        if (json == null) return null;
        return this.objectMapper.readValue(json, type);
    }

    public HashMap<String, String> mapFromJson(String json) throws JacksonException {
        ObjectReader reader = this.objectMapper.readerFor(Map.class);
        return reader.readValue(json);
    }

    public <T> T fromJsonSafe(Class<T> type, String json) {
        if (json == null) return null;
        try {
            return this.objectMapper.readValue(json, type);
        } catch (Exception ex) {
            return null;
        }
    }
}
