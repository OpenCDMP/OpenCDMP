package org.opencdmp.commons.enums.kpi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum KpiVersionType {

    TotalCount("totalCount"),
    LatestVersion("latestVersion");

    private static final Map<String, KpiVersionType> values = new HashMap<>();

    private final String mappedName;

    //For jackson parsing (used by MVC)
    @JsonValue
    public String getMappedName() {
        return mappedName;
    }

    static {
        for (KpiVersionType e : values()) {
            values.put(e.asString(), e);
        }
    }

    private KpiVersionType(String mappedName) {
        this.mappedName = mappedName;
    }

    public String asString() {
        return this.mappedName;
    }

    public static KpiVersionType fromString(String value) {
        return values.getOrDefault(value, null);
    }
}
