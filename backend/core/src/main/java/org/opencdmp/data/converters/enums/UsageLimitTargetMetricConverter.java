package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;

@Converter
public class UsageLimitTargetMetricConverter extends DatabaseEnumConverter<UsageLimitTargetMetric, String> {
    public UsageLimitTargetMetric of(String i) {
        return UsageLimitTargetMetric.of(i);
    }
}
