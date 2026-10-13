package org.opencdmp.service.report;

import org.opencdmp.commons.enums.ReportDimensionType;

/**
 * A validated group-by key of a report request, with the heading the response shows for it.
 * {@code questionKey} is set only when the subjects are grouped by their answer to another
 * selected question.
 */
record ReportDimensionSpec(ReportDimensionType type, String questionKey, String label) {
}
