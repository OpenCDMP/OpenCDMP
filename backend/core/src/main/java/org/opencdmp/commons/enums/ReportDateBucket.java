package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Granularity used when a date answer or a date dimension is turned into a bucket label.
 */
public enum ReportDateBucket {

    Year((short) 0),
    Month((short) 1);

    private final Short value;

    ReportDateBucket(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, ReportDateBucket> map = Arrays.stream(ReportDateBucket.values()).collect(Collectors.toMap(ReportDateBucket::getValue, x -> x));

    public static ReportDateBucket of(Short value) {
        return map.get(value);
    }
}
