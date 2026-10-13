package org.opencdmp.service.report;

import java.util.ArrayList;
import java.util.List;

/**
 * A validated question of a report request: which fields are read together, and how the
 * result should be labelled.
 *
 * <p>A question addressed by fieldset carries the field ids as given; one addressed by
 * semantic carries none, and finds its fields in each version separately, because a
 * semantic tag is the only name a question keeps across templates.</p>
 *
 * @param key stable key inside one request ({@code q0}, {@code q1}, ...), used to address the
 *            question from a dimension and from a cross-tab cell
 */
record ReportQuestionSpec(
        String key,
        String label,
        String numbering,
        String fieldSetId,
        List<String> fieldIds,
        String semantic,
        String fieldType) {

    boolean bySemantic() {
        return this.semantic != null && !this.semantic.isBlank();
    }

    /**
     * The fields this question reads in one particular version: the selected ids that the
     * version still carries, or every field tagged with the semantic. Empty when the version
     * does not ask the question at all.
     */
    List<FieldTarget> targetsIn(ReportDefinitionResolver.ParsedVersion version) {
        List<FieldTarget> targets = new ArrayList<>();
        if (version == null) return targets;

        if (this.bySemantic()) {
            for (String fieldId : version.fieldIdsWithSemantic(this.semantic)) {
                String fieldSetId = version.fieldSetOf(fieldId);
                if (fieldSetId != null) targets.add(new FieldTarget(fieldSetId, fieldId));
            }
            return targets;
        }

        if (this.fieldIds == null) return targets;
        for (String fieldId : this.fieldIds) {
            if (version.field(fieldId) != null) targets.add(new FieldTarget(this.fieldSetId, fieldId));
        }
        return targets;
    }

    /** One field of one fieldset, as stored in a subject's answers. */
    record FieldTarget(String fieldSetId, String fieldId) {
    }
}
