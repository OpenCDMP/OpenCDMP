package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.types.planblueprint.migration.PlanBlueprintSystemFieldType;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum SystemFieldType implements DatabaseEnum<Short> {

    PlanTitle((short)0),
    PlanDescription((short)1),
    PlanLanguage((short)2),
    PlanContact((short)3),
    PlanAccessRights((short)4),
    PlanUser((short)5),
    DescriptionTitle((short)100),
    DescriptionDescription((short)101),
    DescriptionTag((short)102);
    private final Short value;

    SystemFieldType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, SystemFieldType> map = EnumUtils.getEnumValueMap(SystemFieldType.class);

    public static SystemFieldType of(Short i) {
        return map.get(i);
    }

    public static SystemFieldType buildFromBlueprintFieldTypeMigration(PlanBlueprintSystemFieldType blueprintSystemFieldType) {
        switch (blueprintSystemFieldType){
            case Title -> {
                return SystemFieldType.PlanTitle;
            }
            case Description -> {
                return SystemFieldType.PlanDescription;
            }
            case Language -> {
                return SystemFieldType.PlanLanguage;
            }
            case Contact -> {
                return SystemFieldType.PlanContact;
            }
            case AccessRights -> {
                return SystemFieldType.PlanAccessRights;
            }
            case User -> {
                return SystemFieldType.PlanUser;
            }
            default -> throw new MyApplicationException("unrecognized type " + blueprintSystemFieldType);
        }

    }
}