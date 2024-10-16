package org.opencdmp.commons.enums.accounting;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum AccountingAggregateType implements DatabaseEnum<Short> {

    Sum((short) 0),
    Average((short) 1),
    Max((short) 2),
    Min((short) 3);

    private final Short value;

    AccountingAggregateType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, AccountingAggregateType> map = EnumUtils.getEnumValueMap(AccountingAggregateType.class);

    public static AccountingAggregateType of(Short i) {
        return map.get(i);
    }

}
