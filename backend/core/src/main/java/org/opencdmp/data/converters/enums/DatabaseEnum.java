package org.opencdmp.data.converters.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public interface  DatabaseEnum<T> {
    @JsonValue
    T getValue();
}
