package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanBlueprintExtraFieldDataType implements DatabaseEnum<Short> {

    Text((short) 0),
    RichTex((short) 1),
    Date((short) 2),
    Number((short) 3),
    ;

    private final Short value;

    PlanBlueprintExtraFieldDataType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanBlueprintExtraFieldDataType> map = EnumUtils.getEnumValueMap(PlanBlueprintExtraFieldDataType.class);

    public static PlanBlueprintExtraFieldDataType of(Short i) {
        return map.get(i);
    }

    public static boolean isTextType(PlanBlueprintExtraFieldDataType fieldType){
        return fieldType.equals(PlanBlueprintExtraFieldDataType.Text) || fieldType.equals(PlanBlueprintExtraFieldDataType.RichTex);
    }

    public static boolean isDateType(PlanBlueprintExtraFieldDataType fieldType){
        return fieldType.equals(PlanBlueprintExtraFieldDataType.Date);
    }

    public static boolean isNumberType(PlanBlueprintExtraFieldDataType fieldType){
        return fieldType.equals(PlanBlueprintExtraFieldDataType.Number);
    }

}
