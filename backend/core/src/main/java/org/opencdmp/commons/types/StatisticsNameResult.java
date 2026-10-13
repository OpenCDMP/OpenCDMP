package org.opencdmp.commons.types;

import java.util.UUID;

public class StatisticsNameResult {
    private UUID key;
    private String value;

    public StatisticsNameResult(UUID key, String value) {
        this.key = key;
        this.value = value;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
