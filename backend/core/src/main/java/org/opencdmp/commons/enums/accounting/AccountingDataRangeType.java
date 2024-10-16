package org.opencdmp.commons.enums.accounting;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum AccountingDataRangeType implements DatabaseEnum<Short> {

    Custom((short) 0),
    Today((short) 1),
    ThisMonth((short) 2),
    ThisYear((short) 3);

    private final Short value;

    AccountingDataRangeType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, AccountingDataRangeType> map = EnumUtils.getEnumValueMap(AccountingDataRangeType.class);

    public static AccountingDataRangeType of(Short i) {
        return map.get(i);
    }

}
