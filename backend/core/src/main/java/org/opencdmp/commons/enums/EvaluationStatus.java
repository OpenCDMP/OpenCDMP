package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum EvaluationStatus implements DatabaseEnum<Short> {

    Running((short) 0),
    Completed((short) 1),
    ExecutionFailed((short) 2);

    private final Short value;

    EvaluationStatus(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, EvaluationStatus> map = EnumUtils.getEnumValueMap(EvaluationStatus.class);

    public static EvaluationStatus of(Short i) {
        return map.get(i);
    }

}
