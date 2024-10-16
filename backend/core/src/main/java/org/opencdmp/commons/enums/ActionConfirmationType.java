package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum ActionConfirmationType implements DatabaseEnum<Short> {

    MergeAccount((short) 0),
    RemoveCredential((short) 1),
    PlanInvitation((short) 2),
    UserInviteToTenant ((short) 3);

    private final Short value;

    ActionConfirmationType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, ActionConfirmationType> map = EnumUtils.getEnumValueMap(ActionConfirmationType.class);

    public static ActionConfirmationType of(Short i) {
        return map.get(i);
    }


}
