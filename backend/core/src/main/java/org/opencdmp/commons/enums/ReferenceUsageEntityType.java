package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/** Which entity {@code POST /api/reference/usage} counts references against. Not persisted. */
public enum ReferenceUsageEntityType {

    Plan((short) 0), Description((short) 1);

    private final Short value;

    ReferenceUsageEntityType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

}
