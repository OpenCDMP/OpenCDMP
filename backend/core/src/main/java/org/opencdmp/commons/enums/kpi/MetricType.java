package org.opencdmp.commons.enums.kpi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum MetricType {

    Overtime("overtime"),
    ;
    private static final Map<String, MetricType> values = new HashMap<>();

    private final String mappedName;

    //For jackson parsing (used by MVC)
    @JsonValue
    public String getMappedName() {
        return mappedName;
    }

    static {
        for (MetricType e : values()) {
            values.put(e.asString(), e);
        }
    }

    private MetricType(String mappedName) {
        this.mappedName = mappedName;
    }

    public String asString() {
        return this.mappedName;
    }

    public static MetricType fromString(String value) {
        return values.getOrDefault(value, null);
    }
}
