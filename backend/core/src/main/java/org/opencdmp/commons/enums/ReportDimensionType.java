package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A group-by key of an answer report. {@link #Question} breaks the subjects down by the
 * answer to another selected question instead of by a structural property.
 */
public enum ReportDimensionType {

    DescriptionStatus((short) 0),
    PlanStatus((short) 1),
    PlanAccessType((short) 2),
    PlanBlueprint((short) 3),
    PlanVersionStatus((short) 4),
    TemplateVersion((short) 5),
    CreatedPeriod((short) 6),
    FinalizedPeriod((short) 7),
    Question((short) 8),
    PlanQuestion((short) 9),
    Tenant((short) 10);

    private final Short value;

    ReportDimensionType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, ReportDimensionType> map = Arrays.stream(ReportDimensionType.values()).collect(Collectors.toMap(ReportDimensionType::getValue, x -> x));

    public static ReportDimensionType of(Short value) {
        return map.get(value);
    }
}
