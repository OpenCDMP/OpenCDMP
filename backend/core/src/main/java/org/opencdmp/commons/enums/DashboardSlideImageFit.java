package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DashboardSlideImageFit implements DatabaseEnum<Short> {

    Fill((short) 0),
    Fit((short) 1);

    private final Short value;

    DashboardSlideImageFit(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, DashboardSlideImageFit> map = EnumUtils.getEnumValueMap(DashboardSlideImageFit.class);

    public static DashboardSlideImageFit of(Short i) {
        return map.get(i);
    }

}
