package org.opencdmp.commons.enums.accounting;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum AccountingMeasureType implements DatabaseEnum<Short> {

    Time((short) 0),
    Information((short) 1),
    Throughput((short) 2),
    Unit((short) 3);

    private final Short value;

    AccountingMeasureType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, AccountingMeasureType> map = EnumUtils.getEnumValueMap(AccountingMeasureType.class);

    public static AccountingMeasureType of(Short i) {
        return map.get(i);
    }

}
