package org.opencdmp.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JsonHandlingService {
    private final ObjectMapper objectMapper;

    public JsonHandlingService() {
        this.objectMapper = new ObjectMapper();
	    this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public String toJson(Object item) throws JsonProcessingException {
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

    public <T> T fromJson(Class<T> type, String json) throws JsonProcessingException {
        if (json == null) return null;
        return this.objectMapper.readValue(json, type);
    }

    public HashMap<String, String> mapFromJson(String json) throws JsonProcessingException {
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
