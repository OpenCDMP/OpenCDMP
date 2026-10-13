package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

@Deprecated
public enum PlanBlueprintFieldCategory implements DatabaseEnum<Short> {
    @Deprecated
    System((short) 0),
    /**
     * Unified field category for all non-system blueprint fields.
     * Uses the DescriptionTemplate field type infrastructure (FieldType enum + BaseFieldDataEntity).
     * Previously split into Extra(1), ReferenceType(2), Upload(3) — those values are kept below
     * as legacy entries so existing XML definitions can still be deserialized by the migration tool.
     */
    @Deprecated
    Extra((short) 1),
    /**
     * @deprecated Legacy — use {@link #Extra} with FieldType.REFERENCE_TYPES instead.
     * Kept for backward-compatible XML deserialization by the migration maintenance tool.
     */
    @Deprecated
    ReferenceType((short) 2),
    /**
     * @deprecated Legacy — use {@link #Extra} with FieldType.UPLOAD instead.
     * Kept for backward-compatible XML deserialization by the migration maintenance tool.
     */
    @Deprecated
    Upload((short) 3);

    public static class Names {
        @Deprecated public static final String System = "system";
        /** New unified name for all non-system fields. */
        @Deprecated public static final String Field = "field";
        /** @deprecated Legacy XML element name — kept for migration tool. */
        @Deprecated public static final String Extra = "extra";
        /** @deprecated Legacy XML element name — kept for migration tool. */
        @Deprecated public static final String ReferenceType = "referenceType";
        /** @deprecated Legacy XML element name — kept for migration tool. */
        @Deprecated public static final String Upload = "upload";
    }

    private final Short value;

    PlanBlueprintFieldCategory(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanBlueprintFieldCategory> map = EnumUtils.getEnumValueMap(PlanBlueprintFieldCategory.class);

    public static PlanBlueprintFieldCategory of(Short i) {
        return map.get(i);
    }
}
