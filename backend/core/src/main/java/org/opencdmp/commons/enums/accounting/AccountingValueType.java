package org.opencdmp.commons.enums.accounting;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum AccountingValueType implements DatabaseEnum<Short> {

    Plus((short) 0),
    Minus((short) 1),
    Reset((short) 2);

    private final Short value;

    AccountingValueType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, AccountingValueType> map = EnumUtils.getEnumValueMap(AccountingValueType.class);

    public static AccountingValueType of(Short i) {
        return map.get(i);
    }

}
