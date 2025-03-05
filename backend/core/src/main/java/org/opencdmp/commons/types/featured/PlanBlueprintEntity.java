package org.opencdmp.commons.types.featured;

import java.util.UUID;

public class PlanBlueprintEntity {

    private UUID groupId;

    private Integer ordinal;

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }
}
