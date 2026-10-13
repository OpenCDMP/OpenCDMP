package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The unit an answer report counts. Not persisted — a report request is stateless.
 */
public enum ReportSubjectType {

    Description((short) 0),
    Plan((short) 1);

    private final Short value;

    ReportSubjectType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, ReportSubjectType> map = Arrays.stream(ReportSubjectType.values()).collect(Collectors.toMap(ReportSubjectType::getValue, x -> x));

    public static ReportSubjectType of(Short value) {
        return map.get(value);
    }
}
