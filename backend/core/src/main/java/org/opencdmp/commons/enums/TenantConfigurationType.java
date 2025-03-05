package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum TenantConfigurationType implements DatabaseEnum<Short> {

    DepositPlugins((short) 0),
    FileTransformerPlugins((short) 1),
    DefaultUserLocale((short) 2),
    Logo((short) 3),
    CssColors((short) 4),
    EvaluatorPlugins((short) 5),
    FeaturedEntities((short) 6),
    DefaultPlanBlueprint((short) 7);

    private final Short value;

    TenantConfigurationType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, TenantConfigurationType> map = EnumUtils.getEnumValueMap(TenantConfigurationType.class);

    public static TenantConfigurationType of(Short i) {
        return map.get(i);
    }

}
