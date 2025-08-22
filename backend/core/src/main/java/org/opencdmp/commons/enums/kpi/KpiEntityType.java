package org.opencdmp.commons.enums.kpi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum KpiEntityType {

    Plan("plan"),
    Description("description"),
    Reference("reference"),
    User("user"),
    PlanBlueprint("planBlueprint"),
    DescriptionTemplate("descriptionTemplate"),
    Tenant("tenant");

    private static final Map<String, KpiEntityType> values = new HashMap<>();

    private final String mappedName;

    //For jackson parsing (used by MVC)
    @JsonValue
    public java.lang.String getMappedName() {
        return mappedName;
    }

    static {
        for (KpiEntityType e : values()) {
            values.put(e.asString(), e);
        }
    }

    private KpiEntityType(String mappedName) {
        this.mappedName = mappedName;
    }

    public String asString() {
        return this.mappedName;
    }

    public static KpiEntityType fromString(String value) {
        return values.getOrDefault(value, null);
    }
}
