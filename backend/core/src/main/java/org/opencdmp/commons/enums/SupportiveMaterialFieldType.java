package org.opencdmp.commons.enums;

import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum SupportiveMaterialFieldType implements DatabaseEnum<Short> {

    Faq((short) 0),
    About((short) 1),
    Glossary((short) 2),
    TermsOfService((short) 3),
    UserGuide((short) 4),
    CookiePolicy((short) 5);

    private final Short value;

    SupportiveMaterialFieldType(Short value) {
        this.value = value;
    }

    @Override
    public Short getValue() {
        return value;
    }

    private static final Map<Short, SupportiveMaterialFieldType> map = EnumUtils.getEnumValueMap(SupportiveMaterialFieldType.class);

    public static SupportiveMaterialFieldType of(Short i) {
        return map.get(i);
    }
}
