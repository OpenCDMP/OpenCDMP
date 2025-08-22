package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanUserRole implements DatabaseEnum<Short> {

    Owner((short) 0),
    Viewer((short) 1),
    DescriptionContributor((short) 2),
    Reviewer((short) 3),
    DataSteward((short) 4),
    DataPrivacyOfficer((short) 5),
    EthicsReviewer((short) 6);

    private final Short value;

    PlanUserRole(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, PlanUserRole> map = EnumUtils.getEnumValueMap(PlanUserRole.class);

    public static PlanUserRole of(Short i) {
        return map.get(i);
    }

}
