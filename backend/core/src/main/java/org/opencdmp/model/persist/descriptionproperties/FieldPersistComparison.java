package org.opencdmp.model.persist.descriptionproperties;

import org.opencdmp.model.persist.ReferencePersist;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Value-level comparison for {@link FieldPersist} pairs, shared by the plan update
 * request preview and the plan version diff. A field persist with no populated value
 * member is treated as equal to null.
 */
public final class FieldPersistComparison {

    private FieldPersistComparison() {
    }

    public static boolean isEffectivelyEmpty(FieldPersist p) {
        if (p == null) return true;
        return p.getTextValue() == null
                && p.getTextListValue() == null
                && p.getDateValue() == null
                && p.getBooleanValue() == null
                && p.getReference() == null
                && p.getReferences() == null
                && p.getTags() == null
                && p.getNumberValue() == null
                && p.getExternalIdentifier() == null;
    }

    public static boolean fieldPersistEquals(FieldPersist a, FieldPersist b) {
        if (isEffectivelyEmpty(a) && isEffectivelyEmpty(b)) return true;
        if (isEffectivelyEmpty(a) || isEffectivelyEmpty(b)) return false;
        return Objects.equals(a.getTextValue(), b.getTextValue())
                && Objects.equals(a.getTextListValue(), b.getTextListValue())
                && Objects.equals(a.getDateValue(), b.getDateValue())
                && Objects.equals(a.getBooleanValue(), b.getBooleanValue())
                && Objects.equals(a.getNumberValue(), b.getNumberValue())
                && Objects.equals(
                    a.getExternalIdentifier() != null ? a.getExternalIdentifier().getIdentifier() : null,
                    b.getExternalIdentifier() != null ? b.getExternalIdentifier().getIdentifier() : null)
                && Objects.equals(
                    a.getReference() != null ? a.getReference().getId() : null,
                    b.getReference() != null ? b.getReference().getId() : null)
                && referenceListEquals(a.getReferences(), b.getReferences())
                && Objects.equals(a.getTags(), b.getTags());
    }

    private static boolean referenceListEquals(List<ReferencePersist> a, List<ReferencePersist> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;
        List<UUID> aIds = a.stream().map(ReferencePersist::getId).sorted().collect(Collectors.toList());
        List<UUID> bIds = b.stream().map(ReferencePersist::getId).sorted().collect(Collectors.toList());
        return Objects.equals(aIds, bIds);
    }
}
