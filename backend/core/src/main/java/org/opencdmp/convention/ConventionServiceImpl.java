package org.opencdmp.convention;

import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ConventionServiceImpl implements ConventionService {
    private final static Pattern UUID_REGEX_PATTERN = Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ConventionServiceImpl.class));
    private final ErrorThesaurusProperties errors;

    @Autowired
    public ConventionServiceImpl(ErrorThesaurusProperties errors) {
        this.errors = errors;
    }

    @Override
    public Boolean isValidId(Integer id) {
        return id != null && id > 0;
    }

    @Override
    public Boolean isValidGuid(UUID guid) {
        return guid != null && !guid.equals(this.getEmptyUUID());
    }

    @Override
    public Boolean isValidUUID(String str) {
        if (this.isNullOrEmpty(str)) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

    @Override
    public UUID parseUUIDSafe(String str) {
        if (!this.isValidUUID(str)) {
            return null;
        }
        try {
            return UUID.fromString(str);
        } catch (Exception ex){
            logger.warn("invalid uuid" + str, ex);
            return null;
        }
    }

    @Override
    public Boolean isValidHash(String hash) {
        return !this.isNullOrEmpty(hash);
    }

    @Override
    public String hashValue(Object value) throws MyApplicationException {
        if (value == null) return this.stringEmpty();
        if (value instanceof Instant) return String.format("%ts", (Instant) value);
        throw new MyApplicationException(this.errors.getSystemError().getCode(), this.errors.getSystemError().getMessage());
    }

    @Override
    public String limit(String text, int maxLength) {
        if (this.isNullOrEmpty(text)) return text;
        if (text.length() > maxLength) return String.format("%s...", text.substring(0, maxLength));
        else return text;
    }

    @Override
    public String truncate(String text, int maxLength) {
        String truncated = text;
        if (text.length() < maxLength) return text;

        truncated = truncated.trim();
        truncated = truncated.replaceAll("\\s+", " ");//remove multiple spaces
        if (truncated.length() < maxLength) return truncated;
        truncated = truncated.replaceAll("([.!@#$%^&-=':;,<>?*\"/|])+", "");//remove multiple spaces
        if (truncated.length() < maxLength) return truncated;
        truncated = truncated.replaceAll("([aeiou])+", "");//remove multiple spaces
        if (truncated.length() < maxLength) return truncated;
        truncated = truncated.replaceAll("([AEIOU])+", "");//remove multiple spaces
        if (truncated.length() < maxLength) return truncated;

        if (text.length() > maxLength) return String.format("%s...", text.substring(0, maxLength));
        return text;

    }

    @Override
    public UUID getEmptyUUID() {
        return new UUID(0L, 0L);
    }

    @Override
    public boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public boolean isListNullOrEmpty(List<?> value) {
        if(value == null) return true;
        return value.isEmpty();
    }

    @Override
    public String stringEmpty() {
        return "";
    }

    @Override
    public String asPrefix(String name) {
        if (name == null) return null;
        return name + ".";
    }

    @Override
    public String asIndexer(String... names) {
        if (names == null) return null;
        return String.join(".", Arrays.stream(names).filter(x -> !this.isNullOrEmpty(x)).collect(Collectors.toList()));
    }

    @Override
    public String asIndexerPrefix(String part) {
        if (part == null) return null;
        return part + ".";
    }

    @Override
    public <K, V> Map<K, List<V>> toDictionaryOfList(List<V> items, Function<V, K> keySelector) {
        Map<K, List<V>> map = new HashMap<>();
        for (V model : items) {
            K key = keySelector.apply(model);
            if (!map.containsKey(key)) map.put(key, new ArrayList<V>());
            map.get(key).add(model);
        }
        return map;
    }
}
